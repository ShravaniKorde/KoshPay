import { useEffect, useRef, useState, useCallback } from "react";
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

const safeNum = (val) => {
  const n = Number(val);
  return isNaN(n) ? 0 : n;
};

export default function Dashboard() {
  const [balance, setBalance]     = useState(null); // FIX 1: null instead of 0 — avoids flashing ₹0 before data loads
  const [walletId, setWalletId]   = useState(null);
  const [loading, setLoading]     = useState(true);
  const [recentTxs, setRecentTxs] = useState([]);

  const prevBalance = useRef(0);
  const balanceRef  = useRef(0);

  // FIX 2: fetchTransactions accepts an optional delay (ms)
  // After a WebSocket balance update, the backend may not have committed
  // the transaction record yet. We wait 1.5s before fetching so the DB
  // write finishes and we actually see the new transaction row.
  const fetchTransactions = useCallback(async (delayMs = 0) => {
    try {
      if (delayMs > 0) await new Promise((r) => setTimeout(r, delayMs));
      const txRes = await api.get("/wallet/transactions");
      setRecentTxs(txRes.data.slice(0, 5));
    } catch (err) {
      console.error("Failed to refresh transactions", err);
    }
  }, []);

  // Initial load
  useEffect(() => {
    const loadDashboard = async () => {
      try {
        const balanceRes = await api.get("/wallet/balance");
        const bal = safeNum(balanceRes.data.balance);
        prevBalance.current = bal;
        balanceRef.current  = bal;
        setBalance(bal);
        setWalletId(balanceRes.data.walletId);
        await fetchTransactions();
      } catch (err) {
        console.error("Dashboard load failed", err);
      } finally {
        setLoading(false);
      }
    };
    loadDashboard();
  }, [fetchTransactions]);

  // Poll every 30s — catches scheduled payment executions
  useEffect(() => {
    const interval = setInterval(() => fetchTransactions(), 30000);
    return () => clearInterval(interval);
  }, [fetchTransactions]);

  // WebSocket — only depends on walletId
  useEffect(() => {
    if (!walletId) return;

    connectBalanceSocket(walletId, (newBalance) => {
      const bal = safeNum(newBalance);
      prevBalance.current = balanceRef.current;
      balanceRef.current  = bal;
      setBalance(bal);
      // FIX 2 in action: delay 1500ms so backend finishes writing the tx row
      fetchTransactions(1500);
    });

    return () => disconnectBalanceSocket();
  }, [walletId, fetchTransactions]);

  // Derived stats — guard against null balance during first load
  const displayBalance = balance ?? 0;

  const totalReceived = recentTxs
    .filter((t) => t.type === "CREDIT" && t.status === "SUCCESS")
    .reduce((s, t) => s + safeNum(t.amount), 0);

  const totalSent = recentTxs
    .filter((t) => t.type === "DEBIT" && t.status === "SUCCESS")
    .reduce((s, t) => s + safeNum(t.amount), 0);

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

        <div className="dash-header fade-up fade-up-1">
          <h1 className="dash-title">Dashboard</h1>
          <p className="dash-subtitle">
            <span className="live-dot" />
            Real-Time Balance
          </p>
        </div>

        {/* FIX 1: pass displayBalance so BalanceCard never gets null */}
        <BalanceCard balance={displayBalance} prevBalance={prevBalance.current} />

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
                    {isDebit ? "−" : "+"}₹{safeNum(tx.amount).toLocaleString("en-IN")}
                  </div>
                </div>
              );
            })
          )}
        </div>

        <div className="dash-hint fade-up">
          <span className="live-dot" />
          Live · auto-refreshes every 30s
        </div>

      </div>
    </div>
  );
}