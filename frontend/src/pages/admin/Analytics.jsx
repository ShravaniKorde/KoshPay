import { useEffect, useState } from "react";
import {
  PieChart, Pie, Cell, Tooltip, ResponsiveContainer,
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Legend,
} from "recharts";
import api from "../../api/axios";
import { useAuth } from "../../auth/AuthContext";
import "./Analytics.css";

const COLORS      = ["#22c55e", "#f87171", "#eab308", "#60a5fa"];
const CHART_STYLE = { fontSize: "0.78rem", fontFamily: "Sora, sans-serif" };

const DarkTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div style={{
      background: "#111827",
      border: "1px solid rgba(255,255,255,0.1)",
      borderRadius: "10px",
      padding: "0.65rem 0.9rem",
      fontSize: "0.8rem",
      color: "#f1f5f9",
      boxShadow: "0 8px 24px rgba(0,0,0,0.4)",
    }}>
      {label && <div style={{ color: "#94a3b8", marginBottom: "0.25rem", fontSize: "0.72rem" }}>{label}</div>}
      {payload.map((p, i) => (
        <div key={i} style={{ color: p.fill || p.color || "#f5c842", fontWeight: 700 }}>
          {p.name}: {p.value}
        </div>
      ))}
    </div>
  );
};

export default function Analytics() {
  const { adminRole } = useAuth();
  const isSuperAdmin  = adminRole === "ROLE_SUPER_ADMIN";

  const [summary, setSummary]           = useState(null);
  const [distribution, setDistribution] = useState(null);
  const [error, setError]               = useState(false);

  useEffect(() => {
    const loadAnalytics = async () => {
      try {
        if (isSuperAdmin) {
          // Super admin gets everything
          const [summaryRes, distRes] = await Promise.all([
            api.get("/admin/summary"),
            api.get("/admin/status-distribution"),
          ]);
          setSummary(summaryRes.data);
          setDistribution(distRes.data);
        } else {
          // Analytics admin only gets status-distribution (no summary access)
          const distRes = await api.get("/admin/status-distribution");
          setDistribution(distRes.data);
          // Build a partial summary from distribution data
          const d = distRes.data;
          const total = (d.success || 0) + (d.failed || 0) + (d.pending || 0) + (d.initiated || 0);
          setSummary({ totalTransactions: total, totalVolume: null });
        }
      } catch (err) {
        console.error("Analytics load failed", err);
        setError(true);
      }
    };
    loadAnalytics();
  }, [isSuperAdmin]);

  if (error) {
    return (
      <div className="an-loading" style={{ color: "#ef4444" }}>
        ⚠️ Failed to load analytics data.
      </div>
    );
  }

  if (!summary || !distribution) {
    return (
      <div className="an-loading">
        <div className="an-spinner" />
        Loading analytics...
      </div>
    );
  }

  const chartData = [
    { name: "Success",   value: distribution.success   || 0 },
    { name: "Failed",    value: distribution.failed    || 0 },
    { name: "Pending",   value: distribution.pending   || 0 },
    { name: "Initiated", value: distribution.initiated || 0 },
  ];

  const successRate = summary.totalTransactions === 0
    ? "0.0"
    : ((distribution.success / summary.totalTransactions) * 100).toFixed(1);

  return (
    <div className="an-page">

      <div>
        <h1 className="an-title">Platform Analytics</h1>
        <p className="an-subtitle">Real-time transaction metrics and status breakdown</p>
      </div>

      {/* Summary cards */}
      <div className="an-cards">
        <div className="an-card" style={{ animationDelay: "0s" }}>
          <span className="an-card__icon">🔢</span>
          <div className="an-card__label">Total Transactions</div>
          <div className="an-card__value">{summary.totalTransactions}</div>
        </div>

        {/* Volume only visible to super admin — analytics admin doesn't have access */}
        {isSuperAdmin && (
          <div className="an-card" style={{ animationDelay: "0.07s" }}>
            <span className="an-card__icon">💰</span>
            <div className="an-card__label">Successful Volume</div>
            <div className="an-card__value">
              ₹{Number(summary.totalVolume).toLocaleString("en-IN")}
            </div>
          </div>
        )}

        <div className="an-card" style={{ animationDelay: "0.14s" }}>
          <span className="an-card__icon">📈</span>
          <div className="an-card__label">Success Rate</div>
          <div className="an-card__value">{successRate}%</div>
        </div>
      </div>

      {/* Charts */}
      <div className="an-charts">

        <div className="an-chart-card">
          <div className="an-chart-card__title">🥧 Status Distribution</div>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={chartData}
                cx="50%"
                cy="50%"
                outerRadius={100}
                innerRadius={45}
                dataKey="value"
                paddingAngle={3}
              >
                {chartData.map((_, i) => (
                  <Cell key={i} fill={COLORS[i]} stroke="none" />
                ))}
              </Pie>
              <Tooltip content={<DarkTooltip />} />
              <Legend
                iconType="circle"
                iconSize={8}
                wrapperStyle={{ ...CHART_STYLE, color: "#94a3b8" }}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="an-chart-card">
          <div className="an-chart-card__title">📊 Lifecycle Breakdown</div>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={chartData} barCategoryGap="35%">
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
              <XAxis dataKey="name" stroke="#475569" tick={{ ...CHART_STYLE, fill: "#94a3b8" }} />
              <YAxis stroke="#475569" tick={{ ...CHART_STYLE, fill: "#94a3b8" }} />
              <Tooltip content={<DarkTooltip />} />
              <Bar dataKey="value" radius={[6, 6, 0, 0]}>
                {chartData.map((_, i) => (
                  <Cell key={i} fill={COLORS[i]} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>

      </div>
    </div>
  );
}