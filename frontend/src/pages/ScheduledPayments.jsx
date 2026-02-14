import { useState, useEffect } from "react";
import api from "../api/axios";

export default function ScheduledPayments() {
  const [upiId, setUpiId] = useState("");
  const [amount, setAmount] = useState("");
  const [dateTime, setDateTime] = useState("");
  const [loading, setLoading] = useState(false);
  const [schedules, setSchedules] = useState([]);
  const [editingId, setEditingId] = useState(null);

  // ================= LOAD SCHEDULES =================
  const loadSchedules = async () => {
    try {
      const res = await api.get("/scheduled-payments");
      setSchedules(res.data);
    } catch (err) {
      console.error("Failed to load schedules");
    }
  };

  useEffect(() => {
    loadSchedules();
  }, []);

  // 🔥 Real-time countdown refresh
  useEffect(() => {
    const interval = setInterval(() => {
      setSchedules((prev) => [...prev]);
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  // ================= CREATE / UPDATE =================
  const handleSchedule = async (e) => {
    e.preventDefault();

    if (!upiId || !amount || !dateTime) {
      alert("Fill all fields");
      return;
    }

    try {
      setLoading(true);

      if (editingId) {
        await api.put(`/scheduled-payments/${editingId}`, {
          receiverUpiId: upiId,
          amount: Number(amount),
          scheduledAt: new Date(dateTime).toISOString(),
        });

        alert("✅ Schedule Updated Successfully");
        setEditingId(null);
      } else {
        await api.post("/scheduled-payments", {
          receiverUpiId: upiId,
          amount: Number(amount),
          scheduledAt: new Date(dateTime).toISOString(),
        });

        alert("✅ Payment Scheduled Successfully");
      }

      setUpiId("");
      setAmount("");
      setDateTime("");
      loadSchedules();

    } catch (err) {
      alert(err.response?.data?.message || "Operation failed");
    } finally {
      setLoading(false);
    }
  };

  // ================= CANCEL =================
  const handleCancel = async (id) => {
    if (!window.confirm("Cancel this scheduled payment?")) return;

    try {
      await api.delete(`/scheduled-payments/${id}`);
      alert("Cancelled successfully");
      loadSchedules();
    } catch {
      alert("Cancel failed");
    }
  };

  // ================= EDIT =================
  const handleEdit = (item) => {
    if (item.executed) return;

    setEditingId(item.id);
    setUpiId(item.receiverUpiId);
    setAmount(item.amount);
    setDateTime(new Date(item.scheduledAt).toISOString().slice(0, 16));
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  // ================= COUNTDOWN =================
  const getCountdown = (iso) => {
    const diff = new Date(iso) - new Date();

    if (diff <= 0) return "Executing...";

    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff / (1000 * 60 * 60)) % 24);
    const minutes = Math.floor((diff / (1000 * 60)) % 60);
    const seconds = Math.floor((diff / 1000) % 60);

    return `${days}d ${hours}h ${minutes}m ${seconds}s`;
  };

  const formatDate = (iso) =>
    new Date(iso).toLocaleString();

  return (
    <div style={styles.wrapper}>
      <div style={styles.card}>
        <h2 style={styles.title}>
          {editingId ? "✏️ Edit Scheduled Payment" : "⏳ Schedule Payment"}
        </h2>

        <form onSubmit={handleSchedule} style={styles.form}>
          <input
            type="text"
            placeholder="Recipient UPI ID"
            value={upiId}
            onChange={(e) => setUpiId(e.target.value)}
            style={styles.input}
            required
          />

          <input
            type="number"
            placeholder="Amount (₹)"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            style={styles.input}
            required
          />

          <input
            type="datetime-local"
            value={dateTime}
            onChange={(e) => setDateTime(e.target.value)}
            style={styles.input}
            required
          />

          <button
            type="submit"
            style={styles.button}
            disabled={loading}
          >
            {loading
              ? editingId
                ? "Updating..."
                : "Scheduling..."
              : editingId
              ? "Update Payment"
              : "Schedule Payment"}
          </button>

          {editingId && (
            <button
              type="button"
              onClick={() => {
                setEditingId(null);
                setUpiId("");
                setAmount("");
                setDateTime("");
              }}
              style={styles.cancelEdit}
            >
              Cancel Edit
            </button>
          )}
        </form>
      </div>

      {/* ================= UPCOMING PAYMENTS ================= */}
      <div style={styles.listContainer}>
        <h3>📅 Upcoming Payments</h3>

        {schedules.length === 0 && (
          <p style={styles.empty}>No scheduled payments</p>
        )}

        {schedules.map((item) => (
          <div key={item.id} style={styles.scheduleCard}>
            <div>
              <strong>{item.receiverUpiId}</strong>
              <p>₹ {item.amount}</p>
              <p>{formatDate(item.scheduledAt)}</p>

              {!item.executed && (
                <p style={styles.countdown}>
                  ⏳ {getCountdown(item.scheduledAt)}
                </p>
              )}

              <span style={{
                ...styles.status,
                background:
                  item.status === "SUCCESS"
                    ? "#22c55e"
                    : item.status === "FAILED"
                    ? "#ef4444"
                    : "#f59e0b",
              }}>
                {item.status}
              </span>
            </div>

            {!item.executed && (
              <div style={styles.actionGroup}>
                <button
                  style={styles.editBtn}
                  onClick={() => handleEdit(item)}
                >
                  Edit
                </button>

                <button
                  style={styles.cancelBtn}
                  onClick={() => handleCancel(item.id)}
                >
                  Delete
                </button>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

const styles = {
  wrapper: {
    minHeight: "90vh",
    padding: "2rem",
    background: "linear-gradient(135deg,#f0f4ff,#ffffff)",
  },
  card: {
    maxWidth: "500px",
    margin: "auto",
    background: "#fff",
    padding: "2rem",
    borderRadius: "16px",
    boxShadow: "0 20px 50px rgba(0,0,0,0.1)",
  },
  title: {
    textAlign: "center",
    marginBottom: "1rem",
  },
  form: {
    display: "flex",
    flexDirection: "column",
    gap: "1rem",
  },
  input: {
    padding: "0.7rem",
    borderRadius: "8px",
    border: "1px solid #ccc",
  },
  button: {
    padding: "0.8rem",
    borderRadius: "10px",
    border: "none",
    background: "linear-gradient(90deg,#2563eb,#1e40af)",
    color: "#fff",
    cursor: "pointer",
  },
  cancelEdit: {
    background: "none",
    border: "none",
    color: "#ef4444",
    marginTop: "5px",
    cursor: "pointer",
  },
  listContainer: {
    marginTop: "3rem",
    maxWidth: "900px",
    marginInline: "auto",
  },
  scheduleCard: {
    background: "#fff",
    padding: "1.2rem",
    borderRadius: "12px",
    marginTop: "1rem",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    boxShadow: "0 8px 20px rgba(0,0,0,0.05)",
  },
  status: {
    padding: "4px 10px",
    borderRadius: "20px",
    color: "#fff",
    fontSize: "0.8rem",
  },
  countdown: {
    fontSize: "0.85rem",
    color: "#2563eb",
    marginTop: "5px",
  },
  actionGroup: {
    display: "flex",
    gap: "8px",
  },
  editBtn: {
    background: "#3b82f6",
    color: "#fff",
    border: "none",
    padding: "6px 12px",
    borderRadius: "8px",
    cursor: "pointer",
  },
  cancelBtn: {
    background: "#ef4444",
    color: "#fff",
    border: "none",
    padding: "6px 12px",
    borderRadius: "8px",
    cursor: "pointer",
  },
  empty: {
    marginTop: "1rem",
    color: "#777",
  },
};
