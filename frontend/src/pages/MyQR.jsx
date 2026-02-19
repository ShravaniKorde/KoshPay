// ── MyQR.jsx ────────────────────────────────────────────────────────────────
import { useEffect, useState } from "react";
import api from "../api/axios";
import { QRCodeCanvas } from "qrcode.react";
import "./MyQR.css";

export default function MyQR() {
  const [upiId, setUpiId]       = useState("");
  const [qrPayload, setQrPayload] = useState("");
  const [amount, setAmount]     = useState("");
  const [loading, setLoading]   = useState(true);

  useEffect(() => {
    const loadUpi = async () => {
      try {
        const res   = await api.get("/upi/me");
        setUpiId(res.data.upiId);
        const qrRes = await api.get("/upi/qr");
        setQrPayload(qrRes.data.payload);
      } catch (err) {
        console.error("QR load failed", err);
      } finally {
        setLoading(false);
      }
    };
    loadUpi();
  }, []);

  const generateWithAmount = async () => {
    try {
      const qrRes = await api.get("/upi/qr", { params: { amount } });
      setQrPayload(qrRes.data.payload);
    } catch (err) {
      console.error("QR generation failed", err);
    }
  };

  if (loading) {
    return (
      <div className="myqr-page">
        <div className="myqr-card">
          <p style={{ color: "var(--text-muted)", fontFamily: "'JetBrains Mono', monospace", fontSize: "0.85rem" }}>
            Loading QR...
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="myqr-page">
      <div className="myqr-card">
        <h2 className="myqr-title">My UPI QR Code</h2>
        <div className="myqr-upi">{upiId}</div>

        {qrPayload && (
          <div className="myqr-frame">
            <QRCodeCanvas value={qrPayload} size={210} />
          </div>
        )}

        <div className="myqr-amount-section">
          <input
            type="number"
            placeholder="Optional amount (₹)"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            className="myqr-amount-input"
          />
          <button onClick={generateWithAmount} className="myqr-gen-btn">
            Generate
          </button>
        </div>
      </div>
    </div>
  );
}