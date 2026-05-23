import axios from 'axios'

/**
 * Axios instance pre-configured for the Greenhouse API.
 *
 * baseURL points directly at the Spring Boot backend (port 8080) so the
 * browser sends the JSESSIONID session cookie itself on every request.
 *
 * WHY NOT baseURL '/' + Vite proxy?
 *   The Vite proxy makes a server-to-server (Node.js → Spring) call.
 *   The browser's cookie jar is never consulted for that server-side leg,
 *   so the JSESSIONID set by localhost:8080 during the OAuth2 handshake is
 *   never forwarded — Spring gets an anonymous request and returns 401.
 *
 * WHY withCredentials: true?
 *   Required so the browser attaches the session cookie on cross-origin
 *   requests from localhost:5173 → localhost:8080.
 *   Spring's CORS config must match: allowedOrigins("http://localhost:5173")
 *   + allowCredentials(true).
 */
const api = axios.create({
  baseURL: 'http://localhost:8080',
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
