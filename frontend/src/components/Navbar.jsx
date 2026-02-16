import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function Navbar() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <header style={styles.header}>
      <div style={styles.container}>
        {/* LOGO */}
        <div style={styles.logo}>
          💳 <span>KoshPay</span>
        </div>

        {/* MENU */}
        <nav style={styles.menu}>
          <NavLink to="/dashboard" style={navStyle}>
            Dashboard
          </NavLink>

          <NavLink to="/transfer" style={navStyle}>
            Transfer
          </NavLink>

          <NavLink to="/transactions" style={navStyle}>
            Transactions
          </NavLink>

          <NavLink to="/contacts" style={navStyle}>
            Contacts
          </NavLink>

          <NavLink to="/my-qr" style={navStyle}>
            My QR
          </NavLink>

          <NavLink to="/scan-qr" style={navStyle}>
            Scan QR
          </NavLink>

          <NavLink
            to="/scheduled-payments"
            style={({ isActive }) =>
              isActive ? styles.activeLink : styles.link
            }
          >
            Schedule
          </NavLink>

          <NavLink to="/security" style={navStyle}>
            Security
          </NavLink>
        </nav>

        {/* LOGOUT */}
        <button onClick={handleLogout} style={styles.logoutBtn}>
          Logout
        </button>
      </div>
    </header>
  );
}

const navStyle = ({ isActive }) =>
  isActive ? styles.activeLink : styles.link;

/* ===================== STYLES ===================== */

const styles = {
  header: {
    background: "linear-gradient(90deg, #2563eb, #1e40af)",
    boxShadow: "0 6px 20px rgba(0,0,0,0.18)",
    position: "sticky",
    top: 0,
    zIndex: 100,
  },

  container: {
    maxWidth: "1200px",
    margin: "0 auto",
    padding: "1rem 1.75rem",
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    color: "#fff",
  },

  logo: {
    fontSize: "1.45rem",
    fontWeight: "700",
    display: "flex",
    alignItems: "center",
    gap: "0.5rem",
    letterSpacing: "0.5px",
  },

  menu: {
    background: "rgba(255,255,255,0.15)",
    borderRadius: "999px",
    padding: "0.45rem",
    display: "flex",
    gap: "0.4rem",
  },

  link: {
    color: "#e0e7ff",
    textDecoration: "none",
    padding: "0.45rem 1.15rem",
    borderRadius: "999px",
    fontSize: "0.9rem",
    fontWeight: "500",
    transition: "all 0.25s ease",
  },

  activeLink: {
    background: "#ffffff",
    color: "#1e3a8a",
    textDecoration: "none",
    padding: "0.45rem 1.15rem",
    borderRadius: "999px",
    fontSize: "0.9rem",
    fontWeight: "600",
  },

  logoutBtn: {
    background: "#ef4444",
    color: "#fff",
    border: "none",
    padding: "0.5rem 1.1rem",
    borderRadius: "10px",
    fontSize: "0.85rem",
    fontWeight: "600",
    cursor: "pointer",
    transition: "opacity 0.2s ease",
  },
};
