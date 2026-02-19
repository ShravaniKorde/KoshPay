import React, { useState } from 'react';
import api from '../api/axios';

const UpdatePin = () => {
  const [newPin, setNewPin] = useState('');
  const [message, setMessage] = useState({ text: '', type: '' });
  const [loading, setLoading] = useState(false);

  const handleUpdatePin = async (e) => {
    e.preventDefault();
    
    // Frontend Validation: 4-digit check
    if (!/^\d{4}$/.test(newPin)) {
      setMessage({ text: 'PIN must be exactly 4 digits.', type: 'error' });
      return;
    }

    setLoading(true);
    try {
      const response = await api.post(
        `/upi/update-pin?pin=${newPin}`
      );

      setMessage({ text: response.data, type: 'success' });
      setNewPin('');
    } catch (err) {
      setMessage({ 
        text: err.response?.data?.message || 'Failed to update PIN', 
        type: 'error' 
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.card}>
      <h3 style={styles.title}>Update Transaction PIN</h3>
      <p style={styles.subtitle}>Enter a new 4-digit security PIN</p>
      
      <form onSubmit={handleUpdatePin}>
        <input
          type="password"
          maxLength="4"
          placeholder="Enter 4-digit PIN"
          value={newPin}
          onChange={(e) => setNewPin(e.target.value.replace(/\D/g, ''))} // Numeric only
          style={styles.input}
        />
        
        <button type="submit" disabled={loading} style={styles.button}>
          {loading ? 'Updating...' : 'Set New PIN'}
        </button>
      </form>

      {message.text && (
        <p style={{ ...styles.message, color: message.type === 'success' ? '#16a34a' : '#dc2626' }}>
          {message.text}
        </p>
      )}
    </div>
  );
};

/* Styles */
const styles = {
  card: { padding: '24px', background: '#fff', borderRadius: '16px', boxShadow: '0 4px 20px rgba(0,0,0,0.08)', maxWidth: '400px' },
  title: { margin: '0 0 8px 0', fontSize: '1.2rem', color: '#1f2937' },
  subtitle: { margin: '0 0 20px 0', fontSize: '0.9rem', color: '#6b7280' },
  input: { width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #e5e7eb', fontSize: '1.1rem', textAlign: 'center', letterSpacing: '0.5em', marginBottom: '16px' },
  button: { width: '100%', padding: '12px', background: '#2563eb', color: '#fff', border: 'none', borderRadius: '8px', fontWeight: '600', cursor: 'pointer' },
  message: { marginTop: '12px', fontSize: '0.85rem', textAlign: 'center' }
};

export default UpdatePin;