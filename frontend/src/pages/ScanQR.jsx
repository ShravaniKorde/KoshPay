import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { BrowserMultiFormatReader } from "@zxing/browser";
import "./ScanQR.css";

export default function ScanQR() {
  const videoRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    const codeReader = new BrowserMultiFormatReader();
    let isNavigated = false;

    codeReader.decodeFromVideoDevice(null, videoRef.current, (result) => {
      if (result && !isNavigated) {
        isNavigated = true;
        const text = result.getText();
        try {
          const queryString = text.split("?")[1];
          const params = new URLSearchParams(queryString);
          const upiId  = params.get("pa");
          const amount = params.get("am");
          if (upiId) navigate("/transfer", { state: { upiId, amount } });
        } catch {
          console.error("Invalid QR format");
        }
      }
    });

    return () => {
      try { codeReader.reset(); } catch {}
    };
  }, [navigate]);

  return (
    <div className="scanqr-page">
      <div className="scanqr-card">
        <h2 className="scanqr-title">Scan QR Code</h2>
        <p className="scanqr-subtitle">Point your camera at a KoshPay QR to pay instantly</p>

        <div className="scanqr-video-frame">
          <video ref={videoRef} />
        </div>

        <p className="scanqr-hint">
          <span className="live-dot" />
          Camera active — waiting for QR
        </p>
      </div>
    </div>
  );
}
