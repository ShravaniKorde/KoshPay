import React from "react";
import UpdatePin from "../components/UpdatePin";
import "./Security.css";

export default function Security() {
  return (
    <div className="security-page">
      <div className="security-wrapper">
        <h1 className="security-title">Security Settings</h1>
        <p className="security-subtitle">Manage your credentials and transaction PIN</p>

        <div className="security-content">
          <UpdatePin />

          <div className="security-info-card">
            <div className="security-info-card__title">🔒 Safe Banking Tips</div>
            <ul>
              <li>Choose a PIN that isn't easy to guess — avoid 1234 or your birth year.</li>
              <li>Your PIN is required for every transfer for your protection.</li>
              <li>We will never ask for your PIN over email or phone.</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}