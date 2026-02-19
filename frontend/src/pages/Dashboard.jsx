import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/axios";
import BalanceCard from "../components/BalanceCard";
import {
  connectBalanceSocket,
  disconnectBalanceSocket,
} from "../websocket/balanceSocket";
import "./Dashboard.css";

const QUICK_ACTIONS = [
  { icon: "⚡", label: "Transfer",  to: "/transfer" },
  { icon: "📅", label: "Schedule",  to: "/scheduled-payments" },
  { icon: "📷", label: "Scan QR",   to: "/scan-qr" },
  { icon: "🪙", label: "My QR",     to: "/my-qr" },
];

export default function Dashboard() {
  const [balance, setBalance]   = useState(0);
  const [walletId, setWalletId] = useState(null);
  const [loading, setLoading]   = useState(true);
  const [recentTxs, setRecentTxs] = useState([]);

  const prevBalance = useRef(0);

  useEffect(() => {
    const loadDashboard = async () => {
      try {
        const balanceRes = await api.get("/wallet/balance");
        setBalance(balanceRes.data.balance);
        setWalletId(balanceRes.data.walletId);
        prevBalance.current = balanceRes.data.balance;

        const txRes = await api.get("/wallet/transactions");
        setRecentTxs(txRes.data.slice(0, 5));
      } catch (err) {
        console.error("Dashboard load failed", err);
      } finally {
        setLoading(false);
      }
    };
    loadDashboard();
  }, []);

  useEffect(() => {
    if (!walletId) return;
    connectBalanceSocket(walletId, (newBalance) => {
      prevBalance.current = balance;
      setBalance(newBalance);
    });
    return () => disconnectBalanceSocket();
  }, [walletId, balance]);

  const totalSent = recentTxs
    .filter((t) => t.type === "DEBIT" && t.status === "SUCCESS")
    .reduce((s, t) => s + t.amount, 0);

  const totalReceived = recentTxs
    .filter((t) => t.type === "CREDIT" && t.status === "SUCCESS")
    .reduce((s, t) => s + t.amount, 0);

  if (loading) {
    return (
      <div className="page-center">
        <div className="dash-skeleton-wrapper">
          <div className="skel skel-balance" />
          <div className="skel skel-stats" />
          <div className="skel skel-card" />
        </div>
      </div>
    );
  }

  return (
    <div className="page-center">
      <div className="dash-wrapper">

        {/* Header */}
        <div className="dash-header fade-up fade-up-1">
          <h1 className="dash-title">Dashboard</h1>
          <p className="dash-subtitle">
            <span className="live-dot" />
            Real-time balance synced via WebSocket
          </p>
        </div>

        {/* Balance Card */}
        <BalanceCard balance={balance} prevBalance={prevBalance.current} />

        {/* Stats */}
        <div className="dash-stats fade-up fade-up-3">
          <div className="dash-stat">
            <div className="dash-stat__value" style={{ color: "var(--green)" }}>
              ₹{totalReceived.toLocaleString("en-IN")}
            </div>
            <div className="dash-stat__label">Received</div>
          </div>
          <div className="dash-stat">
            <div className="dash-stat__value" style={{ color: "var(--red)" }}>
              ₹{totalSent.toLocaleString("en-IN")}
            </div>
            <div className="dash-stat__label">Sent</div>
          </div>
          <div className="dash-stat">
            <div className="dash-stat__value">{recentTxs.length}</div>
            <div className="dash-stat__label">Transactions</div>
          </div>
        </div>

        {/* Quick Actions */}
        <div className="fade-up fade-up-4">
          <div className="dash-section-label">Quick Actions</div>
          <div className="dash-quick-actions">
            {QUICK_ACTIONS.map((a) => (
              <Link key={a.to} to={a.to} className="dash-qa-btn">
                <span className="qa-icon">{a.icon}</span>
                {a.label}
              </Link>
            ))}
          </div>
        </div>

        {/* Recent Transactions */}
        <div className="dash-recent fade-up fade-up-5">
          <div className="dash-recent__heading">🧾 Recent Transactions</div>

          {recentTxs.length === 0 ? (
            <div className="dash-empty">
              <div className="dash-empty__icon">📭</div>
              <p>No recent transactions</p>
            </div>
          ) : (
            recentTxs.map((tx) => {
              const isDebit = tx.type === "DEBIT";
              return (
                <div key={tx.id} className="dash-tx-row">
                  <div className="dash-tx-left">
                    <div className={`dash-tx-icon ${isDebit ? "debit" : "credit"}`}>
                      {isDebit ? "↑" : "↓"}
                    </div>
                    <div className="dash-tx-info">
                      <span className={`dash-tx-badge ${isDebit ? "debit" : "credit"}`}>
                        {tx.type}
                      </span>
                      <span className="dash-tx-date">
                        {new Date(tx.timestamp).toLocaleDateString("en-IN", {
                          day: "2-digit", month: "short",
                          hour: "2-digit", minute: "2-digit",
                        })}
                      </span>
                    </div>
                  </div>
                  <div className={`dash-tx-amount ${isDebit ? "debit" : "credit"}`}>
                    {isDebit ? "−" : "+"}₹{tx.amount.toLocaleString("en-IN")}
                  </div>
                </div>
              );
            })
          )}
        </div>

        {/* Live hint */}
        <div className="dash-hint fade-up">
          <span className="live-dot" />
          Live balance updates enabled
        </div>

      </div>
    </div>
  );
}