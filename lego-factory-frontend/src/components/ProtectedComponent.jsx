import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

/**
 * ProtectedComponent - Renders a component only if the user has the required role(s)
 * 
 * @param {React.ReactNode} children - Component to render if authorized
 * @param {string|string[]} requiredRole - Single role or array of roles that grant access
 * @param {string} fallbackPath - Route to redirect to if not authorized (default: /dashboard)
 * @returns {React.ReactNode} - Either the protected component or a redirect
 */
export function ProtectedComponent({ children, requiredRole, fallbackPath = "/dashboard" }) {
  const { isAuthenticated, session } = useAuth();

  // If not authenticated, redirect to login
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // Check if user has required role
  const userRole = session?.user?.role;
  const hasAccess = Array.isArray(requiredRole)
    ? requiredRole.includes(userRole)
    : userRole === requiredRole;

  // If user doesn't have required role, redirect to fallback path
  if (!hasAccess) {
    return <Navigate to={fallbackPath} replace />;
  }

  // User is authenticated and authorized, render the component
  return children;
}

export default ProtectedComponent;
