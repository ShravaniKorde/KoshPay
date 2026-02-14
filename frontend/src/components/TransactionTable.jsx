import React from 'react';

const TransactionTable = ({ txs }) => {
  if (!txs || txs.length === 0) {
    return <div style={styles.emptyCard}>No transactions found</div>;
  }

  return (
    <div style={styles.card}>
      <table style={styles.table}>
        <thead>
          <tr style={styles.headerRow}>
            <th style={styles.th}>Type</th>
            <th style={styles.th}>From</th>
            <th style={styles.th}>To</th>
            <th style={styles.th}>Amount</th>
            <th style={styles.th}>Status</th>
            <th style={styles.th}>Date & Time</th>
          </tr>
        </thead>
        <tbody>
          {txs.map((tx) => {
            const isDebit = tx.type === 'DEBIT';
            return (
              <tr key={tx.transactionId} style={styles.row}>
                {/* 1. TYPE BADGE */}
                <td>
                  <span style={{
                    ...styles.badge,
                    backgroundColor: isDebit ? '#fee2e2' : '#dcfce7',
                    color: isDebit ? '#b91c1c' : '#166534'
                  }}>
                    {tx.type}
                  </span>
                </td>

                <td style={styles.upiText}>{tx.fromUpi}</td>
                <td style={styles.upiText}>{tx.toUpi}</td>

                {/* 4. AMOUNT */}
                <td style={{ 
                  ...styles.td, 
                  fontWeight: '700', 
                  color: isDebit ? '#dc2626' : '#16a34a' 
                }}>
                  {isDebit ? '-' : '+'}₹{tx.amount.toLocaleString('en-IN')}
                </td>

                {/* 5. STATUS BADGE */}
                <td>
                  <span style={{
                    ...styles.statusBadge,
                    backgroundColor: tx.status === 'SUCCESS' ? '#dcfce7' : '#fee2e2',
                    color: tx.status === 'SUCCESS' ? '#15803d' : '#991b1b',
                  }}>
                    {tx.status}
                  </span>
                </td>

                {/* 6. DATE */}
                <td style={styles.dateText}>
                  {new Date(tx.timestamp).toLocaleString('en-IN', {
                    day: '2-digit',
                    month: 'short',
                    hour: '2-digit',
                    minute: '2-digit'
                  })}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};

/* =====================  STYLES ===================== */
const styles = {
  card: {
    background: '#ffffff',
    borderRadius: '16px',
    padding: '1.5rem',
    boxShadow: '0 10px 25px rgba(0,0,0,0.05)',
    marginTop: '1.5rem',
    overflowX: 'auto'
  },
  emptyCard: {
    padding: '3rem',
    textAlign: 'center',
    background: '#f9fafb',
    borderRadius: '16px',
    color: '#6b7280',
    border: '2px dashed #e5e7eb'
  },
  table: {
    width: '100%',
    borderCollapse: 'separate',
    borderSpacing: '0 8px',
  },
  th: {
    textAlign: 'left',
    padding: '12px 16px',
    color: '#4b5563',
    fontSize: '0.85rem',
    fontWeight: '600',
    textTransform: 'uppercase',
    letterSpacing: '0.05em'
  },
  row: {
    backgroundColor: '#ffffff',
    transition: 'transform 0.2s ease',
  },
  td: {
    padding: '16px',
    fontSize: '0.95rem',
    color: '#111827'
  },
  upiText: {
    padding: '16px',
    fontSize: '0.9rem',
    color: '#374151',
    fontWeight: '500',
    fontFamily: 'monospace'
  },
  badge: {
    padding: '6px 12px',
    borderRadius: '8px',
    fontSize: '0.75rem',
    fontWeight: '800',
  },
  statusBadge: {
    padding: '4px 10px',
    borderRadius: '6px',
    fontSize: '0.7rem',
    fontWeight: '700',
    display: 'inline-block'
  },
  dateText: {
    padding: '16px',
    fontSize: '0.85rem',
    color: '#6b7280',
  }
};

export default TransactionTable;