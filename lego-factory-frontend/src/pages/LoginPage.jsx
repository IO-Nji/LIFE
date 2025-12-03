import { useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import axios from "axios";

import { LOGIN_ENDPOINT, storeSession } from "../api/apiConfig";

function LoginPage({ onLogin, session }) {
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: "", password: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  if (session) {
    return <Navigate to="/dashboard" replace />;
  }

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    setSuccess("");

    if (!form.username.trim() || !form.password) {
      setError("Username and password are required.");
      return;
    }

    setLoading(true);
    try {
      const response = await axios.post(LOGIN_ENDPOINT, {
        username: form.username.trim(),
        password: form.password,
      });

      const { token, tokenType, expiresAt, user } = response.data;
      const sessionPayload = { token, tokenType, expiresAt, user };
      storeSession(sessionPayload);
      onLogin?.(sessionPayload);
      setSuccess("Login successful. Redirecting to dashboard...");
      setTimeout(() => navigate("/dashboard"), 750);
    } catch (requestError) {
      const message =
        requestError.response?.data?.message ||
        "Login failed. Check your credentials and try again.";
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="form-section">
      <h2>Sign In</h2>
      <p className="form-helper">Enter your LEGO factory credentials.</p>
      <form className="form-card" onSubmit={handleSubmit} noValidate>
        <label htmlFor="username">Username</label>
        <input
          id="username"
          name="username"
          type="text"
          autoComplete="username"
          value={form.username}
          onChange={handleChange}
          disabled={loading}
          required
        />

        <label htmlFor="password">Password</label>
        <input
          id="password"
          name="password"
          type="password"
          autoComplete="current-password"
          value={form.password}
          onChange={handleChange}
          disabled={loading}
          required
        />

        <button type="submit" className="primary-link" disabled={loading}>
          {loading ? "Signing in..." : "Sign in"}
        </button>
      </form>
      {error && (
        <p className="form-error" role="alert">
          {error}
        </p>
      )}
      {success && (
        <p className="form-success" role="status">
          {success}
        </p>
      )}
    </section>
  );
}

export default LoginPage;
