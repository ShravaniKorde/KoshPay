import { useEffect, useState, useMemo } from "react";
import api from "../api/axios";
import "./Transactions.css";

const STATUS_DOT = {
  SUCCESS:  "#22c55e",
  FAILED:   "#f87171",
  PENDING:  "#60a5fa",
  INITIATED:"#f5c842",
};

export default function Transactions() {
  const [txs, setTxs]         = useState([]);
  const [loading, setLoading] = useState(true);

  // ── Filter state ──────────────────────────────────────────
  const [filterType,   setFilterType]   = useState("ALL");   // ALL | DEBIT | CREDIT
  const [filterStatus, setFilterStatus] = useState("ALL");   // ALL | SUCCESS | FAILED | PENDING
  const [filterDate,   setFilterDate]   = useState("");      // YYYY-MM-DD or ""

  useEffect(() => {
    const fetchData = async () => {
      try {
        await api.get("/wallet/balance");           // keep walletId fetch as before
        const txRes = await api.get("/wallet/transactions");
        setTxs(txRes.data);
      } catch (err) {
        console.error("Failed to load transactions", err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  // ── Client-side filtering (no backend changes) ────────────
  const filtered = useMemo(() => {
    return txs.filter((tx) => {
      if (filterType   !== "ALL" && tx.type   !== filterType)   return false;
      if (filterStatus !== "ALL" && tx.status !== filterStatus) return false;
      if (filterDate) {
        const txDate = new Date(tx.timestamp).toISOString().slice(0, 10);
        if (txDate !== filterDate) return false;
      }
      return true;
    });
  }, [txs, filterType, filterStatus, filterDate]);

  const hasFilters = filterType !== "ALL" || filterStatus !== "ALL" || filterDate !== "";

  const clearFilters = () => {
    setFilterType("ALL");
    setFilterStatus("ALL");
    setFilterDate("");
  };

  if (loading) {
    return (
      <div className="txns-page">
        <div className="txns-loading">Loading transactions...</div>
      </div>
    );
  }

  return (
    <div className="txns-page">
      <div className="txns-wrapper">

        <div className="txns-header">
          <h2 className="txns-title">Transaction History</h2>
          <p className="txns-subtitle">All incoming and outgoing wallet activity</p>
        </div>

        {/* ── Filter bar ──────────────────────────────────── */}
        <div className="txns-filters">
          {/* Type */}
          <div className="txns-filter-group">
            <label className="txns-filter-label">Type</label>
            <div className="txns-filter-pills">
              {["ALL", "CREDIT", "DEBIT"].map((v) => (
                <button
                  key={v}
                  className={`txns-pill ${filterType === v ? "active" : ""} ${v.toLowerCase()}`}
                  onClick={() => setFilterType(v)}
                >
                  {v === "ALL" ? "All" : v === "CREDIT" ? "↓ Credit" : "↑ Debit"}
                </button>
              ))}
            </div>
          </div>

          {/* Status */}
          <div className="txns-filter-group">
            <label className="txns-filter-label">Status</label>
            <div className="txns-filter-pills">
              {["ALL", "SUCCESS", "FAILED", "PENDING"].map((v) => (
                <button
                  key={v}
                  className={`txns-pill ${filterStatus === v ? "active" : ""} ${v.toLowerCase()}`}
                  onClick={() => setFilterStatus(v)}
                >
                  {v === "ALL" ? "All" : v}
                </button>
              ))}
            </div>
          </div>

          {/* Date */}
          <div className="txns-filter-group">
            <label className="txns-filter-label">Date</label>
            <input
              type="date"
              value={filterDate}
              onChange={(e) => setFilterDate(e.target.value)}
              className="txns-date-input"
            />
          </div>

          {/* Clear */}
          {hasFilters && (
            <button className="txns-clear-btn" onClick={clearFilters}>
              ✕ Clear
            </button>
          )}
        </div>

        {/* ── Result count ─────────────────────────────────── */}
        <div className="txns-table-card">
          <div className="txns-table-header">
            <span className="txns-table-header__label">
              {hasFilters ? "Filtered Results" : "All Transactions"}
            </span>
            <span className="txns-count">
              {filtered.length}{hasFilters && ` of ${txs.length}`} records
            </span>
          </div>

          {filtered.length === 0 ? (
            <div className="txns-empty">
              <div className="txns-empty__icon">🔍</div>
              <p>No transactions match your filters</p>
              <button className="txns-empty-clear" onClick={clearFilters}>
                Clear filters
              </button>
            </div>
          ) : (
            <div className="txns-table-scroll">
              <table className="txns-table">
                <thead>
                  <tr>
                    <th>Type</th>
                    <th>From</th>
                    <th>To</th>
                    <th>Amount</th>
                    <th>Status</th>
                    <th>Date & Time</th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.map((tx) => {
                    const isDebit   = tx.type === "DEBIT";
                    const statusKey = tx.status || "PENDING";
                    return (
                      <tr key={tx.transactionId}>
                        <td>
                          <span className={`txns-type-badge ${isDebit ? "debit" : "credit"}`}>
                            {isDebit ? "↑" : "↓"} {tx.type}
                          </span>
                        </td>
                        <td className="txns-upi">{tx.fromUpi}</td>
                        <td className="txns-upi">{tx.toUpi}</td>
                        <td className={`txns-amount ${isDebit ? "debit" : "credit"}`}>
                          {isDebit ? "−" : "+"}₹{tx.amount.toLocaleString("en-IN")}
                        </td>
                        <td>
                          <span className={`txns-status-badge ${statusKey}`}>
                            <span className="sdot" style={{ background: STATUS_DOT[statusKey] || "#94a3b8" }} />
                            {tx.status}
                          </span>
                        </td>
                        <td className="txns-date">
                          {new Date(tx.timestamp).toLocaleString("en-IN", {
                            day: "2-digit", month: "short",
                            hour: "2-digit", minute: "2-digit",
                          })}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>

      </div>
    </div>
  );
}