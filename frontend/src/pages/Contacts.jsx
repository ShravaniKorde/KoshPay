import { useState, useEffect, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import "./Contacts.css";

export default function Contacts() {
  const [contacts, setContacts]   = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [name, setName]           = useState("");
  const [upi, setUpi]             = useState("");
  const [loading, setLoading]     = useState(false);
  const navigate = useNavigate();

  const fetchContacts = async () => {
    try {
      const res = await api.get("/contacts");
      setContacts(res.data);
    } catch (err) {
      console.error("Failed to fetch contacts", err);
    }
  };

  useEffect(() => { fetchContacts(); }, []);

  const filteredContacts = useMemo(() => {
    return contacts.filter(
      (c) =>
        c.displayName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        c.upiId.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }, [contacts, searchTerm]);

  const handleAdd = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      await api.post("/contacts", { displayName: name, upiId: upi });
      setName(""); setUpi("");
      fetchContacts();
      alert("Contact added!");
    } catch (err) {
      alert(err.response?.data?.message || "Failed to add contact");
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this contact?")) return;
    try {
      await api.delete(`/contacts/${id}`);
      fetchContacts();
    } catch { alert("Failed to delete"); }
  };

  const handlePay = (contact) => {
    navigate("/transfer", { state: { upiId: contact.upiId } });
  };

  // Get initials for avatar
  const getInitials = (name) => name?.charAt(0)?.toUpperCase() || "?";

  return (
    <div className="contacts-page">
      <div className="contacts-wrapper">

        <h1 className="contacts-title">My Contacts</h1>

        {/* Search */}
        <div className="contacts-search">
          <span className="contacts-search__icon">🔍</span>
          <input
            type="text"
            placeholder="Search by name or UPI ID..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="contacts-search__input"
          />
        </div>

        {/* Add contact */}
        <div className="contacts-add-card">
          <div className="contacts-add-card__title">Add New Contact</div>
          <form onSubmit={handleAdd} className="contacts-add-form">
            <input
              className="contacts-add-input"
              placeholder="Display Name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
            <input
              className="contacts-add-input"
              placeholder="UPI ID (e.g. user@okaxis)"
              value={upi}
              onChange={(e) => setUpi(e.target.value)}
              required
            />
            <button className="contacts-add-btn" disabled={loading}>
              {loading ? "Saving..." : "+ Add"}
            </button>
          </form>
        </div>

        {/* Contact list */}
        <div className="contacts-list-card">
          {filteredContacts.length === 0 ? (
            <div className="contacts-empty">
              <div className="contacts-empty__icon">
                {searchTerm ? "🔍" : "👤"}
              </div>
              <p className="contacts-empty__text">
                {searchTerm
                  ? `No matches for "${searchTerm}"`
                  : "Your contact list is empty"}
              </p>
              {searchTerm && (
                <button
                  className="contacts-clear-btn"
                  onClick={() => setSearchTerm("")}
                >
                  Clear search
                </button>
              )}
            </div>
          ) : (
            filteredContacts.map((c) => (
              <div key={c.id} className="contact-row">
                <div className="contact-row__left">
                  <div className="contact-avatar">
                    {getInitials(c.displayName)}
                  </div>
                  <div>
                    <div className="contact-name">{c.displayName}</div>
                    <div className="contact-upi">{c.upiId}</div>
                  </div>
                </div>
                <div className="contact-row__actions">
                  <button className="contact-pay-btn" onClick={() => handlePay(c)}>
                    Pay
                  </button>
                  <button className="contact-del-btn" onClick={() => handleDelete(c.id)}>
                    Delete
                  </button>
                </div>
              </div>
            ))
          )}
        </div>

      </div>
    </div>
  );
}