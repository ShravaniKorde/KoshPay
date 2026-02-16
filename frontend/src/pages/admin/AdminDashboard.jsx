import { useEffect, useState } from "react";
import api from "../../api/axios";
import "./AdminDashboard.css";

export default function AdminDashboard() {
  const [summary, setSummary] = useState(null);
  const [distribution, setDistribution] = useState(null);

  useEffect(() => {
    const loadData = async () => {
      try {
        const summaryRes = await api.get("/admin/summary");
        const distRes = await api.get("/admin/status-distribution");

        setSummary(summaryRes.data);
        setDistribution(distRes.data);
      } catch (err) {
        console.error("Admin dashboard error", err);
      }
    };

    loadData();
  }, []);

  if (!summary || !distribution) {
    return <div className="admin-loading">Loading Admin Dashboard...</div>;
  }

  return (
    <div className="admin-dashboard fade-in">
      <h1 className="admin-title">Admin Control Center</h1>
      <p className="admin-subtitle">
        System analytics and transaction lifecycle monitoring
      </p>

      <div className="admin-card-grid">
        <DashboardCard
          title="Total Transactions"
          value={summary.totalTransactions}
          color="blue"
          delay="0s"
        />

        <DashboardCard
          title="Successful Transactions"
          value={distribution.success}
          color="green"
          delay="0.1s"
        />

        <DashboardCard
          title="Failed Transactions"
          value={distribution.failed}
          color="red"
          delay="0.2s"
        />

        <DashboardCard
          title="Total Volume"
          value={`₹ ${summary.totalVolume}`}
          color="purple"
          delay="0.3s"
        />
      </div>

      <h2 className="admin-section-title">Transaction Lifecycle</h2>

      <div className="admin-card-grid small">
        <DashboardCard
          title="Initiated"
          value={distribution.initiated}
          color="orange"
          delay="0.4s"
        />
        <DashboardCard
          title="Pending"
          value={distribution.pending}
          color="yellow"
          delay="0.5s"
        />
        <DashboardCard
          title="Success"
          value={distribution.success}
          color="green"
          delay="0.6s"
        />
        <DashboardCard
          title="Failed"
          value={distribution.failed}
          color="red"
          delay="0.7s"
        />
      </div>
    </div>
  );
}

/* ============================= */
/* Card Component */
/* ============================= */

function DashboardCard({ title, value, color, delay }) {
  return (
    <div
      className={`admin-card ${color} card-animate`}
      style={{ animationDelay: delay }}
    >
      <h3>{title}</h3>
      <p className="value-pop">{value}</p>
    </div>
  );
}
