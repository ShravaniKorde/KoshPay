import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import "./Navbar.css";

export default function Navbar() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const navStyle = ({ isActive }) =>
    isActive ? "kp-nav__link kp-nav__link--active" : "kp-nav__link";

  return (
    <header className="kp-header">
      <div className="kp-header__inner">

        {/* Logo */}
        <div className="kp-logo">
          <span className="kp-logo__icon">💳</span>
          <span className="kp-logo__text">KoshPay</span>
        </div>

        {/* Nav links */}
        <nav className="kp-nav">
          <NavLink to="/dashboard"          className={navStyle}>Dashboard</NavLink>
          <NavLink to="/transfer"           className={navStyle}>Transfer</NavLink>
          <NavLink to="/transactions"       className={navStyle}>Transactions</NavLink>
          <NavLink to="/contacts"           className={navStyle}>Contacts</NavLink>
          <NavLink to="/my-qr"              className={navStyle}>My QR</NavLink>
          <NavLink to="/scan-qr"            className={navStyle}>Scan QR</NavLink>
          <NavLink to="/scheduled-payments" className={navStyle}>Schedule</NavLink>
          <NavLink to="/security"           className={navStyle}>Security</NavLink>
        </nav>

        {/* Logout */}
        <button onClick={handleLogout} className="kp-logout-btn">
          Logout
        </button>

      </div>
    </header>
  );
}