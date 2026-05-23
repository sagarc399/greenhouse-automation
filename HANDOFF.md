# Greenhouse Management System — Project Handoff

> **Purpose:** Complete context document for a new chat session.  
> **As of:** 2026-05-23  
> **Status:** Fully working — login, CRUD, role-based access, automation, alerts.

---

## 1. Project Overview

An automated greenhouse management system with a Spring Boot REST backend and a Vue 3 SPA frontend. Users authenticate with their Google account via OAuth2/OIDC. Once logged in, they manage greenhouses, zones, sensors, actuators, automation rules, and alerts. Automation rules fire automatically when a sensor reading breaches a threshold, toggling actuators and creating alert records.

**Repository root:** `C:\dev\Proyecto Invernadero\greenhouse-app\`

```
greenhouse-app/
├── backend/          Spring Boot 3 + PostgreSQL
├── frontend/         Vue 3 + Vite + Tailwind
├── run-backend.ps1   Local dev launcher (gitignored — contains credentials)
├── .gitignore
└── HANDOFF.md        ← this file
```

---

## 2. Tech Stack

### Backend
| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security 6 + OAuth2 Client (OIDC) |
| Persistence | Spring Data JPA + Hibernate |
| Database | PostgreSQL 15 (`greenhouse_db`) |
| API docs | SpringDoc OpenAPI 2.5 (Swagger UI at `/swagger-ui.html`) |
| Build | Maven |
| Extras | Lombok, Bean Validation, i18n messages |

### Frontend
| Layer | Technology |
|---|---|
| Framework | Vue 3 (Composition API, `<script setup>`) |
| Build | Vite 5 |
| Routing | Vue Router 4 |
| State | Pinia 2 |
| HTTP | Axios 1.7 |
| Styling | Tailwind CSS 3 |
| i18n | vue-i18n 9 (EN + ES, persisted in localStorage) |

---

## 3. How to Run

### Prerequisites
- Java 17+, Maven, Node 18+, PostgreSQL running locally
- Database `greenhouse_db` created on `localhost:5432` with user `postgres` / password `postgre123`

### Backend
```powershell
# From project root — sets Google OAuth2 env vars and starts Spring Boot
.\run-backend.ps1
```
Or manually:
```powershell
$env:GOOGLE_CLIENT_ID="633682661067-n4popcn9qcbtsmo2vj23eoqu3shqj5f7.apps.googleusercontent.com"
$env:GOOGLE_CLIENT_SECRET="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"
cd backend
mvn spring-boot:run
```
Backend runs on **http://localhost:8080**

### Frontend
```bash
cd frontend
npm install   # first time only
npm run dev
```
Frontend runs on **http://localhost:5173**

### First login
Navigate to `http://localhost:5173` → redirected to `/login` → click "Sign in with Google" → Google OIDC flow → redirected to `/dashboard`.

**First-time users get `OPERATOR` role.** To promote to `ADMIN`, run directly in PostgreSQL:
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'your@email.com';
```

---

## 4. Architecture

### Domain Model (entity hierarchy)

```
Greenhouse
  └── Zone (1:N)
        ├── Sensor (1:N)
        │     ├── SensorReading (1:N)  — time-series readings
        │     └── Alert (1:N)          — triggered alerts
        └── Actuator (1:N)
              └── AutomationRule (via Zone + Sensor + Actuator)
User            — OAuth2 users with role ADMIN | OPERATOR
```

### Automation flow
```
POST /api/sensor-readings
  → SensorReadingService.create()
  → saved to DB
  → AutomationRuleService.evaluateRulesForSensor(sensor, value)
      for each active rule matching this sensor:
        if rule.evaluate(value) == true:
          → actuatorRepository.save(actuator with target state)
          → alertService.createFromRule(message, MEDIA severity, ...)
```

### Authentication flow
```
Browser → /oauth2/authorization/google
  → Google OIDC
  → CustomOidcUserService.loadUser()
      → DB upsert (new user gets OPERATOR role)
      → returns DefaultOidcUser with ROLE_ADMIN/ROLE_OPERATOR authority added
  → Spring stores enriched DefaultOidcUser in HTTP session (JSESSIONID cookie)
  → redirect to http://localhost:5173/dashboard

Frontend page load:
  → router.beforeEach() → authStore.init() → GET /api/user/me
      200: set user { name, email, role } in Pinia store
      401: user = null → redirect to /login
```

---

## 5. Backend — Package Structure

```
com.greenhouse.app
├── config/
│   ├── SecurityConfig.java        Spring Security filter chain, CORS, OAuth2, logout
│   └── OpenApiConfig.java         Swagger metadata
├── controller/
│   ├── UserController.java        GET /api/user/me
│   ├── DashboardController.java   GET /api/dashboard
│   ├── GreenhouseController.java  CRUD /api/greenhouses
│   ├── ZoneController.java        CRUD /api/zones
│   ├── SensorController.java      CRUD /api/sensors
│   ├── ActuatorController.java    CRUD /api/actuators
│   ├── AutomationRuleController.java  CRUD /api/automation-rules
│   ├── AlertController.java       CRUD + status update /api/alerts
│   └── SensorReadingController.java   paginated /api/sensor-readings
├── dto/                           Java records (request + response payloads)
├── entity/                        JPA entities (Greenhouse, Zone, Sensor, Actuator,
│                                  SensorReading, AutomationRule, Alert, User)
├── exception/
│   ├── ResourceNotFoundException.java
│   └── GlobalExceptionHandler.java    @RestControllerAdvice → 404/400/500 JSON
├── repository/                    Spring Data JPA interfaces
├── security/
│   ├── CustomOAuth2User.java      Wrapper for OAuth2User (non-OIDC path)
│   ├── CustomOAuth2UserService.java   Non-OIDC user service
│   └── CustomOidcUserService.java     OIDC user service — upserts DB user,
│                                      adds ROLE_xxx authority to principal
└── service/                       Business logic layer
```

---

## 6. Backend — Key Files in Detail

### `SecurityConfig.java`
Located at `backend/src/main/java/com/greenhouse/app/config/SecurityConfig.java`

**Authorization rules (first-match-wins):**
```java
.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", ...).permitAll()
.requestMatchers("/login/**", "/oauth2/**").permitAll()
// OPERATOR exceptions (must precede ADMIN catch-all)
.requestMatchers(POST,   "/api/sensor-readings/**").hasAnyRole("ADMIN","OPERATOR")
.requestMatchers(PUT,    "/api/alerts/**").hasAnyRole("ADMIN","OPERATOR")
// ADMIN: all writes
.requestMatchers(POST,   "/api/**").hasRole("ADMIN")
.requestMatchers(PUT,    "/api/**").hasRole("ADMIN")
.requestMatchers(PATCH,  "/api/**").hasRole("ADMIN")
.requestMatchers(DELETE, "/api/**").hasRole("ADMIN")
// All authenticated: reads
.requestMatchers(GET,    "/api/**").hasAnyRole("ADMIN","OPERATOR")
.anyRequest().authenticated()
```

**Logout config:**
- URL: `POST /logout` (Spring Security 6 only processes the LogoutFilter on POST)
- After logout: redirects to `http://localhost:5173/login`
- Clears `JSESSIONID` cookie, invalidates HTTP session

**CORS:** Allows `http://localhost:5173` and `http://localhost:3000` with `allowCredentials = true`.

**Unauthenticated `/api/**` calls:** returns `HTTP 401` JSON instead of redirecting to OAuth2 (so Axios can distinguish "not logged in" from actual errors).

### `CustomOidcUserService.java`
Located at `backend/src/main/java/com/greenhouse/app/security/CustomOidcUserService.java`

Critical class — fixes the 403 issue that occurs when Google OIDC login stores a `DefaultOidcUser` with no `ROLE_ADMIN` authority.

```java
@Override
public OidcUser loadUser(OidcUserRequest userRequest) {
    OidcUser oidcUser = super.loadUser(userRequest);
    // 1. Upsert user in DB (new users get OPERATOR)
    User saved = userRepository.save(user);
    User.UserRole appRole = saved.getRole() != null ? saved.getRole() : OPERATOR;
    // 2. Add ROLE_ADMIN or ROLE_OPERATOR to authority set
    Set<GrantedAuthority> authorities = new LinkedHashSet<>(oidcUser.getAuthorities());
    authorities.add(new SimpleGrantedAuthority("ROLE_" + appRole.name()));
    // 3. Return enriched principal
    return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
}
```

**Why this matters:** Without this, `DefaultOidcUser.getAuthorities()` only contains `[OIDC_USER, SCOPE_openid, SCOPE_profile, SCOPE_email]`. Every `hasRole("ADMIN")` check fails → HTTP 403 on all write endpoints.

### `UserController.java`
`GET /api/user/me` — handles all three possible Spring Security principal types:
1. `CustomOAuth2User` — DB User directly available (non-OIDC path)
2. `OidcUser` (DefaultOidcUser) — extract from OIDC claims, look up role in DB
3. `OAuth2User` — extract from attributes map, look up role in DB

Returns: `{ name, email, role }` where role is `"ADMIN"` | `"OPERATOR"` | `null`.

### `application.properties`
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/greenhouse_db
spring.datasource.username=postgres
spring.datasource.password=postgre123
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=openid,profile,email
app.frontend.base-url=http://localhost:5173
```
Google credentials are injected via environment variables (set in `run-backend.ps1` or `backend/.env.local`).

---

## 7. Frontend — File Structure

```
frontend/src/
├── main.js                        App bootstrap (Pinia, Router, i18n, Tailwind)
├── App.vue                        Root component (<RouterView>)
├── assets/main.css                Tailwind directives
├── i18n/index.js                  EN + ES translations (locale saved in localStorage)
├── router/index.js                Routes + async navigation guard
├── stores/
│   ├── auth.js                    Pinia auth store (user, init, logout)
│   └── greenhouse.js              (Pinia store for greenhouse list cache)
├── services/
│   ├── api.js                     Axios instance (baseURL=:8080, withCredentials=true)
│   ├── greenhouseService.js       CRUD helpers → /api/greenhouses
│   ├── zoneService.js
│   ├── sensorService.js
│   ├── actuatorService.js
│   ├── automationRuleService.js
│   ├── alertService.js
│   ├── sensorReadingService.js
│   └── dashboardService.js
├── components/
│   ├── AppLayout.vue              Sidebar shell (role-filtered nav links, locale toggle, logout)
│   └── DashboardCard.vue          Stat card widget
└── views/
    ├── LoginView.vue              Google OAuth2 login button
    ├── DashboardView.vue          Summary cards from GET /api/dashboard
    ├── GreenhouseView.vue         CRUD table (ADMIN only)
    ├── ZoneView.vue               CRUD table (ADMIN only)
    ├── SensorView.vue             CRUD table (ADMIN only)
    ├── ActuatorView.vue           CRUD table + state toggle (ADMIN only)
    ├── AutomationRuleView.vue     CRUD table (ADMIN only)
    ├── AlertView.vue              Table + mark-as-attended (ADMIN + OPERATOR)
    └── SensorReadingView.vue      Paginated table per sensor (ADMIN + OPERATOR)
```

---

## 8. Frontend — Key Patterns

### Auth store (`stores/auth.js`)
```js
// Called by every route guard on page load — idempotent, concurrent-safe
async function init() {
  if (initialized.value) return   // already done
  initialized.value = true
  initPromise = fetchMe()          // store the in-flight promise
  await initPromise                // concurrent callers await the same one
}

async function logout() {
  await api.post('/logout')        // POST (not GET) — Spring Security 6 requirement
  user.value = null
  initialized.value = false
  router.push('/login')            // Vue Router navigation (no race condition)
}
```

### Axios instance (`services/api.js`)
- `baseURL: 'http://localhost:8080'` — direct browser→backend (NOT Vite proxy, because the Vite proxy is a server-side Node call that never sends the browser's JSESSIONID cookie)
- `withCredentials: true` — required for cross-origin session cookies
- 401 interceptor redirects to `/login`, except when `config._isAuthCheck === true` (used by `fetchMe()` to suppress the redirect)

### Router guard (`router/index.js`)
```js
router.beforeEach(async (to, _from, next) => {
  await authStore.init()                           // verify session once per page load
  if (to.name === 'Login' && authStore.isAuthenticated) return next('/dashboard')
  if (to.meta.requiresAuth === false) return next()
  if (!authStore.isAuthenticated) return next('/login')
  if (to.meta.roles && !to.meta.roles.includes(authStore.user?.role)) return next('/dashboard')
  next()
})
```

### Role-based sidebar (`components/AppLayout.vue`)
```js
const allLinks = [
  { to: '/dashboard',        roles: ['ADMIN', 'OPERATOR'] },
  { to: '/greenhouses',      roles: ['ADMIN'] },
  { to: '/zones',            roles: ['ADMIN'] },
  { to: '/sensors',          roles: ['ADMIN'] },
  { to: '/actuators',        roles: ['ADMIN'] },
  { to: '/automation-rules', roles: ['ADMIN'] },
  { to: '/alerts',           roles: ['ADMIN', 'OPERATOR'] },
  { to: '/sensor-readings',  roles: ['ADMIN', 'OPERATOR'] }
]
const visibleLinks = computed(() =>
  allLinks.filter(link => link.roles.includes(authStore.user?.role))
)
```

---

## 9. API Reference

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/user/me` | Any | Current user profile |
| GET | `/api/dashboard` | Any | System summary stats |
| GET/POST/PUT/DELETE | `/api/greenhouses/**` | ADMIN | Greenhouse CRUD |
| GET/POST/PUT/DELETE | `/api/zones/**` | ADMIN | Zone CRUD |
| GET/POST/PUT/DELETE | `/api/sensors/**` | ADMIN | Sensor CRUD |
| GET/POST/PUT/DELETE | `/api/actuators/**` | ADMIN | Actuator CRUD |
| GET/PUT/PATCH `/{id}/state` | `/api/actuators/**` | ADMIN | Actuator state |
| GET/POST/PUT/DELETE | `/api/automation-rules/**` | ADMIN | Rule CRUD |
| GET | `/api/sensor-readings` | ADMIN+OP | Paginated readings by sensor |
| POST | `/api/sensor-readings` | ADMIN+OP | Record new reading (triggers rules) |
| DELETE | `/api/sensor-readings/{id}` | ADMIN | Delete reading |
| GET | `/api/alerts` | ADMIN+OP | All alerts |
| GET | `/api/alerts/pending` | ADMIN+OP | Pending alerts |
| PUT | `/api/alerts/{id}/status` | ADMIN+OP | Mark attended |
| POST/DELETE | `/api/alerts/**` | ADMIN | Create/delete alert |
| POST | `/logout` | Any | Invalidate session |

Full interactive docs: `http://localhost:8080/swagger-ui.html` (when backend is running)

---

## 10. Database

**Connection:** `jdbc:postgresql://localhost:5432/greenhouse_db`  
**User:** `postgres` / **Password:** `postgre123`  
**DDL:** `spring.jpa.hibernate.ddl-auto=update` (Hibernate auto-migrates on startup)

### Tables
| Table | Key columns |
|---|---|
| `users` | `id`, `email` (unique), `name`, `picture_url`, `provider`, `provider_id`, `role` (ADMIN\|OPERATOR) |
| `greenhouses` | `id`, `name`, `location`, `description` |
| `zones` | `id`, `name`, `description`, `greenhouse_id` |
| `sensors` | `id`, `name`, `type`, `model`, `active`, `zone_id` |
| `sensor_readings` | `id`, `value`, `unit`, `recorded_at`, `sensor_id` |
| `actuators` | `id`, `name`, `type`, `state` (ENCENDIDO\|APAGADO), `zone_id` |
| `automation_rules` | `id`, `name`, `operator`, `threshold`, `active`, `zone_id`, `sensor_id`, `actuator_id`, `actuator_target_state` |
| `alerts` | `id`, `message`, `severity`, `status` (PENDIENTE\|ATENDIDA), `trigger_value`, `sensor_id`, `automation_rule_id` |

---

## 11. Enum Reference

### Backend enums (Spanish, stored as VARCHAR)
| Enum | Values |
|---|---|
| `User.UserRole` | `ADMIN`, `OPERATOR` |
| `Sensor.SensorType` | `TEMPERATURA`, `HUMEDAD_AMBIENTAL`, `HUMEDAD_SUELO`, `LUZ`, `PH` |
| `Actuator.ActuatorType` | `RIEGO`, `VENTILACION`, `ILUMINACION`, `CALEFACCION` |
| `Actuator.ActuatorState` | `ENCENDIDO`, `APAGADO` |
| `AutomationRule.RuleOperator` | `MAYOR_QUE`, `MENOR_QUE`, `IGUAL_QUE` |
| `Alert.AlertSeverity` | `BAJA`, `MEDIA`, `ALTA`, `CRITICA` |
| `Alert.AlertStatus` | `PENDIENTE`, `ATENDIDA` |

---

## 12. Environment & Secrets

**Never committed to git.** The following files are gitignored:

| File | Contains |
|---|---|
| `backend/.env.local` | `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` values |
| `run-backend.ps1` | Same credentials as PowerShell env-var exports |

The `application.properties` reads these as `${GOOGLE_CLIENT_ID}` / `${GOOGLE_CLIENT_SECRET}`.

The real values are:
- Client ID: `633682661067-n4popcn9qcbtsmo2vj23eoqu3shqj5f7.apps.googleusercontent.com`
- Client Secret: `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`

---

## 13. Known Design Decisions & Gotchas

### OIDC vs OAuth2 principal type
Google always uses OIDC (sends an ID token with `openid` scope). Spring Security stores a `DefaultOidcUser`, **not** a `CustomOAuth2User`. The `CustomOidcUserService` must:
1. Upsert the user in the DB
2. Return a **new** `DefaultOidcUser` with `ROLE_ADMIN`/`ROLE_OPERATOR` added to the authority set

Without step 2, `hasRole("ADMIN")` always fails → HTTP 403 on every write endpoint.

### Logout must be POST
Spring Security 6 `LogoutFilter` only matches POST requests. Sending a GET to `/logout` falls through to the dispatcher servlet with no handler → HTTP 500. The frontend always calls `api.post('/logout')`.

### Axios baseURL is `:8080`, not Vite proxy
The Vite dev proxy makes a server-to-server Node.js call that never carries the browser's cookie jar. The `JSESSIONID` cookie set during the Google OAuth2 redirect is tied to `localhost:8080` in the browser. If proxied, Spring gets an anonymous (cookieless) request → 401. Direct `baseURL: 'http://localhost:8080'` + `withCredentials: true` is the correct approach.

### Circular dependency: AutomationRuleService ↔ AlertService
`AutomationRuleService` calls `AlertService.createFromRule()`.  
`AlertService` uses `SensorService.getOrThrow()`.  
`SensorReadingService` calls both `SensorService` and `AutomationRuleService`.  
The cycle is broken by `@Lazy AlertService alertService` in `AutomationRuleService`'s constructor.

### `init()` race condition guard
Multiple Vue route guards can fire simultaneously before the first `GET /api/user/me` response arrives. `initPromise` stores the in-flight `fetchMe()` Promise so all concurrent callers `await` the same network request.

### i18n locale persistence
`vue-i18n` locale defaults to what's in `localStorage.getItem('locale')` — falls back to `'es'` if nothing is stored. The sidebar "EN/ES" toggle updates both `locale.value` and `localStorage`.

---

## 14. Tests

Located at `backend/src/test/java/com/greenhouse/app/`:

| File | Type | What it tests |
|---|---|---|
| `service/GreenhouseServiceTest.java` | JUnit + Mockito | Greenhouse CRUD service layer |
| `service/SensorServiceTest.java` | JUnit + Mockito | Sensor CRUD service layer |
| `service/AutomationRuleServiceTest.java` | JUnit + Mockito | Rule evaluation logic |
| `selenium/LoginFlowTest.java` | Selenium + WebDriverManager | End-to-end Google login flow |

Test profile (`application-test.properties`) uses H2 in-memory database.

Run tests:
```bash
cd backend
mvn test
```

---

## 15. What Is NOT Done Yet (Potential Next Steps)

- **Production deployment** — no Docker, no CI/CD pipeline configured yet
- **Refresh token / session expiry handling** — if the Spring session expires, the frontend gets 401 and redirects to login (works), but there is no proactive session-renewal logic
- **Real-time updates** — no WebSocket or SSE; the dashboard is not live-updated when new readings come in
- **Password-based login** — only Google OAuth2 is supported; no local username/password
- **OPERATOR role in frontend** — the views for OPERATOR (Alerts, Sensor Readings) are functional but no dedicated OPERATOR-specific workflow (e.g., bulk acknowledge alerts) has been built
- **Pagination on most tables** — only `SensorReadingView` has pagination; other tables load all records
- **Frontend unit tests** — no Vitest tests written for Vue components or services
