import { useEffect, useState } from "react";
import api from "../api/axios";
import { QRCodeCanvas } from "qrcode.react";

export default function MyQR() {
  const [upiId, setUpiId] = useState("");
  const [qrPayload, setQrPayload] = useState("");
  const [amount, setAmount] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadUpi = async () => {
      try {
        const res = await api.get("/upi/me");
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
      const qrRes = await api.get("/upi/qr", {
        params: { amount },
      });
      setQrPayload(qrRes.data.payload);
    } catch (err) {
      console.error("QR generation failed", err);
    }
  };

  if (loading) {
    return <div className="page-center">Loading QR...</div>;
  }

  return (
    <div className="page-center">
      <div className="card qr-card">
        <h2>My UPI QR Code</h2>
        <p className="muted">{upiId}</p>

        {qrPayload && (
          <div className="qr-wrapper">
            <QRCodeCanvas value={qrPayload} size={220} />
          </div>
        )}

        <div className="qr-amount-section">
          <input
            type="number"
            placeholder="Optional amount"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
          />
          <button onClick={generateWithAmount}>
            Generate with Amount
          </button>
        </div>
      </div>
    </div>
  );
}
