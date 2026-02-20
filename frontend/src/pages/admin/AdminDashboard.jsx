import { useEffect, useState } from "react";
import api from "../../api/axios";
import "./AdminDashboard.css";

const CARDS_MAIN = [
  { key: "totalTransactions", label: "Total Transactions", icon: "🔢", color: "blue" },
  { key: "success",           label: "Successful",         icon: "✅", color: "green" },
  { key: "failed",            label: "Failed",             icon: "❌", color: "red" },
  { key: "totalVolume",       label: "Total Volume",       icon: "💰", color: "gold", prefix: "₹" },
];

const CARDS_LIFECYCLE = [
  { key: "initiated", label: "Initiated", icon: "🚀", color: "blue"   },
  { key: "pending",   label: "Pending",   icon: "⏳", color: "yellow" },
  { key: "success",   label: "Success",   icon: "✓",  color: "green"  },
  { key: "failed",    label: "Failed",    icon: "✕",  color: "red"    },
];

export default function AdminDashboard() {
  const [summary, setSummary]           = useState(null);
  const [distribution, setDistribution] = useState(null);

  useEffect(() => {
    const loadData = async () => {
      try {
        const [summaryRes, distRes] = await Promise.all([
          api.get("/admin/summary"),
          api.get("/admin/status-distribution"),
        ]);
        setSummary(summaryRes.data);
        setDistribution(distRes.data);
      } catch (err) {
        console.error("Admin dashboard error", err);
      }
    };
    loadData();
  }, []);

  if (!summary || !distribution) {
    return (
      <div className="adash-loading">
        <div className="adash-spinner" />
        Loading Admin Dashboard...
      </div>
    );
  }

  // Merge for easy lookup
  const data = { ...summary, ...distribution };

  return (
    <div className="adash-page">

      <div className="adash-header">
        <h1 className="adash-title">Admin Control Center</h1>
        <p className="adash-subtitle">System analytics and transaction lifecycle monitoring</p>
      </div>

      {/* ── Main KPI cards ─────────────────────────────────── */}
      <div>
        <div className="adash-section">Key Metrics</div>
        <div className="adash-grid">
          {CARDS_MAIN.map(({ key, label, icon, color, prefix }, i) => (
            <div
              key={key}
              className={`adash-card ${color}`}
              style={{ animationDelay: `${i * 0.07}s` }}
            >
              <span className="adash-card__icon">{icon}</span>
              <div className="adash-card__label">{label}</div>
              <div className="adash-card__value">
                {prefix}{data[key] ?? 0}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* ── Lifecycle cards ────────────────────────────────── */}
      <div>
        <div className="adash-section">Transaction Lifecycle</div>
        <div className="adash-grid small">
          {CARDS_LIFECYCLE.map(({ key, label, icon, color }, i) => (
            <div
              key={key}
              className={`adash-card ${color}`}
              style={{ animationDelay: `${(i + 4) * 0.07}s` }}
            >
              <span className="adash-card__icon">{icon}</span>
              <div className="adash-card__label">{label}</div>
              <div className="adash-card__value">{distribution[key] ?? 0}</div>
            </div>
          ))}
        </div>
      </div>

    </div>
  );
}