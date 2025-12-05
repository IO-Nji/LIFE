import { Link, Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import "../styles/HomePage.css";

function HomePage() {
  const { isAuthenticated, session } = useAuth();

  // Define all role checks
  const isAdmin = session?.user?.role === "ADMIN";
  const isPlantWarehouse = session?.user?.role === "PLANT_WAREHOUSE";
  const isModulesSupermarket = session?.user?.role === "MODULES_SUPERMARKET";
  const isProductionPlanning = session?.user?.role === "PRODUCTION_PLANNING";
  const isProductionControl = session?.user?.role === "PRODUCTION_CONTROL";
  const isAssemblyControl = session?.user?.role === "ASSEMBLY_CONTROL";
  const isManufacturingWorkstation = session?.user?.role === "MANUFACTURING_WORKSTATION";
  const isAssemblyWorkstation = session?.user?.role === "ASSEMBLY_WORKSTATION";
  const isPartsSupplyWarehouse = session?.user?.role === "PARTS_SUPPLY_WAREHOUSE";

  // Helper to get manufacturing workstation type
  const getManufacturingWorkstationType = () => {
    const workstationName = session?.user?.workstationName || "";
    if (workstationName.includes("Injection")) return "injection-molding";
    if (workstationName.includes("Pre-Production")) return "parts-pre-production";
    if (workstationName.includes("Finishing")) return "part-finishing";
    return "injection-molding";
  };

  // Helper to get assembly workstation type
  const getAssemblyWorkstationType = () => {
    const workstationName = session?.user?.workstationName || "";
    if (workstationName.includes("Gear")) return "gear-assembly";
    if (workstationName.includes("Motor")) return "motor-assembly";
    if (workstationName.includes("Final")) return "final-assembly";
    return "gear-assembly";
  };

  // Auto-redirect authenticated users to their primary dashboard
  if (isAuthenticated) {
    if (isAdmin) return <Navigate to="/admin-dashboard" replace />;
    if (isPlantWarehouse) return <Navigate to="/warehouse" replace />;
    if (isModulesSupermarket) return <Navigate to="/modules-supermarket" replace />;
    if (isProductionPlanning) return <Navigate to="/production-planning" replace />;
    if (isProductionControl) return <Navigate to="/production-control" replace />;
    if (isAssemblyControl) return <Navigate to="/assembly-control" replace />;
    if (isManufacturingWorkstation) return <Navigate to={`/manufacturing/${getManufacturingWorkstationType()}`} replace />;
    if (isAssemblyWorkstation) return <Navigate to={`/assembly/${getAssemblyWorkstationType()}`} replace />;
    if (isPartsSupplyWarehouse) return <Navigate to="/parts-supply-warehouse" replace />;
    // Default authenticated user goes to dashboard
    return <Navigate to="/dashboard" replace />;
  }

  // Unauthenticated home page
  return (
    <section className="home-page">
      <div className="home-hero">
        <h2>Welcome to the LEGO Factory Control System</h2>
        <p className="subtitle">
          A prototype system for coordinating manufacturing workflows across multiple factory stations.
        </p>
      </div>

      <div className="home-content">
        <div className="feature-cards">
          <div className="card">
            <h3>🏭 Plant Warehouse</h3>
            <p>Manage customer orders and inventory at the plant warehouse distribution center.</p>
          </div>
          <div className="card">
            <h3>🏢 Modules Supermarket</h3>
            <p>Organize module assembly and fulfill warehouse orders with pre-assembled modules.</p>
          </div>
          <div className="card">
            <h3>📋 Production Planning</h3>
            <p>Plan and schedule production workflows across manufacturing and assembly stations.</p>
          </div>
          <div className="card">
            <h3>🏭 Production Control</h3>
            <p>Monitor manufacturing workstations and track production progress in real-time.</p>
          </div>
          <div className="card">
            <h3>⚙️ Assembly Control</h3>
            <p>Supervise assembly workstations including gear, motor, and final assembly stations.</p>
          </div>
          <div className="card">
            <h3>📦 Parts Supply Warehouse</h3>
            <p>Supply parts to manufacturing and assembly stations on demand.</p>
          </div>
          <div className="card">
            <h3>🔧 Manufacturing Stations</h3>
            <p>Execute manufacturing tasks at injection molding, pre-production, and finishing stations.</p>
          </div>
          <div className="card">
            <h3>🔩 Assembly Stations</h3>
            <p>Execute assembly tasks for gear, motor, and final product assembly.</p>
          </div>
        </div>

        <div className="home-actions">
          <p className="lead-text">
            Ready to access the system?
          </p>
          <Link className="primary-link" to="/login">
            Sign In to Continue
          </Link>
          <p className="info-text">
            Contact your administrator if you need account access or role assignment.
          </p>
        </div>
      </div>
    </section>
  );
}

export default HomePage;
