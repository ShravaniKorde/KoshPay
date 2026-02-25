import { useEffect, useState } from "react";
import api from "../../api/axios";
import "./AuditLogs.css";

const STATUS_DOT = {
  SUCCESS: "#22c55e",
  FAILED:  "#f87171",
  PENDING: "#60a5fa",
};

export default function AuditLogs() {
  const [logs, setLogs]                 = useState([]);
  const [searchUser, setSearchUser]     = useState("");
  const [actionFilter, setActionFilter] = useState("");

  useEffect(() => {
    const loadLogs = async () => {
      try {
        const res = await api.get("/admin/audit-logs");
        setLogs(res.data);
      } catch (err) {
        console.error("Error loading audit logs", err);
      }
    };
    loadLogs();
  }, []);

  const filtered = logs.filter((log) => {
    const matchesUser   = log.username?.toLowerCase().includes(searchUser.toLowerCase());
    const matchesAction = actionFilter ? log.actionType === actionFilter : true;
    return matchesUser && matchesAction;
  });

  return (
    <div className="al-page">
      <div>
        <h1 className="al-title">Audit Logs</h1>
        <p className="al-subtitle">Full record of user actions and system events</p>
      </div>

      {/* Filters */}
      <div className="al-filters">
        <input
          type="text"
          placeholder="🔍  Search by username..."
          value={searchUser}
          onChange={(e) => setSearchUser(e.target.value)}
          className="al-search"
        />

        <select
          value={actionFilter}
          onChange={(e) => setActionFilter(e.target.value)}
          className="al-select"
        >
          <option value="">All Actions</option>
          <option value="LOGIN">LOGIN</option>
          <option value="TRANSFER">TRANSFER</option>
        </select>

        <span className="al-count">
          {filtered.length} / {logs.length} records
        </span>
      </div>

      {/* Table */}
      <div className="al-table-card">
        <div className="al-table-scroll">
          <table className="al-table">
            <thead>
              <tr>
                <th>User</th>
                <th>Action</th>
                <th>Status</th>
                <th>Old Balance</th>
                <th>New Balance</th>
                <th>Timestamp (IST)</th>
              </tr>
            </thead>
            <tbody>
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan="6">
                    <div className="al-empty">
                      <div className="al-empty__icon">📋</div>
                      No audit logs found
                    </div>
                  </td>
                </tr>
              ) : (
                filtered.map((log) => (
                  <tr key={log.id}>
                    <td className="al-user">{log.username}</td>
                    <td>
                      <span className="al-action">{log.actionType}</span>
                    </td>
                    <td>
                      <span className={`al-badge ${log.status.toLowerCase()}`}>
                        <span
                          className="dot"
                          style={{ background: STATUS_DOT[log.status] || "#94a3b8" }}
                        />
                        {log.status}
                      </span>
                    </td>
                    <td className="al-balance">
                      {log.oldBalance != null
                        ? `₹${Number(log.oldBalance).toLocaleString("en-IN")}`
                        : "—"}
                    </td>
                    <td className="al-balance">
                      {log.newBalance != null
                        ? `₹${Number(log.newBalance).toLocaleString("en-IN")}`
                        : "—"}
                    </td>
                    <td className="al-date">
                      {log.timestamp
                      ? new Date(log.timestamp).toLocaleString("en-IN", { 
                      day: "2-digit",
                      month: "short",
                      hour: "2-digit",
                      minute: "2-digit",
                      hour12: true,
                      timeZone: "Asia/Kolkata"
                      })
                      : "—"}
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