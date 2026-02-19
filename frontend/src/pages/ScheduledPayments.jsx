import { useState, useEffect } from "react";
import api from "../api/axios";
import { toast } from "../components/Toast";
import "./ScheduledPayments.css";

export default function ScheduledPayments() {
  const [upiId, setUpiId]       = useState("");
  const [amount, setAmount]     = useState("");
  const [dateTime, setDateTime] = useState("");
  const [loading, setLoading]   = useState(false);
  const [schedules, setSchedules] = useState([]);
  const [editingId, setEditingId] = useState(null);
  const [now, setNow]           = useState(Date.now());

  const loadSchedules = async () => {
    try {
      const res = await api.get("/scheduled-payments");
      setSchedules(res.data);
    } catch { console.error("Failed to load schedules"); }
  };

  useEffect(() => { loadSchedules(); }, []);

  // Tick every second for countdown + progress bar
  useEffect(() => {
    const interval = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(interval);
  }, []);

  const handleSchedule = async (e) => {
    e.preventDefault();
    if (!upiId || !amount || !dateTime) { toast.error("Fill all fields"); return; }
    try {
      setLoading(true);
      if (editingId) {
        await api.put(`/scheduled-payments/${editingId}`, {
          receiverUpiId: upiId,
          amount: Number(amount),
          scheduledAt: new Date(dateTime).toISOString(),
        });
        toast.success("Schedule updated successfully!");
        setEditingId(null);
      } else {
        await api.post("/scheduled-payments", {
          receiverUpiId: upiId,
          amount: Number(amount),
          scheduledAt: new Date(dateTime).toISOString(),
        });
        toast.success("Payment scheduled successfully!");
      }
      setUpiId(""); setAmount(""); setDateTime("");
      loadSchedules();
    } catch (err) {
      toast.error(err.response?.data?.message || "Operation failed");
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (id) => {
    if (!window.confirm("Cancel this scheduled payment?")) return;
    try {
      await api.delete(`/scheduled-payments/${id}`);
      toast.success("Cancelled successfully");
      loadSchedules();
    } catch {
      toast.error("Cancel failed");
    }
  };

  const handleEdit = (item) => {
    if (item.executed) return;
    setEditingId(item.id);
    setUpiId(item.receiverUpiId);
    setAmount(item.amount);
    setDateTime(new Date(item.scheduledAt).toISOString().slice(0, 16));
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const getCountdown = (iso) => {
    const diff = new Date(iso) - now;
    if (diff <= 0) return "Executing...";
    const d = Math.floor(diff / (1000 * 60 * 60 * 24));
    const h = Math.floor((diff / (1000 * 60 * 60)) % 24);
    const m = Math.floor((diff / (1000 * 60)) % 60);
    const s = Math.floor((diff / 1000) % 60);
    return `${d}d ${h}h ${m}m ${s}s`;
  };

  // Progress: % elapsed from creation toward scheduled time
  const getProgress = (item) => {
    const end   = new Date(item.scheduledAt).getTime();
    const start = item.createdAt
      ? new Date(item.createdAt).getTime()
      : end - 24 * 60 * 60 * 1000;
    const total   = end - start;
    const elapsed = now - start;
    if (total <= 0) return 100;
    return Math.min(100, Math.max(0, Math.round((elapsed / total) * 100)));
  };

  const formatDate = (iso) => new Date(iso).toLocaleString("en-IN");

  return (
    <div className="sp-page">

      <div className="sp-form-card">
        <h2 className="sp-form-title">
          {editingId ? "✏️ Edit Scheduled Payment" : "⏳ Schedule Payment"}
        </h2>

        <form onSubmit={handleSchedule} className="sp-form">
          <input type="text" placeholder="Recipient UPI ID" value={upiId}
            onChange={(e) => setUpiId(e.target.value)} className="sp-input" required />
          <input type="number" placeholder="Amount (₹)" value={amount}
            onChange={(e) => setAmount(e.target.value)} className="sp-input" required />
          <input type="datetime-local" value={dateTime}
            onChange={(e) => setDateTime(e.target.value)} className="sp-input" required />
          <button type="submit" className="sp-submit-btn" disabled={loading}>
            {loading
              ? editingId ? "Updating..." : "Scheduling..."
              : editingId ? "Update Payment" : "Schedule Payment"}
          </button>
          {editingId && (
            <button type="button" className="sp-cancel-edit-btn"
              onClick={() => { setEditingId(null); setUpiId(""); setAmount(""); setDateTime(""); }}>
              Cancel Edit
            </button>
          )}
        </form>
      </div>

      <div className="sp-list-section">
        <div className="sp-list-heading">📅 Upcoming Payments</div>

        {schedules.length === 0 && (
          <div className="sp-empty">No scheduled payments yet</div>
        )}

        {schedules.map((item) => {
          const progress  = getProgress(item);
          const isPending = !item.executed && item.status === "PENDING";

          return (
            <div key={item.id} className="sp-card">
              <div className="sp-card__left">
                <div className="sp-card__upi">{item.receiverUpiId}</div>
                <div className="sp-card__amount">
                  ₹{Number(item.amount).toLocaleString("en-IN")}
                </div>
                <div className="sp-card__date">{formatDate(item.scheduledAt)}</div>

                {/* Progress bar — only for PENDING */}
                {isPending && (
                  <div className="sp-progress-wrap">
                    <div className="sp-progress-bar">
                      <div className="sp-progress-fill" style={{ width: `${progress}%` }} />
                    </div>
                    <div className="sp-card__countdown">
                      ⏳ {getCountdown(item.scheduledAt)}
                    </div>
                  </div>
                )}

                <span className={`sp-status ${item.status}`}>{item.status}</span>
              </div>

              {!item.executed && (
                <div className="sp-card__actions">
                  <button className="sp-edit-btn" onClick={() => handleEdit(item)}>Edit</button>
                  <button className="sp-del-btn"  onClick={() => handleCancel(item.id)}>Delete</button>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}