import { useState, useEffect } from "react";
import { useNavigate, useLocation, useSearchParams } from "react-router-dom";
import api from "../api/axios";

export default function Transfer() {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();

  const [toUpiId, setToUpiId] = useState("");
  const [amount, setAmount] = useState("");
  const [pin, setPin] = useState("");
  const [loading, setLoading] = useState(false);

  const [showOtpModal, setShowOtpModal] = useState(false);
  const [otp, setOtp] = useState("");
  const [backendOtp, setBackendOtp] = useState("");

  const [amountLocked, setAmountLocked] = useState(false);

  // OTP TIMER
  const [timer, setTimer] = useState(0);

  // ===============================
  // AUTO FILL FROM QR
  // ===============================
  useEffect(() => {
    const upiFromQR = searchParams.get("upi");
    const amountFromQR = searchParams.get("amount");

    const upiFromState = location.state?.upiId;
    const amountFromState = location.state?.amount;

    if (upiFromQR) {
      setToUpiId(upiFromQR);
      if (amountFromQR) {
        setAmount(amountFromQR);
        setAmountLocked(true);
      } else {
        setAmount("");
        setAmountLocked(false);
      }
    } else if (upiFromState) {
      setToUpiId(upiFromState);
      if (amountFromState) {
        setAmount(amountFromState);
        setAmountLocked(true);
      } else {
        setAmount("");
        setAmountLocked(false);
      }
    }
  }, [location, searchParams]);

  // ===============================
  // OTP TIMER EFFECT
  // ===============================
  useEffect(() => {
    let interval;
    if (timer > 0) {
      interval = setInterval(() => {
        setTimer((prev) => prev - 1);
      }, 1000);
    }
    return () => clearInterval(interval);
  }, [timer]);

  // ===============================
  // SUBMIT TRANSFER
  // ===============================
  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!toUpiId || !amount || Number(amount) <= 0 || pin.length !== 4) {
      alert("Enter valid UPI ID, amount, and 4-digit PIN");
      return;
    }

    try {
      setLoading(true);

      const res = await api.post("/upi/transfer", {
        toUpiId,
        amount: Number(amount),
        pin,
      });

      if (res.data?.status === "OTP_REQUIRED") {
        setBackendOtp(res.data.otp);
        alert(`🔐 OTP Sent!\n\nYour OTP is: ${res.data.otp}`);
        setShowOtpModal(true);
        setTimer(60); // 60 sec countdown
        setOtp("");
        return;
      }

      if (res.data === "SUCCESS" || res.data === "UPI transfer successful") {
        alert("✅ Transfer Successful");
        navigate("/dashboard");
      }

    } catch (err) {
      alert(
        err.response?.data?.message ||
        err.response?.data ||
        "Transfer failed"
      );
    } finally {
      setLoading(false);
    }
  };

  // ===============================
  // VERIFY OTP
  // ===============================
  const handleVerifyOtp = async () => {
    if (otp.length !== 4) {
      alert("Enter valid 4-digit OTP");
      return;
    }

    try {
      setLoading(true);

      await api.post("/upi/transfer", {
        toUpiId,
        amount: Number(amount),
        pin,
        otp,
      });

      alert("✅ Transfer Successful!");
      setShowOtpModal(false);
      navigate("/dashboard");

    } catch (err) {
      alert(
        err.response?.data?.message ||
        err.response?.data ||
        "Invalid OTP"
      );
    } finally {
      setLoading(false);
    }
  };

  // ===============================
  // RESEND OTP
  // ===============================
  const handleResendOtp = async () => {
    if (timer > 0) return;

    try {
      const res = await api.post("/upi/transfer", {
        toUpiId,
        amount: Number(amount),
        pin,
      });

      if (res.data?.status === "OTP_REQUIRED") {
        setBackendOtp(res.data.otp);
        alert(`🔁 New OTP Sent!\n\nYour OTP is: ${res.data.otp}`);
        setTimer(60);
      }

    } catch (err) {
      alert("Failed to resend OTP");
    }
  };

  return (
    <div style={styles.wrapper}>
      <div style={styles.card}>
        <h2 style={styles.title}>💸 Wallet Transfer</h2>
        <p style={styles.subtitle}>Send money securely using UPI ID</p>

        <form onSubmit={handleSubmit} style={styles.form}>
          <label style={styles.label}>Recipient UPI ID</label>
          <input
            type="text"
            value={toUpiId}
            onChange={(e) => setToUpiId(e.target.value)}
            style={styles.input}
            required
          />

          <label style={styles.label}>
            Amount (₹)
            {amountLocked && <span style={styles.lockTag}>Fixed by QR</span>}
          </label>

          <input
            type="number"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            disabled={amountLocked}
            style={{
              ...styles.input,
              background: amountLocked ? "#f3f4f6" : "#ffffff",
            }}
            required
          />

          <label style={styles.label}>Transaction PIN</label>
          <input
            type="password"
            maxLength="4"
            value={pin}
            onChange={(e) => setPin(e.target.value.replace(/\D/g, ""))}
            style={styles.input}
            required
          />

          <button
            type="submit"
            disabled={loading}
            style={styles.button}
          >
            {loading ? "Processing..." : "Send Money"}
          </button>
        </form>
      </div>

      {/* OTP MODAL */}
      {showOtpModal && (
        <div style={styles.modalOverlay}>
          <div style={styles.modal}>
            <h3>🔐 OTP Verification</h3>

            <p style={styles.modalText}>
              Enter the 4-digit OTP sent to you
            </p>

            <input
              type="text"
              maxLength="4"
              value={otp}
              onChange={(e) => setOtp(e.target.value.replace(/\D/g, ""))}
              style={styles.otpInput}
            />

            <div style={styles.timerText}>
              {timer > 0
                ? `Resend OTP in ${timer}s`
                : "You can resend OTP now"}
            </div>

            <button
              onClick={handleVerifyOtp}
              style={styles.button}
              disabled={loading}
            >
              Verify & Complete
            </button>

            <button
              onClick={handleResendOtp}
              disabled={timer > 0}
              style={{
                ...styles.resendBtn,
                opacity: timer > 0 ? 0.5 : 1,
              }}
            >
              🔁 Resend OTP
            </button>

            <button
              onClick={() => setShowOtpModal(false)}
              style={styles.cancelBtn}
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
  wrapper: {
    minHeight: "80vh",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    background: "linear-gradient(135deg, #f0f4ff, #ffffff)",
  },
  card: {
    width: "420px",
    background: "#fff",
    padding: "2rem",
    borderRadius: "16px",
    boxShadow: "0 20px 50px rgba(0,0,0,0.12)",
  },
  title: { textAlign: "center" },
  subtitle: {
    textAlign: "center",
    color: "#6b7280",
    marginBottom: "1.5rem",
  },
  form: { display: "flex", flexDirection: "column", gap: "0.9rem" },
  label: { fontWeight: "600" },
  lockTag: {
    fontSize: "0.7rem",
    background: "#e0e7ff",
    padding: "2px 8px",
    borderRadius: "20px",
  },
  input: {
    padding: "0.6rem",
    borderRadius: "8px",
    border: "1px solid #ccc",
  },
  button: {
    marginTop: "1rem",
    padding: "0.7rem",
    borderRadius: "10px",
    border: "none",
    background: "linear-gradient(90deg,#2563eb,#1e40af)",
    color: "#fff",
    cursor: "pointer",
  },
  modalOverlay: {
    position: "fixed",
    inset: 0,
    background: "rgba(0,0,0,0.6)",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
  },
  modal: {
    background: "#fff",
    padding: "2rem",
    borderRadius: "12px",
    width: "320px",
    textAlign: "center",
  },
  modalText: { marginBottom: "10px", color: "#555" },
  otpInput: {
    padding: "0.6rem",
    fontSize: "1.2rem",
    textAlign: "center",
    letterSpacing: "8px",
    borderRadius: "8px",
    border: "1px solid #ccc",
    width: "100%",
  },
  timerText: {
    marginTop: "10px",
    fontSize: "0.85rem",
    color: "#555",
  },
  resendBtn: {
    marginTop: "10px",
    background: "none",
    border: "none",
    color: "#2563eb",
    cursor: "pointer",
  },
  cancelBtn: {
    marginTop: "8px",
    background: "none",
    border: "none",
    color: "red",
    cursor: "pointer",
  },
};
