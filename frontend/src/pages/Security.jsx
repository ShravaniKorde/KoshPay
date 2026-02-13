import React from "react";
import UpdatePin from "../components/UpdatePin";

export default function Security() {
  return (
    <div className="page-center">
      <div style={styles.container}>
        <h1 className="page-title">Security Settings</h1>
        <p className="page-subtitle">Manage your credentials and transaction PIN</p>

        <div style={styles.content}>
          {/* THE PIN UPDATE CARD */}
          <UpdatePin />

          {/* ADDITIONAL SECURITY INFO */}
          <div style={styles.infoCard}>
            <h4 style={{ margin: "0 0 10px 0" }}>🔒 Safe Banking Tips</h4>
            <ul style={styles.list}>
              <li>Choose a PIN that isn't easy to guess (avoid 1234 or your birth year).</li>
              <li>Your PIN is required for every transfer for your protection.</li>
              <li>We will never ask for your PIN over email or phone call.</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: { maxWidth: "800px", width: "100%", textAlign: "center" },
  content: { display: "flex", flexDirection: "column", alignItems: "center", gap: "2rem", marginTop: "2rem" },
  infoCard: {
    background: "#f1f5f9",
    padding: "1.5rem",
    borderRadius: "16px",
    textAlign: "left",
    fontSize: "0.9rem",
    color: "#475569",
    border: "1px solid #e2e8f0",
    maxWidth: "400px"
  },
  list: { paddingLeft: "1.2rem", lineHeight: "1.6" }
};