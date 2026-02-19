import { useState, useEffect } from "react";
import { useNavigate, useLocation, useSearchParams } from "react-router-dom";
import api from "../api/axios";
import { toast } from "../components/Toast";
import "./Transfer.css";

// ── Confetti helper ───────────────────────────────────────────
// Install: npm install canvas-confetti
// Then replace the dynamic import below with:
// import confetti from "canvas-confetti";
function fireConfetti() {
  try {
    import("canvas-confetti").then(({ default: confetti }) => {
      confetti({
        particleCount: 120,
        spread: 70,
        origin: { y: 0.6 },
        colors: ["#f5c842", "#4ade80", "#60a5fa"],
      });
    });
  } catch {}
}

export default function Transfer() {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();

  const [toUpiId, setToUpiId]   = useState("");
  const [amount, setAmount]     = useState("");
  const [pin, setPin]           = useState("");
  const [loading, setLoading]   = useState(false);
  const [amountLocked, setAmountLocked] = useState(false);

  const [showOtpModal, setShowOtpModal] = useState(false);
  const [otp, setOtp]       = useState("");
  const [backendOtp, setBackendOtp] = useState("");
  const [timer, setTimer]   = useState(0);

  // Auto fill from QR / state
  useEffect(() => {
    const upiFromQR    = searchParams.get("upi");
    const amountFromQR = searchParams.get("amount");
    const upiFromState    = location.state?.upiId;
    const amountFromState = location.state?.amount;

    if (upiFromQR) {
      setToUpiId(upiFromQR);
      if (amountFromQR) { setAmount(amountFromQR); setAmountLocked(true); }
      else { setAmount(""); setAmountLocked(false); }
    } else if (upiFromState) {
      setToUpiId(upiFromState);
      if (amountFromState) { setAmount(amountFromState); setAmountLocked(true); }
      else { setAmount(""); setAmountLocked(false); }
    }
  }, [location, searchParams]);

  // OTP countdown
  useEffect(() => {
    let interval;
    if (timer > 0) interval = setInterval(() => setTimer((p) => p - 1), 1000);
    return () => clearInterval(interval);
  }, [timer]);

  // Submit transfer
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!toUpiId || !amount || Number(amount) <= 0 || pin.length !== 4) {
      toast.error("Enter valid UPI ID, amount, and 4-digit PIN");
      return;
    }
    try {
      setLoading(true);
      const res = await api.post("/upi/transfer", {
        toUpiId, amount: Number(amount), pin,
      });

      if (res.data?.status === "OTP_REQUIRED") {
        setBackendOtp(res.data.otp);
        toast.info(`OTP sent! Your OTP is: ${res.data.otp}`);
        setShowOtpModal(true);
        setTimer(60);
        setOtp("");
        return;
      }

      if (res.data === "SUCCESS" || res.data === "UPI transfer successful") {
        fireConfetti();
        toast.success("Transfer successful!");
        setTimeout(() => navigate("/dashboard"), 1200);
      }
    } catch (err) {
      toast.error(err.response?.data?.message || err.response?.data || "Transfer failed");
    } finally {
      setLoading(false);
    }
  };

  // Verify OTP
  const handleVerifyOtp = async () => {
    if (otp.length !== 4) { toast.error("Enter valid 4-digit OTP"); return; }
    try {
      setLoading(true);
      await api.post("/upi/transfer", { toUpiId, amount: Number(amount), pin, otp });
      setShowOtpModal(false);
      fireConfetti();
      toast.success("Transfer successful!");
      setTimeout(() => navigate("/dashboard"), 1200);
    } catch (err) {
      toast.error(err.response?.data?.message || err.response?.data || "Invalid OTP");
    } finally {
      setLoading(false);
    }
  };

  // Resend OTP
  const handleResendOtp = async () => {
    if (timer > 0) return;
    try {
      const res = await api.post("/upi/transfer", { toUpiId, amount: Number(amount), pin });
      if (res.data?.status === "OTP_REQUIRED") {
        setBackendOtp(res.data.otp);
        toast.info(`New OTP sent! Your OTP is: ${res.data.otp}`);
        setTimer(60);
      }
    } catch {
      toast.error("Failed to resend OTP");
    }
  };

  return (
    <div className="transfer-page">
      <div className="transfer-card">
        <h2 className="transfer-title">💸 Wallet Transfer</h2>
        <p className="transfer-subtitle">Send money securely using UPI ID</p>

        <form onSubmit={handleSubmit} className="transfer-form">
          <label className="transfer-label">Recipient UPI ID</label>
          <input
            type="text"
            value={toUpiId}
            onChange={(e) => setToUpiId(e.target.value)}
            className="transfer-input"
            placeholder="user@okaxis"
            required
          />

          <label className="transfer-label">
            Amount (₹)
            {amountLocked && <span className="transfer-lock-tag">Fixed by QR</span>}
          </label>
          <input
            type="number"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            disabled={amountLocked}
            className="transfer-input"
            placeholder="0"
            required
          />

          <label className="transfer-label">Transaction PIN</label>
          <input
            type="password"
            maxLength="4"
            value={pin}
            onChange={(e) => setPin(e.target.value.replace(/\D/g, ""))}
            className="transfer-input"
            placeholder="••••"
            required
          />

          <button type="submit" disabled={loading} className="transfer-submit">
            {loading ? "Processing..." : "Send Money →"}
          </button>
        </form>
      </div>

      {/* OTP Modal */}
      {showOtpModal && (
        <div className="otp-overlay">
          <div className="otp-modal">
            <div className="otp-modal__icon">🔐</div>
            <div className="otp-modal__title">OTP Verification</div>
            <p className="otp-modal__text">Enter the 4-digit OTP sent to you</p>

            <input
              type="text"
              maxLength="4"
              value={otp}
              onChange={(e) => setOtp(e.target.value.replace(/\D/g, ""))}
              className="otp-modal__input"
              placeholder="0000"
            />

            <p className="otp-timer">
              {timer > 0 ? `Resend in ${timer}s` : "You can resend OTP now"}
            </p>

            <button onClick={handleVerifyOtp} disabled={loading} className="otp-verify-btn">
              {loading ? "Verifying..." : "Verify & Complete"}
            </button>
            <br />
            <button onClick={handleResendOtp} disabled={timer > 0} className="otp-resend-btn">
              🔁 Resend OTP
            </button>
            <br />
            <button onClick={() => setShowOtpModal(false)} className="otp-cancel-btn">
              Cancel
            </button>
          </div>
        </div>
      )}
    </div>
  );
}