import { Outlet } from "react-router-dom";
import AdminSidebar from "../components/admin/AdminSidebar";
import { useAuth } from "../auth/AuthContext";

export default function AdminLayout() {
  const { logout } = useAuth();

  return (
    <div style={styles.wrapper}>
      <AdminSidebar />

      <div style={styles.main}>
        <div style={styles.topbar}>
          <h2 style={styles.title}>KoshPay Admin Panel</h2>
          <button onClick={logout} style={styles.logoutBtn}>
            Logout
          </button>
        </div>

        <div style={styles.content}>
          <Outlet />
        </div>
      </div>
    </div>
  );
}

/* ================= STYLES ================= */

const styles = {
  wrapper: {
    display: "flex",
    minHeight: "100vh",
    backgroundColor: "#0f172a",
    color: "#fff",
  },
  main: {
    flex: 1,
    display: "flex",
    flexDirection: "column",
  },
  topbar: {
    height: "70px",
    backgroundColor: "#111827",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    padding: "0 2rem",
    borderBottom: "1px solid #1f2937",
  },
  title: {
    fontSize: "1.2rem",
    fontWeight: "600",
  },
  logoutBtn: {
    backgroundColor: "#ef4444",
    border: "none",
    padding: "0.5rem 1rem",
    borderRadius: "8px",
    color: "#fff",
    cursor: "pointer",
  },
  content: {
    padding: "2rem",
    flex: 1,
  },
};
