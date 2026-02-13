import { useState, useEffect, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";

export default function Contacts() {
  const [contacts, setContacts] = useState([]);
  const [searchTerm, setSearchTerm] = useState(""); 
  const [name, setName] = useState("");
  const [upi, setUpi] = useState("");
  const [loading, setLoading] = useState(false);
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

  // 2. FILTER LOGIC: This happens on every render
  const filteredContacts = useMemo(() => {
    return contacts.filter(contact => 
        contact.displayName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        contact.upiId.toLowerCase().includes(searchTerm.toLowerCase())
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
    } finally { setLoading(false); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this contact?")) return;
    try {
        await api.delete(`/contacts/${id}`);
        fetchContacts();
    } catch (err) { alert("Failed to delete"); }
  };

  const handlePay = (contact) => {
    navigate("/transfer", { state: { upiId: contact.upiId } });
  };

  return (
    <div className="page-center">
      <div style={{ maxWidth: "600px", width: "100%" }}>
        <h1 className="page-title">My Contacts ({filteredContacts.length})</h1>

        {/* SEARCH BAR  */}
      <div style={{ marginBottom: "1.5rem" }}>
        <input 
          type="text"
          placeholder="🔍 Search by name or UPI ID..."
          style={localStyles.searchInput}
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>
        
        {/* ADD CONTACT FORM */}
        <div className="card" style={{ marginBottom: "2rem" }}>
          <form onSubmit={handleAdd} style={localStyles.form}>
            <input 
              style={localStyles.input}
              placeholder="Display Name" 
              value={name} 
              onChange={(e) => setName(e.target.value)} 
              required 
            />
            <input 
              style={localStyles.input}
              placeholder="UPI ID (e.g. user@okaxis)" 
              value={upi} 
              onChange={(e) => setUpi(e.target.value)} 
              required 
            />
            <button style={localStyles.addBtn} disabled={loading}>
              {loading ? "Saving..." : "Add Contact"}
            </button>
          </form>
        </div>

        {/* CONTACT LIST */}
        <div className="card">
          {filteredContacts.length === 0 ? (
    <div style={{ textAlign: "center", padding: "2rem" }}>
      <p style={{ fontSize: "1.1rem", color: "#64748b", marginBottom: "0.5rem" }}>
        {searchTerm ? `🔍 No matches for "${searchTerm}"` : "👤 Your contact list is empty"}
      </p>
      {searchTerm && (
        <button 
          onClick={() => setSearchTerm("")} 
          style={{ 
            background: "none", 
            border: "none", 
            color: "#2563eb", 
            textDecoration: "underline", 
            cursor: "pointer",
            fontSize: "0.9rem" 
          }}
        >
          Clear search and view all
        </button>
      )}
    </div>
          ) : (
            filteredContacts.map(c => (
              <div key={c.id} style={localStyles.contactRow}>
                <div>
                  <div style={{ fontWeight: "600" }}>{c.displayName}</div>
                  <div style={{ fontSize: "0.8rem", color: "#666" }}>{c.upiId}</div>
                </div>
                <div style={{ display: "flex", gap: "10px" }}>
                  <button onClick={() => handlePay(c)} style={localStyles.payBtn}>Pay</button>
                  <button onClick={() => handleDelete(c.id)} style={localStyles.delBtn}>Delete</button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}

const localStyles = {
  form: { display: "flex", gap: "10px", flexWrap: "wrap" },
  input: { flex: 1, padding: "0.6rem", borderRadius: "8px", border: "1px solid #ddd" },
  addBtn: { padding: "0.6rem 1rem", background: "#2563eb", color: "#fff", border: "none", borderRadius: "8px", cursor: "pointer" },
  contactRow: {
  display: "flex",
  justifyContent: "space-between",
  alignItems: "center",
  padding: "12px 10px", 
  borderBottom: "1px solid #eee",
  transition: "background 0.2s ease", 
},
  payBtn: { background: "#10b981", color: "#fff", border: "none", padding: "5px 12px", borderRadius: "6px", cursor: "pointer" },
  delBtn: { background: "#ef4444", color: "#fff", border: "none", padding: "5px 12px", borderRadius: "6px", cursor: "pointer" },
  searchInput: {
    width: "100%",
    padding: "0.8rem",
    borderRadius: "12px",
    border: "1px solid #cbd5e1",
    fontSize: "1rem",
    outline: "none",
    boxShadow: "0 2px 4px rgba(0,0,0,0.05)"
  },

};