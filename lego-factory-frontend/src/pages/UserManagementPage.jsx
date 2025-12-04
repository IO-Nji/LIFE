import { useState } from "react";
import axios from "axios";

import { USERS_ENDPOINT } from "../api/apiConfig";
import { useAuth } from "../context/AuthContext.jsx";

const ROLE_OPTIONS = [
  "ADMIN",
  "PLANT_WAREHOUSE",
  "MODULES_SUPERMARKET",
  "PRODUCTION_PLANNING",
  "PRODUCTION_CONTROL",
  "ASSEMBLY_CONTROL",
  "PARTS_SUPPLY",
  "MANUFACTURING",
  "VIEWER",
];

function UserManagementPage() {
  const { session, isAdmin, logout } = useAuth();
  const [form, setForm] = useState({
    username: "",
    password: "",
    role: "VIEWER",
    workstationId: "",
  });
  const [feedback, setFeedback] = useState({ type: "", message: "" });
  const [submitting, setSubmitting] = useState(false);

  const authToken = session?.token ?? null;

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const resetFeedback = () => setFeedback({ type: "", message: "" });

  const handleSubmit = async (event) => {
    event.preventDefault();
    resetFeedback();

    if (!authToken || !isAdmin) {
      setFeedback({ type: "error", message: "Please sign in as an administrator first." });
      return;
    }

    if (!form.username.trim() || !form.password.trim()) {
      setFeedback({ type: "error", message: "Username and password are required." });
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        username: form.username.trim(),
        password: form.password,
        role: form.role,
        workstationId: form.workstationId ? Number(form.workstationId) : null,
      };

      await axios.post(USERS_ENDPOINT, payload, {
        headers: {
          Authorization: `Bearer ${authToken}`,
        },
      });

      setFeedback({ type: "success", message: `User ${payload.username} created.` });
      setForm({ username: "", password: "", role: form.role, workstationId: "" });
    } catch (error) {
      if (error.response?.status === 401) {
        logout();
      }

      const message =
        error.response?.data?.message ||
        error.response?.data?.detail ||
        (error.response?.status === 403
          ? "You do not have permission to manage users."
          : "Unable to create user. Confirm your admin session is valid.");
      setFeedback({ type: "error", message });
    } finally {
      setSubmitting(false);
    }
  };

  if (!session) {
    return (
      <section className="form-section">
        <h2>Administrator sign-in required</h2>
        <p className="form-helper">Log in with an administrator account to manage users.</p>
        {feedback.message && (
          <p className={feedback.type === "error" ? "form-error" : "form-success"}>{feedback.message}</p>
        )}
      </section>
    );
  }

  if (!isAdmin) {
    return (
      <section className="form-section">
        <h2>Insufficient permissions</h2>
        <p className="form-helper">
          You are signed in as <strong>{session.user?.username}</strong>, but only administrators can create
          new users.
        </p>
      </section>
    );
  }

  return (
    <section className="form-section">
      <h2>Admin: Create New User</h2>
      <p className="form-helper">
        Use your administrator token to add operators for other factory roles.
      </p>
      <form className="form-card" onSubmit={handleSubmit} noValidate>
        <label htmlFor="username">Username</label>
        <input
          id="username"
          name="username"
          type="text"
          value={form.username}
          onChange={handleChange}
          disabled={submitting}
          required
        />

        <label htmlFor="password">Password</label>
        <input
          id="password"
          name="password"
          type="password"
          value={form.password}
          onChange={handleChange}
          disabled={submitting}
          required
        />

        <label htmlFor="role">Role</label>
        <select
          id="role"
          name="role"
          value={form.role}
          onChange={handleChange}
          disabled={submitting}
        >
          {ROLE_OPTIONS.map((role) => (
            <option key={role} value={role}>
              {role}
            </option>
          ))}
        </select>

        <label htmlFor="workstationId">Workstation ID (optional)</label>
        <input
          id="workstationId"
          name="workstationId"
          type="number"
          value={form.workstationId}
          onChange={handleChange}
          disabled={submitting}
          min="0"
        />

        <button type="submit" className="primary-link" disabled={submitting}>
          {submitting ? "Creating..." : "Create user"}
        </button>
      </form>
      {feedback.message && (
        <p
          className={feedback.type === "error" ? "form-error" : "form-success"}
          role={feedback.type === "error" ? "alert" : "status"}
        >
          {feedback.message}
        </p>
      )}
    </section>
  );
}

export default UserManagementPage;
