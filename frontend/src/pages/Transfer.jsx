import { useState } from "react";
import api from "../api/axios";
import { useNavigate } from "react-router-dom";

export default function Transfer() {
  const [toUpiId, setToUpiId] = useState(""); // Renamed for clarity
  const [amount, setAmount] = useState("");
  const [pin, setPin] = useState(""); 
  const [loading, setLoading] = useState(false);

  // OTP States
  const [showOtpModal, setShowOtpModal] = useState(false);
  const [otp, setOtp] = useState("");

  const navigate = useNavigate();

  // STEP 1: Initial Transfer Request (Will trigger OTP if > 1000)
  const submit = async (e) => {
    e.preventDefault();

    if (!toUpiId || !amount || Number(amount) <= 0 || pin.length !== 4) {
      alert("Enter valid UPI ID, amount, and 4-digit PIN");
      return;
    }

    try {
      setLoading(true);

      const res = await api.post("/upi/transfer", {
        toUpiId: toUpiId,
        amount: Number(amount),
        pin: pin,
        // otp is omitted here on purpose
      });

      // Check if backend returned the OtpResponse object
      if (res.data && res.data.status === "OTP_REQUIRED") {
        setShowOtpModal(true);
      } else {
        alert("✅ Transfer successful");
        navigate("/dashboard");
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || "Transfer failed";
      alert(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  // STEP 2: Verify OTP (Hits the SAME endpoint but includes the OTP)
  const handleVerifyOtp = async () => {
    try {
      setLoading(true);
      const res = await api.post("/upi/transfer", {
        toUpiId: toUpiId,
        amount: Number(amount),
        pin: pin,
        otp: otp, // Now sending the OTP collected from the modal
      });

      // If successful, backend returns "UPI transfer successful" or similar string
      alert("✅ Transfer successful!");
      navigate("/dashboard");
    } catch (err) {
      alert(err.response?.data?.message || "Invalid OTP or Transaction Failed");
    } finally {
      setLoading(false);
      setShowOtpModal(false);
      setOtp(""); // Clear OTP for security
    }
  };

  return (
    <div style={styles.wrapper}>
      <div style={styles.card}>
        <h2 style={styles.title}>💸 UPI Transfer</h2>
        <p style={styles.subtitle}>Send money securely using UPI ID</p>

        <form onSubmit={submit} style={styles.form}>
          <label style={styles.label}>Recipient UPI ID</label>
          <input
            type="text"
            placeholder="e.g. user@okaxis"
            value={toUpiId}
            onChange={(e) => setToUpiId(e.target.value)}
            style={styles.input}
            required
          />

          <label style={styles.label}>Amount (₹)</label>
          <input
            type="number"
            placeholder="e.g. 1200"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            style={styles.input}
            required
          />

          <label style={styles.label}>Transaction PIN (4 Digits)</label>
          <input
            type="password"
            placeholder="****"
            maxLength="4"
            value={pin}
            onChange={(e) => setPin(e.target.value.replace(/\D/g, ""))}
            style={styles.input}
            required
          />

          <button
            type="submit"
            disabled={loading}
            style={{
              ...styles.button,
              opacity: loading ? 0.7 : 1,
            }}
          >
            {loading ? "Processing..." : "Send Money"}
          </button>
        </form>
      </div>

      {showOtpModal && (
        <div style={styles.modalOverlay}>
          <div style={styles.modal}>
            <h3 style={{ marginBottom: "10px" }}>🔐 OTP Verification</h3>
            <p style={{ fontSize: "0.85rem", color: "#666", marginBottom: "15px" }}>
              A high-value transfer requires verification. <br/>
              Enter the code sent to your email.
            </p>
            <input
              type="text"
              placeholder="Enter OTP"
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              style={{ ...styles.input, textAlign: "center", letterSpacing: "5px" }}
            />
            <button 
              onClick={handleVerifyOtp} 
              style={{ ...styles.button, width: "100%", marginTop: "15px" }}
            >
              Verify & Complete
            </button>
            <button 
              onClick={() => setShowOtpModal(false)} 
              style={{ background: "none", border: "none", color: "red", marginTop: "10px", cursor: "pointer" }}
            >
              Cancel
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

const styles = {
  wrapper: { minHeight: "80vh", display: "flex", alignItems: "center", justifyContent: "center", padding: "1rem" },
  card: { width: "100%", maxWidth: "420px", background: "#ffffff", borderRadius: "16px", padding: "2.2rem", boxShadow: "0 15px 40px rgba(0,0,0,0.12)" },
  title: { textAlign: "center", marginBottom: "0.4rem" },
  subtitle: { textAlign: "center", color: "#6b7280", fontSize: "0.9rem", marginBottom: "1.8rem" },
  form: { display: "flex", flexDirection: "column", gap: "0.9rem" },
  label: { fontSize: "0.85rem", fontWeight: "600", color: "#374151" },
  input: { padding: "0.6rem 0.7rem", borderRadius: "8px", border: "1px solid #d1d5db", fontSize: "0.9rem" },
  button: { marginTop: "1rem", padding: "0.65rem", borderRadius: "10px", border: "none", background: "linear-gradient(90deg, #2563eb, #1e40af)", color: "#fff", fontSize: "0.95rem", fontWeight: "600", cursor: "pointer" },
  modalOverlay: { position: "fixed", top: 0, left: 0, right: 0, bottom: 0, background: "rgba(0,0,0,0.5)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 1000 },
  modal: { background: "#fff", padding: "2rem", borderRadius: "12px", width: "320px", textAlign: "center", boxShadow: "0 10px 25px rgba(0,0,0,0.2)" },
};