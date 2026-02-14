import { useEffect, useState } from "react";
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Legend
} from "recharts";
import api from "../../api/axios";
import "./Analytics.css";

export default function Analytics() {
  const [summary, setSummary] = useState(null);
  const [distribution, setDistribution] = useState(null);

  useEffect(() => {
    const loadAnalytics = async () => {
      try {
        const summaryRes = await api.get("/admin/summary");
        const distRes = await api.get("/admin/status-distribution");

        setSummary(summaryRes.data);
        setDistribution(distRes.data);
      } catch (err) {
        console.error("Analytics load failed", err);
      }
    };

    loadAnalytics();
  }, []);

  if (!summary || !distribution) {
    return <div className="analytics">Loading analytics...</div>;
  }

  const chartData = [
    { name: "Success", value: distribution.success },
    { name: "Failed", value: distribution.failed },
    { name: "Pending", value: distribution.pending },
    { name: "Initiated", value: distribution.initiated }
  ];

  const COLORS = ["#22c55e", "#ef4444", "#eab308", "#3b82f6"];

  const successRate =
    summary.totalTransactions === 0
      ? 0
      : (
          (distribution.success / summary.totalTransactions) *
          100
        ).toFixed(1);

  return (
    <div className="analytics">
      <h1>Platform Analytics</h1>

      {/* ================= SUMMARY CARDS ================= */}
        <div className="cards">
          <div className="card">
            <h3>Total Transactions</h3>
            <p>{summary.totalTransactions}</p>
          </div>

          <div className="card">
            <h3>Successful Volume</h3>
            <p>₹ {summary.totalVolume}</p>
          </div>

          <div className="card">
            <h3>Success Rate</h3>
            <p>{successRate}%</p>
          </div>
        </div>


      {/* ================= PIE CHART ================= */}
      <div className="chart-container">
        <h2>Status Distribution</h2>
        <ResponsiveContainer width="100%" height={320}>
          <PieChart>
            <Pie
              data={chartData}
              cx="50%"
              cy="50%"
              outerRadius={110}
              dataKey="value"
              label
            >
              {chartData.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={COLORS[index]} />
              ))}
            </Pie>
            <Tooltip />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      </div>

      {/* ================= BAR CHART ================= */}
      <div className="chart-container">
        <h2>Lifecycle Breakdown</h2>
        <ResponsiveContainer width="100%" height={320}>
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" />
            <XAxis dataKey="name" stroke="#cbd5e1" />
            <YAxis stroke="#cbd5e1" />
            <Tooltip />
            <Legend />
            <Bar dataKey="value" fill="#3b82f6" radius={[8, 8, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
