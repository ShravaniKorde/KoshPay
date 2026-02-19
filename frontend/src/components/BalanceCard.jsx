import { useEffect, useState } from "react";
import "./BalanceCard.css";

export default function BalanceCard({ balance, prevBalance }) {
  const [displayBalance, setDisplayBalance] = useState(balance);
  const isIncrease = balance >= prevBalance;
  const diff = Math.abs(balance - prevBalance);

  // Animated counter with ease-out cubic
  useEffect(() => {
    const start    = displayBalance;
    const end      = balance;
    const duration = 600;
    let startTime  = null;

    const easeOut = (t) => 1 - Math.pow(1 - t, 3);

    function animate(ts) {
      if (!startTime) startTime = ts;
      const progress = Math.min((ts - startTime) / duration, 1);
      setDisplayBalance(Math.floor(start + (end - start) * easeOut(progress)));
      if (progress < 1) requestAnimationFrame(animate);
    }

    requestAnimationFrame(animate);
  }, [balance]);

  return (
    <div className={`bc-card fade-up fade-up-2 ${isIncrease ? "up pulse-green" : "down pulse-red"}`}>
      <div className="bc-inner">

        {/* Live chip */}
        <div className="bc-chip">
          <span className="bc-chip__dot" />
          LIVE BALANCE
        </div>

        {/* Label */}
        <div className="bc-label">Wallet Balance</div>

        {/* Amount */}
        <div className="bc-amount">
          <span className="bc-amount__currency">₹</span>
          {displayBalance.toLocaleString("en-IN")}
        </div>

        {/* Footer */}
        <div className="bc-footer">
          {prevBalance !== balance ? (
            <div className={`bc-change ${isIncrease ? "up" : "down"}`}>
              <span>{isIncrease ? "▲" : "▼"}</span>
              <span>₹{diff.toLocaleString("en-IN")} {isIncrease ? "credited" : "debited"}</span>
            </div>
          ) : (
            <div className="bc-change neutral">— No recent change</div>
          )}

          <div className="bc-tag">KoshPay Wallet</div>
        </div>

      </div>
    </div>
  );
}