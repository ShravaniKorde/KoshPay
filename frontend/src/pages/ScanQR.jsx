import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { BrowserMultiFormatReader } from "@zxing/browser";

export default function ScanQR() {
  const videoRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    const codeReader = new BrowserMultiFormatReader();

    let isNavigated = false; // prevent double navigation

    codeReader.decodeFromVideoDevice(
      null,
      videoRef.current,
      (result) => {
        if (result && !isNavigated) {
          isNavigated = true;

          const text = result.getText();

          try {
            const queryString = text.split("?")[1];
            const params = new URLSearchParams(queryString);

            const upiId = params.get("pa");
            const amount = params.get("am");

            if (upiId) {
              navigate("/transfer", {
                state: { upiId, amount },
              });
            }
          } catch (e) {
            console.error("Invalid QR format");
          }
        }
      }
    );

    // Cleanup when component unmounts
    return () => {
      try {
        codeReader.reset();
      } catch (e) {
        // ignore cleanup errors
      }
    };
  }, [navigate]);

  return (
    <div className="page-center">
      <div className="card">
        <h2>Scan QR</h2>
        <video
          ref={videoRef}
          style={{ width: "100%", borderRadius: "12px" }}
        />
      </div>
    </div>
  );
}
