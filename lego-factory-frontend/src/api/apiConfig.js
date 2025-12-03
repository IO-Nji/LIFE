const API_BASE_URL = import.meta.env.VITE_USER_SERVICE_URL ?? "http://localhost:8012";

export const LOGIN_ENDPOINT = `${API_BASE_URL}/api/auth/login`;
export const USERS_ENDPOINT = `${API_BASE_URL}/api/users`;

export function readStoredSession() {
  const raw = localStorage.getItem("authSession");
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw);
  } catch (error) {
    localStorage.removeItem("authSession");
    return null;
  }
}

export function storeSession(session) {
  localStorage.setItem("authSession", JSON.stringify(session));
  localStorage.setItem("authToken", session.token);
}

export function clearStoredSession() {
  localStorage.removeItem("authSession");
  localStorage.removeItem("authToken");
}
