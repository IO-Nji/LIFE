import { Navigate, Route, Routes } from "react-router-dom";
import HomePage from "./pages/HomePage.jsx";
import DashboardPage from "./pages/DashboardPage.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import UserManagementPage from "./pages/UserManagementPage.jsx";
import AdminDashboardPage from "./pages/AdminDashboardPage.jsx";
import ProductsPage from "./pages/ProductsPage.jsx";
import PlantWarehousePage from "./pages/PlantWarehousePage.jsx";
import ModulesSupermarketPage from "./pages/ModulesSupermarketPage.jsx";
import ProductionPlanningPage from "./pages/ProductionPlanningPage.jsx";
import ProductionControlPage from "./pages/ProductionControlPage.jsx";
import AssemblyControlPage from "./pages/AssemblyControlPage.jsx";
import PartsSupplyWarehousePage from "./pages/PartsSupplyWarehousePage.jsx";
import ManufacturingWorkstationPage from "./pages/ManufacturingWorkstationPage.jsx";
import DashboardLayout from "./layouts/DashboardLayout.jsx";
import { useAuth } from "./context/AuthContext.jsx";

function App() {
  const { isAuthenticated, isAdmin, isPlantWarehouse, session } = useAuth();

  // Define all role checks for consistent guard logic
  const isModulesSupermarket = session?.user?.role === "MODULES_SUPERMARKET";
  const isProductionPlanning = session?.user?.role === "PRODUCTION_PLANNING";
  const isProductionControl = session?.user?.role === "PRODUCTION_CONTROL";
  const isAssemblyControl = session?.user?.role === "ASSEMBLY_CONTROL";
  const isPartsSupplyWarehouse = session?.user?.role === "PARTS_SUPPLY_WAREHOUSE";
  const isManufacturingWorkstation = session?.user?.role === "MANUFACTURING_WORKSTATION";
  const isAssemblyWorkstation = session?.user?.role === "ASSEMBLY_WORKSTATION";

  return (
    <Routes>
      <Route element={<DashboardLayout />}>
        <Route index element={<HomePage />} />
        <Route
          path="dashboard"
          element={isAuthenticated ? <DashboardPage /> : <Navigate to="/login" replace />}
        />
        <Route
          path="admin-dashboard"
          element={
            isAdmin ? (
              <AdminDashboardPage />
            ) : (
              <Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />
            )
          }
        />
        <Route
          path="users"
          element={
            isAdmin ? (
              <UserManagementPage />
            ) : (
              <Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />
            )
          }
        />
        <Route
          path="products"
          element={isAuthenticated ? <ProductsPage /> : <Navigate to="/login" replace />}
        />
        <Route
          path="warehouse"
          element={
            isPlantWarehouse ? (
              <PlantWarehousePage />
            ) : (
              <Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />
            )
          }
        />
        <Route
          path="modules-supermarket"
          element={
            isModulesSupermarket ? (
              <ModulesSupermarketPage />
            ) : (
              <Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />
            )
          }
        />
        <Route
          path="production-planning"
          element={
            isProductionPlanning ? (
              <ProductionPlanningPage />
            ) : (
              <Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />
            )
          }
        />
        <Route
          path="production-control"
          element={
            isProductionControl ? (
              <ProductionControlPage />
            ) : (
              <Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />
            )
          }
        />
        <Route
          path="assembly-control"
          element={
            isAssemblyControl ? (
              <AssemblyControlPage />
            ) : (
              <Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />
            )
          }
        />
        <Route
          path="parts-supply-warehouse"
          element={
            isPartsSupplyWarehouse ? (
              <PartsSupplyWarehousePage />
            ) : (
              <Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />
            )
          }
        />
        <Route
          path="manufacturing/:workstationType"
          element={
            isManufacturingWorkstation ? (
              <ManufacturingWorkstationPage />
            ) : (
              <Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />
            )
          }
        />
        <Route
          path="assembly/:workstationType"
          element={
            isAssemblyWorkstation ? (
              <ManufacturingWorkstationPage />
            ) : (
              <Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />
            )
          }
        />
      </Route>
      <Route path="/login" element={<LoginPage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
