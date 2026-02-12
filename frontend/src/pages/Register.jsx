import { useState } from "react";
import api from "../api/axios";
import { useNavigate, Link } from "react-router-dom";

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
    
    // Validate PIN length before sending to backend
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
    <div className="container" style={{ maxWidth: "420px" }}>
      <h2 style={{ textAlign: "center", marginBottom: "1rem" }}>
        Create Wallet Account
      </h2>

      <form onSubmit={submit} className="card">
        <label>Full Name</label>
        <input
          type="text"
          placeholder="John Doe"
          required
          value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
        />

        <label>Email</label>
        <input
          type="email"
          placeholder="john@email.com"
          required
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
        />

        <label>Password</label>
        <input
          type="password"
          placeholder="********"
          required
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
        />

        <label>Transaction PIN (4 Digits)</label>
        <input
          type="password"
          placeholder="1234"
          maxLength="4"
          required
          value={form.transactionPin}
          onChange={(e) =>
            setForm({ ...form, transactionPin: e.target.value.replace(/\D/g, '') })
          }
        />
        <small style={{ color: "#666", display: "block", marginBottom: "1rem" }}>
          Used for authorizing payments.
        </small>

        <label>Initial Balance (₹)</label>
        <input
          type="number"
          min={1000}
          step={100}
          value={form.initialBalance}
          onChange={(e) =>
            setForm({
              ...form,
              initialBalance: Number(e.target.value),
            })
          }
        />

        <small style={{ color: "#666" }}>
          Minimum balance required: ₹1000
        </small>

        <button type="submit" style={{ marginTop: "1rem" }}>
          Create Account
        </button>
      </form>

      <p style={{ textAlign: "center", marginTop: "1rem" }}>
        Already have an account? <Link to="/login">Login</Link>
      </p>
    </div>
  );
}