import { createContext, useContext, useEffect, useRef, useState } from "react";
import { jwtDecode } from "jwt-decode";
import { toast } from "../components/Toast";

const AuthContext = createContext(null);

// ── Roles that count as "admin" (any of these = admin user) ──
const ADMIN_ROLES = [
  "ROLE_SUPER_ADMIN",
  "ROLE_ANALYTICS",
  "ROLE_TRANSACTIONS",
  "ROLE_AUDIT_LOGS",
];

function isTokenExpired(token) {
  try {
    const decoded = jwtDecode(token);
    return decoded.exp * 1000 < Date.now();
  } catch {
    return true;
  }
}

function getExpiry(token) {
  try {
    return jwtDecode(token).exp * 1000;
  } catch {
    return null;
  }
}

const WARN_BEFORE_MS = 5 * 60 * 1000; // 5 minutes

export const AuthProvider = ({ children }) => {
  const [token, setToken]         = useState(null);
  const [isAdmin, setIsAdmin]     = useState(false);
  const [adminRole, setAdminRole] = useState(null); // e.g. "ROLE_ANALYTICS"
  const [loading, setLoading]     = useState(true);

  const warnTimerRef   = useRef(null);
  const logoutTimerRef = useRef(null);

  const clearTimers = () => {
    if (warnTimerRef.current)   clearTimeout(warnTimerRef.current);
    if (logoutTimerRef.current) clearTimeout(logoutTimerRef.current);
  };

  const scheduleSessionTimers = (jwtToken) => {
    clearTimers();
    const expiry = getExpiry(jwtToken);
    if (!expiry) return;

    const now        = Date.now();
    const msToExpiry = expiry - now;
    const msToWarn   = msToExpiry - WARN_BEFORE_MS;

    if (msToWarn > 0) {
      warnTimerRef.current = setTimeout(() => {
        toast.info("⚠️ Your session expires in 5 minutes. Save your work.");
      }, msToWarn);
    } else if (msToExpiry > 0) {
      const minsLeft = Math.max(1, Math.round(msToExpiry / 60000));
      toast.info(`⚠️ Session expires in ~${minsLeft} minute${minsLeft > 1 ? "s" : ""}.`);
    }

    if (msToExpiry > 0) {
      logoutTimerRef.current = setTimeout(() => {
        toast.error("Session expired. Please log in again.");
        doLogout();
      }, msToExpiry);
    }
  };

  const doLogout = () => {
    clearTimers();
    localStorage.removeItem("token");
    setToken(null);
    setIsAdmin(false);
    setAdminRole(null);
  };

  // ── Helper: decode and apply role state from a token ─────
  const applyToken = (jwtToken) => {
    try {
      const decoded = jwtDecode(jwtToken);
      const role    = decoded.role ?? null;
      const admin   = ADMIN_ROLES.includes(role);
      setIsAdmin(admin);
      setAdminRole(admin ? role : null);
    } catch {
      setIsAdmin(false);
      setAdminRole(null);
    }
  };

  // ── On mount: validate stored token ──────────────────────
  useEffect(() => {
    const storedToken = localStorage.getItem("token");

    if (storedToken) {
      if (isTokenExpired(storedToken)) {
        localStorage.removeItem("token");
      } else {
        setToken(storedToken);
        applyToken(storedToken);
        scheduleSessionTimers(storedToken);
      }
    }

    setLoading(false);
    return () => clearTimers();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const login = (jwtToken) => {
    localStorage.setItem("token", jwtToken);
    setToken(jwtToken);
    applyToken(jwtToken);
    scheduleSessionTimers(jwtToken);
  };

  const logout = () => doLogout();

  return (
    <AuthContext.Provider
      value={{
        token,
        isAuthenticated: !!token,
        isAdmin,
        adminRole,   // ← e.g. "ROLE_ANALYTICS" — use this in sidebar/routes
        loading,
        login,
        logout,
      }}
    >
      {!loading && children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);