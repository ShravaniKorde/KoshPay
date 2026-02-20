import { createContext, useContext, useEffect, useRef, useState } from "react";
import { jwtDecode } from "jwt-decode";
import { toast } from "../components/Toast";

const AuthContext = createContext(null);

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
    return jwtDecode(token).exp * 1000; // ms
  } catch {
    return null;
  }
}

// How many ms before expiry to warn the user
const WARN_BEFORE_MS = 5 * 60 * 1000; // 5 minutes

export const AuthProvider = ({ children }) => {
  const [token, setToken]     = useState(null);
  const [isAdmin, setIsAdmin] = useState(false);
  const [loading, setLoading] = useState(true);

  const warnTimerRef   = useRef(null);
  const logoutTimerRef = useRef(null);

  // ── Clear all pending timers ──────────────────────────────
  const clearTimers = () => {
    if (warnTimerRef.current)   clearTimeout(warnTimerRef.current);
    if (logoutTimerRef.current) clearTimeout(logoutTimerRef.current);
  };

  // ── Schedule warn toast + auto-logout based on token expiry ─
  const scheduleSessionTimers = (jwtToken) => {
    clearTimers();
    const expiry = getExpiry(jwtToken);
    if (!expiry) return;

    const now        = Date.now();
    const msToExpiry = expiry - now;
    const msToWarn   = msToExpiry - WARN_BEFORE_MS;

    // Warn 5 min before expiry
    if (msToWarn > 0) {
      warnTimerRef.current = setTimeout(() => {
        toast.info("⚠️ Your session expires in 5 minutes. Save your work.");
      }, msToWarn);
    } else if (msToExpiry > 0) {
      // Already inside the 5-min window — warn immediately
      const minsLeft = Math.max(1, Math.round(msToExpiry / 60000));
      toast.info(`⚠️ Session expires in ~${minsLeft} minute${minsLeft > 1 ? "s" : ""}.`);
    }

    // Auto-logout at expiry
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
  };

  // ── On mount: validate stored token ──────────────────────
  useEffect(() => {
    const storedToken = localStorage.getItem("token");

    if (storedToken) {
      if (isTokenExpired(storedToken)) {
        localStorage.removeItem("token");
      } else {
        setToken(storedToken);
        try {
          const decoded = jwtDecode(storedToken);
          setIsAdmin(decoded.role === "ROLE_ADMIN");
        } catch {
          localStorage.removeItem("token");
        }
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
    try {
      const decoded = jwtDecode(jwtToken);
      setIsAdmin(decoded.role === "ROLE_ADMIN");
    } catch {
      setIsAdmin(false);
    }
    scheduleSessionTimers(jwtToken);
  };

  const logout = () => {
    doLogout();
  };

  return (
    <AuthContext.Provider
      value={{ token, isAuthenticated: !!token, isAdmin, loading, login, logout }}
    >
      {!loading && children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);