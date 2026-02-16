import { useState } from "react";
import api from "../api/axios";
import { useAuth } from "../auth/AuthContext";
import { useNavigate, Link } from "react-router-dom";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [mode, setMode] = useState("user"); // user | admin
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const res = await api.post("/auth/login", {
        email,
        password,
        adminLogin: mode === "admin", // 🔥 IMPORTANT FIX
      });

      login(res.data.token);

      // Redirect properly
      navigate(mode === "admin" ? "/admin/dashboard" : "/dashboard");

    } catch (err) {
      alert(err.response?.data?.message || "Invalid credentials");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>

        <h2 style={styles.title}>Welcome Back 👋</h2>

        <p style={styles.subtitle}>
          {mode === "admin"
            ? "Admin access to KoshPay Control Panel"
            : "Login to access your KoshPay account"}
        </p>

        {/* Toggle */}
        <div style={styles.toggleWrapper}>
          <button
            type="button"
            onClick={() => setMode("user")}
            style={{
              ...styles.toggleBtn,
              ...(mode === "user" ? styles.activeToggle : {})
            }}
          >
            User
          </button>

          <button
            type="button"
            onClick={() => setMode("admin")}
            style={{
              ...styles.toggleBtn,
              ...(mode === "admin" ? styles.activeToggle : {})
            }}
          >
            Admin
          </button>
        </div>

        <form onSubmit={submit} style={styles.form}>
          <label style={styles.label}>Email</label>
          <input
            type="email"
            required
            placeholder={
              mode === "admin"
                ? "admin@koshpay.com"
                : "you@example.com"
            }
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            style={styles.input}
          />

          <label style={styles.label}>Password</label>
          <input
            type="password"
            required
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            style={styles.input}
          />

          <button
            type="submit"
            disabled={loading}
            style={{
              ...styles.submitBtn,
              opacity: loading ? 0.7 : 1
            }}
          >
            {loading ? "Authenticating..." : "Login"}
          </button>
        </form>

        {mode === "user" && (
          <p style={styles.registerText}>
            New user?{" "}
            <Link to="/register" style={styles.link}>
              Create account
            </Link>
          </p>
        )}

      </div>
    </div>
  );
}

/* ===================== STYLES ===================== */

const styles = {
  container: {
    minHeight: "100vh",
    background: "linear-gradient(135deg, #0f172a, #1e293b)",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    padding: "1rem",
  },

  card: {
    background: "#ffffff",
    width: "100%",
    maxWidth: "420px",
    padding: "2.5rem",
    borderRadius: "18px",
    boxShadow: "0 25px 60px rgba(0, 0, 0, 0.25)",
    transition: "0.3s",
  },

  title: {
    textAlign: "center",
    marginBottom: "0.4rem",
  },

  subtitle: {
    textAlign: "center",
    color: "#64748b",
    fontSize: "0.9rem",
    marginBottom: "1.5rem",
  },

  toggleWrapper: {
    display: "flex",
    marginBottom: "1.5rem",
    background: "#f1f5f9",
    borderRadius: "999px",
    padding: "4px",
  },

  toggleBtn: {
    flex: 1,
    padding: "0.6rem",
    border: "none",
    background: "transparent",
    borderRadius: "999px",
    cursor: "pointer",
    fontWeight: "600",
    transition: "0.3s",
  },

  activeToggle: {
    background: "#2563eb",
    color: "#fff",
  },

  form: {
    display: "flex",
    flexDirection: "column",
  },

  label: {
    fontSize: "0.85rem",
    fontWeight: "600",
    marginTop: "0.8rem",
  },

  input: {
    width: "100%",
    padding: "0.7rem",
    borderRadius: "8px",
    border: "1px solid #cbd5e1",
    marginTop: "0.3rem",
    fontSize: "0.9rem",
  },

  submitBtn: {
    width: "100%",
    marginTop: "1.5rem",
    padding: "0.75rem",
    border: "none",
    borderRadius: "10px",
    background: "linear-gradient(90deg, #2563eb, #1e40af)",
    color: "white",
    fontWeight: "600",
    cursor: "pointer",
    transition: "0.3s",
  },

  registerText: {
    textAlign: "center",
    marginTop: "1.3rem",
    fontSize: "0.85rem",
  },

  link: {
    color: "#2563eb",
    fontWeight: "600",
    textDecoration: "none",
  },
};
