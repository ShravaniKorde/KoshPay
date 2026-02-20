import { useState } from "react";
import api from "../api/axios";
import { useAuth } from "../auth/AuthContext";
import { useNavigate, Link } from "react-router-dom";
import "./Login.css";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [mode, setMode] = useState("user");
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
        adminLogin: mode === "admin",
      });
      login(res.data.token);
      navigate(mode === "admin" ? "/admin/dashboard" : "/dashboard");
    } catch (err) {
      alert(err.response?.data?.message || "Invalid credentials");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-logo">KoshPay</div>
        <h2 className="login-title">Welcome Back</h2>
        <p className="login-subtitle">
          {mode === "admin"
            ? "Admin access to KoshPay Control Panel"
            : "Login to access your KoshPay wallet"}
        </p>

        {/* Toggle */}
        <div className="login-toggle">
          <button
            type="button"
            className={`login-toggle__btn ${mode === "user" ? "active" : ""}`}
            onClick={() => setMode("user")}
          >
            User
          </button>
          <button
            type="button"
            className={`login-toggle__btn ${mode === "admin" ? "active" : ""}`}
            onClick={() => setMode("admin")}
          >
            Admin
          </button>
        </div>

        <form onSubmit={submit} className="login-form">
          <label className="login-label">Email</label>
          <input
            type="email"
            required
            placeholder={mode === "admin" ? "admin@koshpay.com" : "you@example.com"}
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="login-input"
          />

          <label className="login-label">Password</label>
          <input
            type="password"
            required
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="login-input"
          />

          <button type="submit" disabled={loading} className="login-submit">
            {loading ? "Authenticating..." : "Login"}
          </button>
        </form>

        {mode === "user" && (
          <p className="login-footer">
            New user? <Link to="/register">Create account</Link>
          </p>
        )}
      </div>
    </div>
  );
}