import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import "./AdminSidebar.css";

// Each nav item declares which roles can see it.
// ROLE_SUPER_ADMIN always sees everything.
const NAV_ITEMS = [
  {
    to: "/admin/dashboard",
    icon: "📊",
    label: "Dashboard",
    roles: ["ROLE_SUPER_ADMIN"],           // only super admin has the overview
  },
  {
    to: "/admin/analytics",
    icon: "📈",
    label: "Analytics",
    roles: ["ROLE_SUPER_ADMIN", "ROLE_ANALYTICS"],
  },
  {
    to: "/admin/transactions",
    icon: "💳",
    label: "Transactions",
    roles: ["ROLE_SUPER_ADMIN", "ROLE_TRANSACTIONS"],
  },
  {
    to: "/admin/audit-logs",
    icon: "📋",
    label: "Audit Logs",
    roles: ["ROLE_SUPER_ADMIN", "ROLE_AUDIT_LOGS"],
  },
];

// Human-readable label for the role badge in the sidebar
const ROLE_LABELS = {
  ROLE_SUPER_ADMIN:  "Super Admin",
  ROLE_ANALYTICS:    "Analytics Admin",
  ROLE_TRANSACTIONS: "Transactions Admin",
  ROLE_AUDIT_LOGS:   "Audit Logs Admin",
};

export default function AdminSidebar() {
  const { logout, adminRole } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  // Only show nav items this role is allowed to see
  const visibleItems = NAV_ITEMS.filter(
    (item) => item.roles.includes(adminRole)
  );

  return (
    <aside className="as-sidebar">
      <div>
        {/* Logo */}
        <div className="as-logo">
          <span className="as-logo__icon">💳</span>
          <span className="as-logo__name">KoshPay</span>
          <span className="as-logo__badge">Admin Panel</span>
        </div>

        {/* Role badge */}
        <div className="as-role-badge">
          {ROLE_LABELS[adminRole] ?? "Admin"}
        </div>

        {/* Nav — only permitted tabs */}
        <nav className="as-nav">
          {visibleItems.map(({ to, icon, label }) => (
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