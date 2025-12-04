import { Link, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

function DashboardLayout() {
  const { isAuthenticated, isAdmin, logout, session } = useAuth();

  return (
    <div className="app-shell">
      <header className="app-header">
        <h1>LEGO Factory Control</h1>
        <nav>
          <ul className="nav-list">
            <li><Link to="/">Home</Link></li>
            <li><Link to="/dashboard">Dashboard</Link></li>
            {isAdmin && <li><Link to="/users">User Admin</Link></li>}
            {!isAuthenticated && <li><Link to="/login">Login</Link></li>}
            {isAuthenticated && (
              <>
                <li className="nav-user">Signed in as <strong>{session?.user?.username}</strong></li>
                <li>
                  <button type="button" className="link-button" onClick={logout}>
                    Log out
                  </button>
                </li>
              </>
            )}
          </ul>
        </nav>
      </header>
      <main className="app-content">
        <Outlet />
      </main>
    </div>
  );
}

export default DashboardLayout;
