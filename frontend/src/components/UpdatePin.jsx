import React, { useState } from 'react';
import axios from 'axios';
import './UpdatePin.css';

const UpdatePin = () => {
  const [newPin, setNewPin] = useState('');
  const [message, setMessage] = useState({ text: '', type: '' });
  const [loading, setLoading] = useState(false);

  const handleUpdatePin = async (e) => {
    e.preventDefault();

    // Frontend validation: 4-digit check
    if (!/^\d{4}$/.test(newPin)) {
      setMessage({ text: 'PIN must be exactly 4 digits.', type: 'error' });
      return;
    }

    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      const response = await axios.post(
        `http://localhost:8080/api/upi/update-pin?pin=${newPin}`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );

      setMessage({ text: response.data, type: 'success' });
      setNewPin('');
    } catch (err) {
      setMessage({
        text: err.response?.data?.message || 'Failed to update PIN',
        type: 'error',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="upin-card">
      <div className="upin-icon">🔑</div>
      <h3 className="upin-title">Update Transaction PIN</h3>
      <p className="upin-subtitle">Enter a new 4-digit security PIN</p>

      <form onSubmit={handleUpdatePin} className="upin-form">
        <input
          type="password"
          maxLength="4"
          placeholder="• • • •"
          value={newPin}
          onChange={(e) => setNewPin(e.target.value.replace(/\D/g, ''))}
          className="upin-input"
        />

        <button type="submit" disabled={loading} className="upin-btn">
          {loading ? 'Updating...' : 'Set New PIN'}
        </button>
      </form>

      {message.text && (
        <p className={`upin-message ${message.type}`}>
          {message.type === 'success' ? '✓ ' : '✕ '}
          {message.text}
        </p>
      )}
    </div>
  );
};

export default UpdatePin;