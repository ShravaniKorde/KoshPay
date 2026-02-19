import { useEffect, useState } from "react";
import "./Toast.css";

// ── Global toast queue (module-level, no context needed) ──────
let _setToasts = null;
let _id = 0;

export function toast(message, type = "success") {
  if (!_setToasts) return;
  const id = ++_id;
  _setToasts((prev) => [...prev, { id, message, type }]);
  setTimeout(() => {
    _setToasts((prev) => prev.filter((t) => t.id !== id));
  }, 3500);
}

// Convenience helpers
toast.success = (msg) => toast(msg, "success");
toast.error   = (msg) => toast(msg, "error");
toast.info    = (msg) => toast(msg, "info");

// ── ToastContainer — mount once in App.jsx ───────────────────
export default function ToastContainer() {
  const [toasts, setToasts] = useState([]);
  _setToasts = setToasts;

  const remove = (id) => setToasts((p) => p.filter((t) => t.id !== id));

  return (
    <div className="toast-container">
      {toasts.map((t) => (
        <div key={t.id} className={`toast toast--${t.type}`}>
          <span className="toast__icon">
            {t.type === "success" ? "✓" : t.type === "error" ? "✕" : "ℹ"}
          </span>
          <span className="toast__msg">{t.message}</span>
          <button className="toast__close" onClick={() => remove(t.id)}>×</button>
        </div>
      ))}
    </div>
  );
}