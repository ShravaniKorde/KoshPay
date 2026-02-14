import { useEffect, useState } from "react";
import api from "../../api/axios";
import "./AuditLogs.css";

export default function AuditLogs() {
  const [logs, setLogs] = useState([]);
  const [searchUser, setSearchUser] = useState("");
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
    const matchesUser = log.username
      ?.toLowerCase()
      .includes(searchUser.toLowerCase());

    const matchesAction = actionFilter
      ? log.actionType === actionFilter
      : true;

    return matchesUser && matchesAction;
  });

  return (
    <div className="audit-logs">
      <h1>Audit Logs</h1>

      <div className="filters">
        <input
          type="text"
          placeholder="Search by username..."
          value={searchUser}
          onChange={(e) => setSearchUser(e.target.value)}
        />

        <select
          value={actionFilter}
          onChange={(e) => setActionFilter(e.target.value)}
        >
          <option value="">All Actions</option>
          <option value="LOGIN">LOGIN</option>
          <option value="TRANSFER">TRANSFER</option>
        </select>
      </div>

      <div className="table-container">
        <table>
          <thead>
            <tr>
              <th>User</th>
              <th>Action</th>
              <th>Status</th>
              <th>Old Balance</th>
              <th>New Balance</th>
              <th>Timestamp</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((log) => (
              <tr key={log.id}>
                <td>{log.username}</td>
                <td>{log.actionType}</td>
                <td>
                  <span className={`badge ${log.status.toLowerCase()}`}>
                    {log.status}
                  </span>
                </td>
                <td>{log.oldBalance ?? "-"}</td>
                <td>{log.newBalance ?? "-"}</td>
                <td>
                  {new Date(log.timestamp).toLocaleString()}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
