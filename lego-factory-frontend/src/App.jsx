import { Navigate, Route, Routes } from "react-router-dom";
import HomePage from "./pages/HomePage.jsx";
import DashboardPage from "./pages/DashboardPage.jsx";
import AdminDashboard from "./pages/AdminDashboard.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import UserManagementPage from "./pages/UserManagementPage.jsx";
import WarehouseManagementPage from "./pages/WarehouseManagementPage.jsx";
import InventoryManagementPage from "./pages/InventoryManagementPage.jsx";
import ProductsPage from "./pages/ProductsPage.jsx";
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
              <AdminDashboard />
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
          path="warehouses"
          element={
            isAdmin ? (
              <WarehouseManagementPage />
            ) : (
              <Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />
            )
          }
        />
        <Route
          path="inventory"
          element={
            isAdmin ? (
              <InventoryManagementPage />
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
              <DashboardPage />
            ) : (
              <Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />
            )
          }
        />
        <Route
          path="modules-supermarket"
          element={
            isModulesSupermarket ? (
              <DashboardPage />
            ) : (
              <Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />
            )
          }
        />
        <Route
          path="production-planning"
          element={
            isProductionPlanning ? (
              <DashboardPage />
            ) : (
              <Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />
            )
          }
        />
        <Route
          path="production-control"
          element={
            isProductionControl ? (
              <DashboardPage />
            ) : (
              <Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />
            )
          }
        />
        <Route
          path="assembly-control"
          element={
            isAssemblyControl ? (
              <DashboardPage />
            ) : (
              <Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />
            )
          }
        />
        <Route
          path="parts-supply-warehouse"
          element={
            isPartsSupplyWarehouse ? (
              <DashboardPage />
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
