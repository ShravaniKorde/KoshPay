import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";

export default function AdminSidebar() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  return (
    <aside style={styles.sidebar}>
      <div>
        <h2 style={styles.logo}>KOSHPAY</h2>

        <nav style={styles.nav}>
          <NavLink
            to="/admin/dashboard"
            style={({ isActive }) =>
              isActive ? styles.activeLink : styles.link
            }
          >
            Dashboard
          </NavLink>

          <NavLink
            to="/admin/analytics"
            style={({ isActive }) =>
              isActive ? styles.activeLink : styles.link
            }
          >
            Analytics
          </NavLink>

          <NavLink
            to="/admin/transactions"
            style={({ isActive }) =>
              isActive ? styles.activeLink : styles.link
            }
          >
            Transactions
          </NavLink>

          <NavLink
            to="/admin/audit-logs"
            style={({ isActive }) =>
              isActive ? styles.activeLink : styles.link
            }
          >
            Audit Logs
          </NavLink>
        </nav>
      </div>

      <button onClick={handleLogout} style={styles.logoutBtn}>
        Logout
      </button>
    </aside>
  );
}

/* ================= STYLES ================= */

const styles = {
  sidebar: {
    width: "260px",
    minHeight: "100vh",
    background: "linear-gradient(180deg, #1e293b 0%, #0f172a 100%)",
    padding: "2rem 1.5rem",
    display: "flex",
    flexDirection: "column",
    justifyContent: "space-between",
    borderRight: "1px solid #334155",
  },

  logo: {
    color: "#ffffff",
    marginBottom: "2.5rem",
    textAlign: "center",
    fontWeight: "700",
    fontSize: "1.3rem",
    letterSpacing: "3px",
  },

  nav: {
    display: "flex",
    flexDirection: "column",
    gap: "1rem",
  },

  link: {
    textDecoration: "none",
    color: "#e2e8f0",
    padding: "0.8rem 1.2rem",
    borderRadius: "12px",
    backgroundColor: "#1e293b",
    transition: "all 0.3s ease",
    fontWeight: "500",
  },

  activeLink: {
    textDecoration: "none",
    padding: "0.8rem 1.2rem",
    borderRadius: "12px",
    background: "linear-gradient(90deg, #2563eb, #1d4ed8)",
    color: "#ffffff",
    fontWeight: "600",
    boxShadow: "0 0 12px rgba(37, 99, 235, 0.5)",
  },

  logoutBtn: {
    padding: "0.8rem",
    borderRadius: "12px",
    border: "none",
    background: "linear-gradient(90deg, #ef4444, #dc2626)",
    color: "#ffffff",
    fontWeight: "600",
    cursor: "pointer",
    transition: "all 0.3s ease",
  },
};
