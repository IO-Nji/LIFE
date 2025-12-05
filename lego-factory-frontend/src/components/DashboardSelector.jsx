import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

/**
 * DashboardSelector - Intelligently routes authenticated users to their primary dashboard
 * based on their role. Handles cases where users have multiple roles by prioritizing.
 * 
 * Fallback priority:
 * 1. Admin Dashboard (highest priority)
 * 2. Role-specific dashboard (warehouse, supermarket, etc.)
 * 3. Generic Dashboard (lowest priority)
 */
function DashboardSelector() {
  const { isAuthenticated, session } = useAuth();

  // If not authenticated, redirect to login
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  const userRole = session?.user?.role;
  const workstationName = session?.user?.workstationName || "";

  // Helper functions for workstation type detection
  const getManufacturingWorkstationType = () => {
    if (workstationName.includes("Injection")) return "injection-molding";
    if (workstationName.includes("Pre-Production")) return "parts-pre-production";
    if (workstationName.includes("Finishing")) return "part-finishing";
    return "injection-molding";
  };

  const getAssemblyWorkstationType = () => {
    if (workstationName.includes("Gear")) return "gear-assembly";
    if (workstationName.includes("Motor")) return "motor-assembly";
    if (workstationName.includes("Final")) return "final-assembly";
    return "gear-assembly";
  };

  // Route based on role (priority-ordered)
  switch (userRole) {
    case "ADMIN":
      return <Navigate to="/admin-dashboard" replace />;
    case "PLANT_WAREHOUSE":
      return <Navigate to="/warehouse" replace />;
    case "MODULES_SUPERMARKET":
      return <Navigate to="/modules-supermarket" replace />;
    case "PRODUCTION_PLANNING":
      return <Navigate to="/production-planning" replace />;
    case "PRODUCTION_CONTROL":
      return <Navigate to="/production-control" replace />;
    case "ASSEMBLY_CONTROL":
      return <Navigate to="/assembly-control" replace />;
    case "MANUFACTURING_WORKSTATION":
      return <Navigate to={`/manufacturing/${getManufacturingWorkstationType()}`} replace />;
    case "ASSEMBLY_WORKSTATION":
      return <Navigate to={`/assembly/${getAssemblyWorkstationType()}`} replace />;
    case "PARTS_SUPPLY_WAREHOUSE":
      return <Navigate to="/parts-supply-warehouse" replace />;
    default:
      // Unknown role, go to generic dashboard
      return <Navigate to="/dashboard" replace />;
  }
}

export default DashboardSelector;
