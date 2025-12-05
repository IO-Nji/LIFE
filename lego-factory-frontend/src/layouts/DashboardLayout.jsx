import { Link, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

function DashboardLayout() {
  const { isAuthenticated, isAdmin, isPlantWarehouse, logout, session } = useAuth();

  // Define all role checks
  const isModulesSupermarket = session?.user?.role === "MODULES_SUPERMARKET";
  const isProductionControl = session?.user?.role === "PRODUCTION_CONTROL";
  const isAssemblyControl = session?.user?.role === "ASSEMBLY_CONTROL";
  const isPartsSupplyWarehouse = session?.user?.role === "PARTS_SUPPLY_WAREHOUSE";
  const isManufacturingWorkstation = session?.user?.role === "MANUFACTURING_WORKSTATION";
  const isAssemblyWorkstation = session?.user?.role === "ASSEMBLY_WORKSTATION";
  const isProductionPlanning = session?.user?.role === "PRODUCTION_PLANNING";

  // Determine which manufacturing station to display based on workstation type
  const getManufacturingWorkstationType = () => {
    const workstationName = session?.user?.workstationName || "";
    if (workstationName.includes("Injection")) return "injection-molding";
    if (workstationName.includes("Pre-Production")) return "parts-pre-production";
    if (workstationName.includes("Finishing")) return "part-finishing";
    return "injection-molding"; // default
  };

  // Determine which assembly station to display based on workstation type
  const getAssemblyWorkstationType = () => {
    const workstationName = session?.user?.workstationName || "";
    if (workstationName.includes("Gear")) return "gear-assembly";
    if (workstationName.includes("Motor")) return "motor-assembly";
    if (workstationName.includes("Final")) return "final-assembly";
    return "gear-assembly"; // default
  };

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="header-top">
          <h1>LEGO Factory Control</h1>
          {isAuthenticated && (
            <div className="user-info">
              Signed in as <strong>{session?.user?.username}</strong>
              {session?.user?.role && <span className="role-badge">{session.user.role.replace(/_/g, " ")}</span>}
            </div>
          )}
        </div>
        <nav>
          <ul className="nav-list">
            {/* Public Navigation */}
            <li><Link to="/">Home</Link></li>
            
            {/* Authenticated User Navigation */}
            {isAuthenticated && <li><Link to="/dashboard">Dashboard</Link></li>}
            {isAuthenticated && <li><Link to="/products">📦 Products</Link></li>}

            {/* Plant Warehouse Navigation */}
            {isPlantWarehouse && <li><Link to="/warehouse">🏭 Plant Warehouse</Link></li>}

            {/* Modules Supermarket Navigation */}
            {isModulesSupermarket && <li><Link to="/modules-supermarket">🏢 Modules Supermarket</Link></li>}

            {/* Production Planning Navigation */}
            {isProductionPlanning && <li><Link to="/production-planning">📋 Production Planning</Link></li>}

            {/* Production Control Navigation */}
            {isProductionControl && <li><Link to="/production-control">🏭 Production Control</Link></li>}

            {/* Assembly Control Navigation */}
            {isAssemblyControl && <li><Link to="/assembly-control">⚙️ Assembly Control</Link></li>}

            {/* Manufacturing Workstation Navigation */}
            {isManufacturingWorkstation && (
              <li><Link to={`/manufacturing/${getManufacturingWorkstationType()}`}>🔧 Manufacturing Station</Link></li>
            )}

            {/* Assembly Workstation Navigation */}
            {isAssemblyWorkstation && (
              <li><Link to={`/assembly/${getAssemblyWorkstationType()}`}>🔩 Assembly Station</Link></li>
            )}

            {/* Parts Supply Warehouse Navigation */}
            {isPartsSupplyWarehouse && <li><Link to="/parts-supply-warehouse">📦 Parts Supply Warehouse</Link></li>}

            {/* Admin Navigation */}
            {isAdmin && <li><Link to="/admin-dashboard">📊 Admin Dashboard</Link></li>}
            {isAdmin && <li><Link to="/users">👥 User Management</Link></li>}

            {/* Authentication */}
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