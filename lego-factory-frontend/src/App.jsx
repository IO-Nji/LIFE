import { Navigate, Route, Routes } from "react-router-dom";
import HomePage from "./pages/HomePage.jsx";
import DashboardPage from "./pages/DashboardPage.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import UserManagementPage from "./pages/UserManagementPage.jsx";
import ProductsPage from "./pages/ProductsPage.jsx";
import PlantWarehousePage from "./pages/PlantWarehousePage.jsx";
import ModulesSupermarketPage from "./pages/ModulesSupermarketPage.jsx";
import DashboardLayout from "./layouts/DashboardLayout.jsx";
import { useAuth } from "./context/AuthContext.jsx";

function App() {
  const { isAuthenticated, isAdmin, isPlantWarehouse, session } = useAuth();

  const isModulesSupermarket = session?.user?.role === "MODULES_SUPERMARKET";

  return (
    <Routes>
      <Route element={<DashboardLayout />}>
        <Route index element={<HomePage />} />
        <Route
          path="dashboard"
          element={isAuthenticated ? <DashboardPage /> : <Navigate to="/login" replace />}
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
      </Route>
      <Route path="/login" element={<LoginPage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
