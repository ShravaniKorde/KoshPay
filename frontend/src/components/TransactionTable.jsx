import React from 'react';
import './TransactionTable.css';

const STATUS_DOT = {
  SUCCESS:  '#22c55e',
  FAILED:   '#f87171',
  PENDING:  '#60a5fa',
  INITIATED:'#f5c842',
};

const TransactionTable = ({ txs }) => {
  if (!txs || txs.length === 0) {
    return (
      <div className="tt-empty">
        <div className="tt-empty__icon">📭</div>
        <p className="tt-empty__text">No transactions found</p>
      </div>
    );
  }

  return (
    <div className="tt-card">
      <div className="tt-scroll">
        <table className="tt-table">
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
            {txs.map((tx) => {
              const isDebit   = tx.type === 'DEBIT';
              const statusKey = tx.status || 'PENDING';

              return (
                <tr key={tx.transactionId}>

                  {/* TYPE */}
                  <td>
                    <span className={`tt-type-badge ${isDebit ? 'debit' : 'credit'}`}>
                      {isDebit ? '↑' : '↓'} {tx.type}
                    </span>
                  </td>

                  {/* FROM */}
                  <td className="tt-upi">{tx.fromUpi}</td>

                  {/* TO */}
                  <td className="tt-upi">{tx.toUpi}</td>

                  {/* AMOUNT */}
                  <td className={`tt-amount ${isDebit ? 'debit' : 'credit'}`}>
                    {isDebit ? '−' : '+'}₹{tx.amount.toLocaleString('en-IN')}
                  </td>

                  {/* STATUS */}
                  <td>
                    <span className={`tt-status-badge ${statusKey}`}>
                      <span
                        className="tt-status-dot"
                        style={{ background: STATUS_DOT[statusKey] || '#94a3b8' }}
                      />
                      {tx.status}
                    </span>
                  </td>

                  {/* DATE */}
                  <td className="tt-date">
                    {new Date(tx.timestamp).toLocaleString('en-IN', {
                      day: '2-digit',
                      month: 'short',
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </td>

                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default TransactionTable;