import { useEffect, useState } from "react";
import api from "../../api/axios";
import "./AdminTransactions.css";

export default function AdminTransactions() {
  const [transactions, setTransactions] = useState([]);
  const [search, setSearch] = useState("");
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

  // ================= FILTER LOGIC =================
  const filtered = transactions.filter((tx) => {
    const matchesSearch =
      tx.transactionId.toString().includes(search) ||
      tx.fromUpiId.toLowerCase().includes(search.toLowerCase()) ||
      tx.toUpiId.toLowerCase().includes(search.toLowerCase());

    const matchesStatus =
      statusFilter === "ALL" || tx.status === statusFilter;

    return matchesSearch && matchesStatus;
  });

  return (
    <div className="admin-transactions">
      <h1>All Transactions</h1>

      <div className="filter-bar">
        <input
          type="text"
          placeholder="Search by ID or UPI..."
          className="search-input"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />

        <select
          className="status-filter"
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
        >
          <option value="ALL">All Status</option>
          <option value="SUCCESS">SUCCESS</option>
          <option value="FAILED">FAILED</option>
          <option value="PENDING">PENDING</option>
          <option value="INITIATED">INITIATED</option>
        </select>
      </div>

      <div className="table-container">
        <table>
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
                <td colSpan="6" style={{ textAlign: "center", padding: "20px" }}>
                  No transactions found
                </td>
              </tr>
            ) : (
              filtered.map((tx) => (
                <tr key={tx.transactionId}>
                  <td>{tx.transactionId}</td>
                  <td>{tx.fromUpiId}</td>
                  <td>{tx.toUpiId}</td>
                  <td>₹ {tx.amount}</td>
                  <td>
                    <span className={`badge ${tx.status.toLowerCase()}`}>
                      {tx.status}
                    </span>
                  </td>
                  <td>
                    {new Date(tx.timestamp).toLocaleString()}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
