import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import "./AdminSidebar.css";

const NAV_ITEMS = [
  { to: "/admin/dashboard",    icon: "📊", label: "Dashboard"    },
  { to: "/admin/analytics",    icon: "📈", label: "Analytics"    },
  { to: "/admin/transactions", icon: "💳", label: "Transactions" },
  { to: "/admin/audit-logs",   icon: "📋", label: "Audit Logs"   },
];

export default function AdminSidebar() {
  const { logout } = useAuth();
  const navigate   = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  return (
    <aside className="as-sidebar">
      <div>
        {/* Logo */}
        <div className="as-logo">
          <span className="as-logo__icon">💳</span>
          <span className="as-logo__name">KoshPay</span>
          <span className="as-logo__badge">Admin Panel</span>
        </div>

        {/* Nav */}
        <nav className="as-nav">
          {NAV_ITEMS.map(({ to, icon, label }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `as-nav__item${isActive ? " as-nav__item--active" : ""}`
              }
            >
              <span className="as-nav__icon">{icon}</span>
              {label}
            </NavLink>
          ))}
        </nav>
      </div>

      <button onClick={handleLogout} className="as-logout">
        ⎋ Logout
      </button>
    </aside>
  );
}