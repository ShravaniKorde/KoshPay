import { Routes, Route, Navigate } from "react-router-dom";
import { useAuth } from "./auth/AuthContext";
import PrivateRoute from "./auth/PrivateRoute";
import AdminRoute from "./auth/AdminRoute";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import Transfer from "./pages/Transfer";
import Transactions from "./pages/Transactions";
import Contacts from "./pages/Contacts";
import MyQR from "./pages/MyQR";
import ScanQR from "./pages/ScanQR";
import ScheduledPayments from "./pages/ScheduledPayments";
import Security from "./pages/Security";

// Admin Pages
import AdminDashboard from "./pages/admin/AdminDashboard";
import AdminTransactions from "./pages/admin/AdminTransactions";
import AuditLogs from "./pages/admin/AuditLogs";
import Analytics from "./pages/admin/Analytics";

import Navbar from "./components/Navbar";
import AdminLayout from "./layouts/AdminLayout";
import ToastContainer from "./components/Toast";

// ── Maps each admin role to their correct landing page ────────
function getAdminHome(adminRole) {
  switch (adminRole) {
    case "ROLE_SUPER_ADMIN":  return "/admin/dashboard";
    case "ROLE_ANALYTICS":    return "/admin/analytics";
    case "ROLE_TRANSACTIONS": return "/admin/transactions";
    case "ROLE_AUDIT_LOGS":   return "/admin/audit-logs";
    default:                  return "/admin/dashboard";
  }
}

// ── Inline Unauthorized page ──────────────────────────────────
function AdminUnauthorized() {
  return (
    <div style={{
      display: "flex", flexDirection: "column",
      alignItems: "center", justifyContent: "center",
      height: "100%", gap: "1rem", color: "#ef4444",
    }}>
      <h2>🚫 Access Denied</h2>
      <p>You don't have permission to view this page.</p>
    </div>
  );
}

export default function App() {
  const { isAuthenticated, isAdmin, adminRole, loading } = useAuth();

  if (loading) {
    return (
      <div style={{
        height: "100vh", display: "flex",
        justifyContent: "center", alignItems: "center",
        fontSize: "1.2rem",
      }}>
        Loading...
      </div>
    );
  }

  // Correct landing page for whoever is logged in
  const adminHome = getAdminHome(adminRole);

  return (
    <>
      <ToastContainer />

      {/* Navbar only for regular users */}
      {isAuthenticated && !isAdmin && <Navbar />}

      <Routes>
        {/* ── PUBLIC ─────────────────────────────────────────── */}
        <Route
          path="/login"
          element={
            isAuthenticated
              ? <Navigate to={isAdmin ? adminHome : "/dashboard"} />
              : <Login />
          }
        />
        <Route
          path="/register"
          element={isAuthenticated ? <Navigate to="/dashboard" /> : <Register />}
        />

        {/* ── USER ───────────────────────────────────────────── */}
        <Route element={<PrivateRoute />}>
          <Route path="/dashboard"          element={<Dashboard />} />
          <Route path="/transfer"           element={<Transfer />} />
          <Route path="/transactions"       element={<Transactions />} />
          <Route path="/contacts"           element={<Contacts />} />
          <Route path="/security"           element={<Security />} />
          <Route path="/my-qr"              element={<MyQR />} />
          <Route path="/scan-qr"            element={<ScanQR />} />
          <Route path="/scheduled-payments" element={<ScheduledPayments />} />
        </Route>

        {/* ── ADMIN ──────────────────────────────────────────── */}
        <Route element={<AdminLayout />}>

          {/* Dashboard — super admin only */}
          <Route element={<AdminRoute allowedRoles={["ROLE_SUPER_ADMIN"]} />}>
            <Route path="/admin/dashboard" element={<AdminDashboard />} />
          </Route>

          {/* Analytics — super admin + analytics admin */}
          <Route element={<AdminRoute allowedRoles={["ROLE_SUPER_ADMIN", "ROLE_ANALYTICS"]} />}>
            <Route path="/admin/analytics" element={<Analytics />} />
          </Route>

          {/* Transactions — super admin + transactions admin */}
          <Route element={<AdminRoute allowedRoles={["ROLE_SUPER_ADMIN", "ROLE_TRANSACTIONS"]} />}>
            <Route path="/admin/transactions" element={<AdminTransactions />} />
          </Route>

          {/* Audit Logs — super admin + audit logs admin */}
          <Route element={<AdminRoute allowedRoles={["ROLE_SUPER_ADMIN", "ROLE_AUDIT_LOGS"]} />}>
            <Route path="/admin/audit-logs" element={<AuditLogs />} />
          </Route>

          {/* Unauthorized — when admin types a URL they can't access */}
          <Route path="/admin/unauthorized" element={<AdminUnauthorized />} />

        </Route>

        {/* ── DEFAULT / CATCH-ALL ─────────────────────────────── */}
        <Route
          path="/"
          element={
            isAuthenticated
              ? <Navigate to={isAdmin ? adminHome : "/dashboard"} />
              : <Navigate to="/login" />
          }
        />
        <Route
          path="*"
          element={
            <Navigate to={
              isAuthenticated
                ? (isAdmin ? adminHome : "/dashboard")
                : "/login"
            } />
          }
        />
      </Routes>
    </>
  );
}