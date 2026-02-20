import { Outlet } from "react-router-dom";
import AdminSidebar from "../components/admin/AdminSidebar";
import { useAuth } from "../auth/AuthContext";
import "./AdminLayout.css";

export default function AdminLayout() {
  const { logout } = useAuth();

  return (
    <div className="al-wrapper">
      <AdminSidebar />

      <div className="al-main">
        {/* Topbar */}
        <div className="al-topbar">
          <div className="al-topbar__left">
            <span className="al-topbar__title">KoshPay Admin Panel</span>
            <span className="al-topbar__subtitle">
              <span className="al-topbar__dot" />
              System operational
            </span>
          </div>

          <div className="al-topbar__right">
            <button onClick={logout} className="al-topbar__logout">
              ⎋ Logout
            </button>
          </div>
        </div>

        {/* Page content */}
        <div className="al-content">
          <Outlet />
        </div>
      </div>
    </div>
  );
}