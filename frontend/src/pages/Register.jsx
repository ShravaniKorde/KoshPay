import { useState } from "react";
import api from "../api/axios";
import { useNavigate, Link } from "react-router-dom";
import "./Register.css";

export default function Register() {
  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    transactionPin: "",
    initialBalance: 1000,
  });

  const navigate = useNavigate();

  const submit = async (e) => {
    e.preventDefault();
    if (form.initialBalance < 1000) {
      alert("Minimum initial balance is ₹1000");
      return;
    }
    if (form.transactionPin.length !== 4) {
      alert("Transaction PIN must be exactly 4 digits");
      return;
    }
    try {
      await api.post("/setup/user", form);
      alert("Account created successfully");
      navigate("/login");
    } catch (err) {
      alert(err.response?.data?.message || "Registration failed");
    }
  };

  return (
    <div className="register-page">
      <div className="register-card">
        <div className="register-logo">KoshPay</div>
        <h2 className="register-title">Create Wallet Account</h2>
        <p className="register-subtitle">Join KoshPay and start transacting instantly</p>

        <form onSubmit={submit} className="register-form">
          <label className="register-label">Full Name</label>
          <input
            type="text"
            placeholder="John Doe"
            required
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            className="register-input"
          />

          <label className="register-label">Email</label>
          <input
            type="email"
            placeholder="john@email.com"
            required
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            className="register-input"
          />

          <label className="register-label">Password</label>
          <input
            type="password"
            placeholder="••••••••"
            required
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            className="register-input"
          />

          <label className="register-label">Transaction PIN (4 Digits)</label>
          <input
            type="password"
            placeholder="1234"
            maxLength="4"
            required
            value={form.transactionPin}
            onChange={(e) =>
              setForm({ ...form, transactionPin: e.target.value.replace(/\D/g, "") })
            }
            className="register-input"
          />
          <p className="register-hint">Used for authorizing every payment.</p>

          <label className="register-label">Initial Balance (₹)</label>
          <input
            type="number"
            min={1000}
            step={100}
            value={form.initialBalance}
            onChange={(e) => setForm({ ...form, initialBalance: Number(e.target.value) })}
            className="register-input"
          />
          <p className="register-hint">Minimum required: ₹1,000</p>

          <button type="submit" className="register-submit">
            Create Account
          </button>
        </form>

        <p className="register-footer">
          Already have an account? <Link to="/login">Login</Link>
        </p>
      </div>
    </div>
  );
}