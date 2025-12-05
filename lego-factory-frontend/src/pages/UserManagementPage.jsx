import { useState, useEffect } from "react";
import axios from "axios";

import { USERS_ENDPOINT, WORKSTATIONS_ENDPOINT } from "../api/apiConfig";
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
  const [users, setUsers] = useState([]);
  const [workstations, setWorkstations] = useState([]);
  const [editingUserId, setEditingUserId] = useState(null);
  const [editForm, setEditForm] = useState({
    role: "",
    workstationId: "",
  });
  const [feedback, setFeedback] = useState({ type: "", message: "" });
  const [submitting, setSubmitting] = useState(false);
  const [createdUser, setCreatedUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const authToken = session?.token ?? null;

  // Load users and workstations on mount
  useEffect(() => {
    if (isAdmin && authToken) {
      loadUsersAndWorkstations();
    } else {
      setLoading(false);
    }
  }, [isAdmin, authToken]);

  const loadUsersAndWorkstations = async () => {
    try {
      const [usersRes, workstationsRes] = await Promise.all([
        axios.get(USERS_ENDPOINT, {
          headers: { Authorization: `Bearer ${authToken}` },
        }),
        axios.get(WORKSTATIONS_ENDPOINT, {
          headers: { Authorization: `Bearer ${authToken}` },
        }),
      ]);
      setUsers(usersRes.data || []);
      setWorkstations(workstationsRes.data || []);
    } catch (error) {
      console.error("Failed to load users/workstations:", error);
      if (error.response?.status === 401) {
        logout();
      }
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleEditChange = (event) => {
    const { name, value } = event.target;
    setEditForm((current) => ({ ...current, [name]: value }));
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

      const response = await axios.post(USERS_ENDPOINT, payload, {
        headers: {
          Authorization: `Bearer ${authToken}`,
        },
      });

      const userData = response.data;
      setCreatedUser(userData);
      setUsers([...users, userData]);
      setFeedback({ 
        type: "success", 
        message: `User "${userData.username}" created successfully (ID: ${userData.id}, Role: ${userData.role})` 
      });
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

  const startEditUser = (user) => {
    setEditingUserId(user.id);
    setEditForm({
      role: user.role,
      workstationId: user.workstationId || "",
    });
  };

  const cancelEdit = () => {
    setEditingUserId(null);
    setEditForm({ role: "", workstationId: "" });
  };

  const saveUserUpdate = async (userId) => {
    resetFeedback();
    setSubmitting(true);
    try {
      const user = users.find((u) => u.id === userId);
      const payload = {
        username: user.username,
        role: editForm.role,
        workstationId: editForm.workstationId ? Number(editForm.workstationId) : null,
      };

      const response = await axios.put(`${USERS_ENDPOINT}/${userId}`, payload, {
        headers: {
          Authorization: `Bearer ${authToken}`,
        },
      });

      setUsers(users.map((u) => (u.id === userId ? response.data : u)));
      setEditingUserId(null);
      setFeedback({
        type: "success",
        message: `User "${response.data.username}" updated successfully`,
      });
    } catch (error) {
      if (error.response?.status === 401) {
        logout();
      }
      const message =
        error.response?.data?.message ||
        error.response?.data?.detail ||
        "Unable to update user.";
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
    <div className="user-management-landscape">
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

          <label htmlFor="workstationId">Workstation</label>
          <select
            id="workstationId"
            name="workstationId"
            value={form.workstationId}
            onChange={handleChange}
            disabled={submitting}
          >
            <option value="">-- None --</option>
            {workstations.map((ws) => (
              <option key={ws.id} value={ws.id}>
                {ws.name}
              </option>
            ))}
          </select>

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
        {createdUser && (
          <div className="form-success-details">
            <h3>Created User Details:</h3>
            <ul>
              <li><strong>ID:</strong> {createdUser.id}</li>
              <li><strong>Username:</strong> {createdUser.username}</li>
              <li><strong>Role:</strong> {createdUser.role}</li>
              {createdUser.workstationId && (
                <li><strong>Workstation ID:</strong> {createdUser.workstationId}</li>
              )}
            </ul>
          </div>
        )}
      </section>

      <section className="form-section">
        <h2>Manage Existing Users</h2>
        {loading ? (
          <p>Loading users...</p>
        ) : users.length === 0 ? (
          <p className="form-helper">No users found.</p>
        ) : (
          <div className="users-table-container">
            <table className="users-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Username</th>
                  <th>Role</th>
                  <th>Workstation</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => {
                  const isEditing = editingUserId === user.id;
                  const workstationName = workstations.find(
                    (ws) => ws.id === user.workstationId
                  )?.name || "-- None --";

                  return (
                    <tr key={user.id}>
                      <td>{user.id}</td>
                      <td>{user.username}</td>
                      <td>
                        {isEditing ? (
                          <select
                            value={editForm.role}
                            onChange={(e) =>
                              setEditForm({ ...editForm, role: e.target.value })
                            }
                          >
                            {ROLE_OPTIONS.map((role) => (
                              <option key={role} value={role}>
                                {role}
                              </option>
                            ))}
                          </select>
                        ) : (
                          user.role
                        )}
                      </td>
                      <td>
                        {isEditing ? (
                          <select
                            value={editForm.workstationId}
                            onChange={(e) =>
                              setEditForm({
                                ...editForm,
                                workstationId: e.target.value,
                              })
                            }
                          >
                            <option value="">-- None --</option>
                            {workstations.map((ws) => (
                              <option key={ws.id} value={ws.id}>
                                {ws.name}
                              </option>
                            ))}
                          </select>
                        ) : (
                          workstationName
                        )}
                      </td>
                      <td>
                        {isEditing ? (
                          <>
                            <button
                              className="primary-link"
                              onClick={() => saveUserUpdate(user.id)}
                              disabled={submitting}
                            >
                              {submitting ? "Saving..." : "Save"}
                            </button>
                            <button
                              className="secondary-link"
                              onClick={cancelEdit}
                              disabled={submitting}
                              style={{ marginLeft: "8px" }}
                            >
                              Cancel
                            </button>
                          </>
                        ) : (
                          <button
                            className="primary-link"
                            onClick={() => startEditUser(user)}
                          >
                            Edit
                          </button>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}

export default UserManagementPage;
