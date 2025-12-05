const API_BASE_URL = import.meta.env.VITE_API_GATEWAY_URL ?? "http://localhost:8011";

export const LOGIN_ENDPOINT = `${API_BASE_URL}/api/auth/login`;
export const USERS_ENDPOINT = `${API_BASE_URL}/api/users`;
export const WORKSTATIONS_ENDPOINT = `${API_BASE_URL}/api/masterdata/workstations`;
export const PRODUCT_VARIANTS_ENDPOINT = `${API_BASE_URL}/api/masterdata/product-variants`;
export const MODULES_ENDPOINT = `${API_BASE_URL}/api/masterdata/modules`;
export const PARTS_ENDPOINT = `${API_BASE_URL}/api/masterdata/parts`;

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
