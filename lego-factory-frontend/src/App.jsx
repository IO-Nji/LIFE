import { useEffect, useState } from "react";
import { Link, Navigate, Route, Routes, useNavigate } from "react-router-dom";
import HomePage from "./pages/HomePage.jsx";
import DashboardPage from "./pages/DashboardPage.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import UserManagementPage from "./pages/UserManagementPage.jsx";
import { clearStoredSession, readStoredSession } from "./api/apiConfig";

function App() {
  const [session, setSession] = useState(() => readStoredSession());
  const navigate = useNavigate();

  useEffect(() => {
    const handleStorage = (event) => {
      if (event.key === "authSession" || event.key === "authToken") {
        setSession(readStoredSession());
      }
    };

    window.addEventListener("storage", handleStorage);
    return () => window.removeEventListener("storage", handleStorage);
  }, []);

  const handleLogout = () => {
    clearStoredSession();
    setSession(null);
    navigate("/login");
  };

  const isAdmin = session?.user?.role === "ADMIN";

  return (
    <div className="app-shell">
      <header className="app-header">
        <h1>LEGO Factory Control</h1>
        <nav>
          <ul className="nav-list">
            <li><Link to="/">Home</Link></li>
            <li><Link to="/dashboard">Dashboard</Link></li>
            {!session && <li><Link to="/login">Login</Link></li>}
            {isAdmin && <li><Link to="/users">User Admin</Link></li>}
            {session && (
              <li>
                <button type="button" className="link-button" onClick={handleLogout}>
                  Log out
                </button>
              </li>
            )}
          </ul>
        </nav>
      </header>
      <main className="app-content">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route
            path="/login"
            element={<LoginPage onLogin={setSession} session={session} />}
          />
          <Route
            path="/users"
            element={
              isAdmin ? (
                <UserManagementPage session={session} onSessionExpired={() => setSession(null)} />
              ) : (
                <Navigate to={session ? "/dashboard" : "/login"} replace />
              )
            }
          />
        </Routes>
      </main>
    </div>
  );
}

export default App;
