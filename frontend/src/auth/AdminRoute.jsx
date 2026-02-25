import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "./AuthContext";

/**
 * AdminRoute — protects admin pages by role.
 *
 * Usage in App.jsx:
 *   <Route element={<AdminRoute allowedRoles={["ROLE_SUPER_ADMIN", "ROLE_ANALYTICS"]} />}>
 *     <Route path="/admin/analytics" element={<Analytics />} />
 *   </Route>
 *
 * If allowedRoles is omitted, any admin role is permitted.
 */
export default function AdminRoute({ allowedRoles }) {
  const { isAuthenticated, isAdmin, adminRole, loading } = useAuth();

  if (loading) return <p>Loading...</p>;

  // Not logged in at all
  if (!isAuthenticated) return <Navigate to="/login" replace />;

  // Logged in but not an admin
  if (!isAdmin) return <Navigate to="/dashboard" replace />;

  // If specific roles are required, check them
  if (allowedRoles && allowedRoles.length > 0) {
    if (!allowedRoles.includes(adminRole)) {
      // Admin is logged in but doesn't have access to this tab
      return <Navigate to="/admin/unauthorized" replace />;
    }
  }

  return <Outlet />;
}