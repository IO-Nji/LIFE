import { Link, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

function DashboardLayout() {
  const { isAuthenticated, isAdmin, isPlantWarehouse, logout, session } = useAuth();

  const isModulesSupermarket = session?.user?.role === "MODULES_SUPERMARKET";
  const isProductionControl = session?.user?.role === "PRODUCTION_CONTROL";
  const isAssemblyControl = session?.user?.role === "ASSEMBLY_CONTROL";

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="header-top">
          <h1>LEGO Factory Control</h1>
          {isAuthenticated && (
            <div className="user-info">Signed in as <strong>{session?.user?.username}</strong></div>
          )}
        </div>
        <nav>
          <ul className="nav-list">
            <li><Link to="/">Home</Link></li>
            <li><Link to="/dashboard">Dashboard</Link></li>
            {isAuthenticated && <li><Link to="/products">Products</Link></li>}
            {isPlantWarehouse && <li><Link to="/warehouse">Warehouse</Link></li>}
            {isModulesSupermarket && <li><Link to="/modules-supermarket">Modules Supermarket</Link></li>}
            {isProductionControl && <li><Link to="/production-control">🏭 Production Control</Link></li>}
            {isAssemblyControl && <li><Link to="/assembly-control">⚙️ Assembly Control</Link></li>}
            {isAdmin && <li><Link to="/users">User Admin</Link></li>}
            {!isAuthenticated && <li><Link to="/login">Login</Link></li>}
            {isAuthenticated && (
              <li>
                <button type="button" className="link-button" onClick={logout}>
                  Log out
                </button>
              </li>
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