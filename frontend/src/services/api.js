import axios from 'axios'

/**
 * Axios instance pre-configured for the Greenhouse API.
 *
 * baseURL is resolved from the VITE_API_BASE_URL environment variable so the
 * same build artifact works in every environment:
 *   - Development : VITE_API_BASE_URL is unset → falls back to localhost:8080
 *   - Production  : VITE_API_BASE_URL=https://greenhouse-automation-production.up.railway.app
 *
 * The browser sends the JSESSIONID session cookie on every request because
 * the URL is always the exact origin that issued the cookie.
 *
 * WHY NOT baseURL '/' + Vite proxy?
 *   The Vite proxy makes a server-to-server (Node.js → Spring) call.
 *   The browser's cookie jar is never consulted for that server-side leg,
 *   so the JSESSIONID set by the backend during the OAuth2 handshake is
 *   never forwarded — Spring gets an anonymous request and returns 401.
 *
 * WHY withCredentials: true?
 *   Required so the browser attaches the session cookie on cross-origin
 *   requests (localhost:5173 → localhost:8080 in dev, or frontend domain →
 *   Railway domain in production).  Spring's CORS config must match:
 *   allowedOrigins(frontendOrigin) + allowCredentials(true).
 */
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const api = axios.create({
  baseURL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
})

/**
 * Response interceptor — redirects to /login on HTTP 401.
 *
 * Suppressed in two situations to avoid redirect loops:
 *  1. _isAuthCheck flag — fetchMe() handles 401 by setting user = null.
 *  2. Already on /login — a second redirect would cause an infinite reload.
 */
api.interceptors.response.use(
  response => response,
  error => {
    const is401       = error.response?.status === 401
    const isAuthCheck = error.config?._isAuthCheck === true
    const onLogin     = window.location.pathname === '/login'

    if (is401 && !isAuthCheck && !onLogin) {
      window.location.href = '/login'
    }

    return Promise.reject(error)
  }
)

export default api
