import { useEffect, useState } from "react";
import api from "../../api/axios";
import "./AdminTransactions.css";

const STATUS_DOT = {
  SUCCESS:  "#22c55e",
  FAILED:   "#f87171",
  PENDING:  "#60a5fa",
  INITIATED:"#f5c842",
};

export default function AdminTransactions() {
  const [transactions, setTransactions] = useState([]);
  const [search, setSearch]             = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");

  useEffect(() => {
    const loadTransactions = async () => {
      try {
        const res = await api.get("/admin/transactions");
        setTransactions(res.data);
      } catch (err) {
        console.error("Error loading transactions", err);
      }
    };
    loadTransactions();
  }, []);

  const filtered = transactions.filter((tx) => {
    const matchesSearch =
      tx.transactionId.toString().includes(search) ||
      tx.fromUpiId.toLowerCase().includes(search.toLowerCase()) ||
      tx.toUpiId.toLowerCase().includes(search.toLowerCase());

    const matchesStatus = statusFilter === "ALL" || tx.status === statusFilter;

    return matchesSearch && matchesStatus;
  });

  return (
    <div className="atx-page">
      <div>
        <h1 className="atx-title">All Transactions</h1>
        <p className="atx-subtitle">Full transaction log across all wallets</p>
      </div>

      {/* Filter bar */}
      <div className="atx-filters">
        <input
          type="text"
          placeholder="🔍  Search by ID or UPI..."
          className="atx-search"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />

        <select
          className="atx-select"
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
        >
          <option value="ALL">All Status</option>
          <option value="SUCCESS">SUCCESS</option>
          <option value="FAILED">FAILED</option>
          <option value="PENDING">PENDING</option>
          <option value="INITIATED">INITIATED</option>
        </select>

        <span className="atx-count">
          {filtered.length} / {transactions.length} records
        </span>
      </div>

      {/* Table */}
      <div className="atx-table-card">
        <div className="atx-table-scroll">
          <table className="atx-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>From UPI</th>
                <th>To UPI</th>
                <th>Amount</th>
                <th>Status</th>
                <th>Timestamp</th>
              </tr>
            </thead>
            <tbody>
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan="6">
                    <div className="atx-empty">
                      <div className="atx-empty__icon">📭</div>
                      No transactions found
                    </div>
                  </td>
                </tr>
              ) : (
                filtered.map((tx) => (
                  <tr key={tx.transactionId}>
                    <td className="atx-id">#{tx.transactionId}</td>
                    <td className="atx-upi">{tx.fromUpiId}</td>
                    <td className="atx-upi">{tx.toUpiId}</td>
                    <td className="atx-amount">₹{tx.amount.toLocaleString("en-IN")}</td>
                    <td>
                      <span className={`atx-badge ${tx.status.toLowerCase()}`}>
                        <span className="dot" style={{ background: STATUS_DOT[tx.status] }} />
                        {tx.status}
                      </span>
                    </td>
                    <td className="atx-date">
                      {new Date(tx.timestamp).toLocaleString("en-IN", {
                        day: "2-digit", month: "short",
                        hour: "2-digit", minute: "2-digit",
                      })}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}