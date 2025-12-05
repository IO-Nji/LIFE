import axios from "axios";
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";

import { LOGIN_ENDPOINT, clearStoredSession, readStoredSession, storeSession } from "../api/apiConfig";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [session, setSession] = useState(() => readStoredSession());
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const handler = (event) => {
      if (event.key === "authSession" || event.key === "authToken") {
        setSession(readStoredSession());
      }
    };

    window.addEventListener("storage", handler);
    return () => window.removeEventListener("storage", handler);
  }, []);

  const login = useCallback(async (username, password) => {
    setLoading(true);
    try {
      const response = await axios.post(LOGIN_ENDPOINT, {
        username: username.trim(),
        password,
      });

      const { token, tokenType, expiresAt, user } = response.data;
      const payload = { token, tokenType, expiresAt, user };
      storeSession(payload);
      setSession(payload);
      return payload;
    } catch (error) {
      const message =
        error.response?.data?.message ||
        error.response?.data?.detail ||
        "Login failed. Check your credentials and try again.";
      throw new Error(message);
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    clearStoredSession();
    setSession(null);
  }, []);

  const value = useMemo(
    () => ({
      session,
      isAuthenticated: Boolean(session),
      isAdmin: session?.user?.role === "ADMIN",
      isPlantWarehouse: session?.user?.role === "PLANT_WAREHOUSE",
      loading,
      login,
      logout,
    }),
    [session, loading, login, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
