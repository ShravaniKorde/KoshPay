import { useEffect, useState, useMemo } from "react";
import api from "../api/axios";
import "./Transactions.css";

const STATUS_DOT = {
  SUCCESS:  "#22c55e",
  FAILED:   "#f87171",
  PENDING:  "#60a5fa",
  INITIATED:"#f5c842",
};

// ── CSV export — pure frontend, no backend needed ─────────────
function exportToCSV(data) {
  if (!data.length) return;
  const headers = ["ID", "Type", "From UPI", "To UPI", "Amount (INR)", "Status", "Date & Time"];
  const rows = data.map((tx) => [
    tx.transactionId,
    tx.type,
    tx.fromUpi,
    tx.toUpi,
    tx.amount,
    tx.status,
    new Date(tx.timestamp).toLocaleString("en-IN"),
  ]);
  const csv  = [headers, ...rows].map((r) => r.map((c) => `"${c}"`).join(",")).join("\n");
  const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
  const url  = URL.createObjectURL(blob);
  const a    = document.createElement("a");
  a.href     = url;
  a.download = `koshpay-transactions-${new Date().toISOString().slice(0,10)}.csv`;
  a.click();
  URL.revokeObjectURL(url);
}

export default function Transactions() {
  const [txs, setTxs]         = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterType,   setFilterType]   = useState("ALL");
  const [filterStatus, setFilterStatus] = useState("ALL");
  const [filterDate,   setFilterDate]   = useState("");

  useEffect(() => {
    const fetchData = async () => {
      try {
        await api.get("/wallet/balance");
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

  const filtered = useMemo(() => txs.filter((tx) => {
    if (filterType   !== "ALL" && tx.type   !== filterType)   return false;
    if (filterStatus !== "ALL" && tx.status !== filterStatus) return false;
    if (filterDate) {
      if (new Date(tx.timestamp).toISOString().slice(0,10) !== filterDate) return false;
    }
    return true;
  }), [txs, filterType, filterStatus, filterDate]);

  const hasFilters = filterType !== "ALL" || filterStatus !== "ALL" || filterDate !== "";
  const clearFilters = () => { setFilterType("ALL"); setFilterStatus("ALL"); setFilterDate(""); };

  if (loading) return <div className="txns-page"><div className="txns-loading">Loading transactions...</div></div>;

  return (
    <div className="txns-page">
      <div className="txns-wrapper">

        {/* Header row with export button */}
        <div className="txns-header">
          <div className="txns-header__left">
            <h2 className="txns-title">Transaction History</h2>
            <p className="txns-subtitle">All incoming and outgoing wallet activity</p>
          </div>
          <button
            className="txns-export-btn"
            onClick={() => exportToCSV(filtered)}
            disabled={filtered.length === 0}
          >
            ↓ Export CSV
          </button>
        </div>

        {/* Filters */}
        <div className="txns-filters">
          <div className="txns-filter-group">
            <label className="txns-filter-label">Type</label>
            <div className="txns-filter-pills">
              {["ALL","CREDIT","DEBIT"].map((v) => (
                <button key={v} className={`txns-pill ${filterType===v?"active":""} ${v.toLowerCase()}`} onClick={() => setFilterType(v)}>
                  {v==="ALL"?"All":v==="CREDIT"?"↓ Credit":"↑ Debit"}
                </button>
              ))}
            </div>
          </div>
          <div className="txns-filter-group">
            <label className="txns-filter-label">Status</label>
            <div className="txns-filter-pills">
              {["ALL","SUCCESS","FAILED","PENDING"].map((v) => (
                <button key={v} className={`txns-pill ${filterStatus===v?"active":""} ${v.toLowerCase()}`} onClick={() => setFilterStatus(v)}>
                  {v==="ALL"?"All":v}
                </button>
              ))}
            </div>
          </div>
          <div className="txns-filter-group">
            <label className="txns-filter-label">Date</label>
            <input type="date" value={filterDate} onChange={(e) => setFilterDate(e.target.value)} className="txns-date-input" />
          </div>
          {hasFilters && <button className="txns-clear-btn" onClick={clearFilters}>✕ Clear</button>}
        </div>

        {/* Table */}
        <div className="txns-table-card">
          <div className="txns-table-header">
            <span className="txns-table-header__label">{hasFilters ? "Filtered Results" : "All Transactions"}</span>
            <span className="txns-count">{filtered.length}{hasFilters && ` of ${txs.length}`} records</span>
          </div>
          {filtered.length === 0 ? (
            <div className="txns-empty">
              <div className="txns-empty__icon">🔍</div>
              <p>No transactions match your filters</p>
              <button className="txns-empty-clear" onClick={clearFilters}>Clear filters</button>
            </div>
          ) : (
            <div className="txns-table-scroll">
              <table className="txns-table">
                <thead>
                  <tr><th>Type</th><th>From</th><th>To</th><th>Amount</th><th>Status</th><th>Date & Time</th></tr>
                </thead>
                <tbody>
                  {filtered.map((tx) => {
                    const isDebit = tx.type === "DEBIT";
                    const statusKey = tx.status || "PENDING";
                    return (
                      <tr key={tx.transactionId}>
                        <td><span className={`txns-type-badge ${isDebit?"debit":"credit"}`}>{isDebit?"↑":"↓"} {tx.type}</span></td>
                        <td className="txns-upi">{tx.fromUpi}</td>
                        <td className="txns-upi">{tx.toUpi}</td>
                        <td className={`txns-amount ${isDebit?"debit":"credit"}`}>{isDebit?"−":"+"}₹{Number(tx.amount).toLocaleString("en-IN")}</td>
                        <td>
                          <span className={`txns-status-badge ${statusKey}`}>
                            <span className="sdot" style={{ background: STATUS_DOT[statusKey]||"#94a3b8" }} />
                            {tx.status}
                          </span>
                        </td>
                        <td className="txns-date">{new Date(tx.timestamp).toLocaleString("en-IN",{day:"2-digit",month:"short",hour:"2-digit",minute:"2-digit"})}</td>
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