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

// Admin Pages
import AdminDashboard from "./pages/admin/AdminDashboard";
import AdminTransactions from "./pages/admin/AdminTransactions";
import AuditLogs from "./pages/admin/AuditLogs";
import Analytics from "./pages/admin/Analytics";

import Navbar from "./components/Navbar";
import Security from "./pages/Security";
import AdminLayout from "./layouts/AdminLayout";

// ── Toast (mount once here, use toast() anywhere) ─────────────
import ToastContainer from "./components/Toast";

export default function App() {
  const { isAuthenticated, isAdmin, loading } = useAuth();

  if (loading) {
    return (
      <div style={{
        height: "100vh",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        fontSize: "1.2rem",
      }}>
        Loading...
      </div>
    );
  }

  return (
    <>
      {/* Global toast notifications */}
      <ToastContainer />

      {/* Show Navbar only for normal users */}
      {isAuthenticated && !isAdmin && <Navbar />}

      <Routes>
        {/* ── PUBLIC ─────────────────────────────────────────── */}
        <Route
          path="/login"
          element={
            isAuthenticated
              ? <Navigate to={isAdmin ? "/admin/dashboard" : "/dashboard"} />
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
        <Route element={<AdminRoute />}>
          <Route element={<AdminLayout />}>
            <Route path="/admin/dashboard"    element={<AdminDashboard />} />
            <Route path="/admin/transactions" element={<AdminTransactions />} />
            <Route path="/admin/audit-logs"   element={<AuditLogs />} />
            <Route path="/admin/analytics"    element={<Analytics />} />
          </Route>
        </Route>

        {/* ── DEFAULT / CATCH-ALL ─────────────────────────────── */}
        <Route
          path="/"
          element={
            isAuthenticated
              ? <Navigate to={isAdmin ? "/admin/dashboard" : "/dashboard"} />
              : <Navigate to="/login" />
          }
        />
        <Route
          path="*"
          element={
            <Navigate to={isAuthenticated ? (isAdmin ? "/admin/dashboard" : "/dashboard") : "/login"} />
          }
        />
      </Routes>
    </>
  );
}