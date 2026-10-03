import { useEffect, useState } from 'react';
import {
  getAllPatientsApi, softDeletePatientApi, reactivatePatientApi,
} from '../../api/adminApi';
import Navbar from '../../components/Navbar';

export default function UserManager() {
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  const fetchPatients = () => {
    getAllPatientsApi()
      .then((res) => setPatients(res.data))
      .finally(() => setLoading(false));
  };

  useEffect(fetchPatients, []);

  const handleDeactivate = async (id) => {
    if (!window.confirm('Deactivate this patient? Their appointments will be cancelled.')) return;
    try {
      await softDeletePatientApi(id);
      fetchPatients();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed.');
    }
  };

  const handleReactivate = async (id) => {
    try {
      await reactivatePatientApi(id);
      fetchPatients();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed.');
    }
  };

  const filtered = patients.filter((p) =>
    p.fullName.toLowerCase().includes(search.toLowerCase()) ||
    p.email.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2>Patient Records</h2>

        <input style={styles.search} placeholder="Search by name or email..."
          value={search}
          onChange={(e) => setSearch(e.target.value)} />

        {loading ? <p>Loading...</p> : filtered.length === 0 ? (
          <p style={{ color: '#666' }}>No patients found.</p>
        ) : (
          filtered.map((patient) => (
            <div key={patient.id} style={styles.card}>
              <div style={styles.cardHeader}>
                <div>
                  <strong>{patient.fullName}</strong>
                  <span style={styles.email}> · {patient.email}</span>
                </div>
                <span style={{
                  ...styles.badge,
                  background: patient.active ? '#27ae60' : '#e74c3c',
                }}>
                  {patient.active ? 'Active' : 'Deactivated'}
                </span>
              </div>

              {patient.bloodGroup && (
                <div style={styles.detail}>🩸 {patient.bloodGroup}</div>
              )}
              {patient.allergies && (
                <div style={styles.detail}>⚠️ Allergies: {patient.allergies}</div>
              )}
              {patient.emergencyContact && (
                <div style={styles.detail}>📞 Emergency: {patient.emergencyContact}</div>
              )}

              <div style={styles.actions}>
                {patient.active ? (
                  <button style={styles.deactivateBtn}
                    onClick={() => handleDeactivate(patient.id)}>
                    Deactivate
                  </button>
                ) : (
                  <button style={styles.reactivateBtn}
                    onClick={() => handleReactivate(patient.id)}>
                    Reactivate
                  </button>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

const styles = {
  page: { maxWidth: 800, margin: '0 auto', padding: '32px 16px' },
  search: {
    width: '100%', padding: '10px 14px', border: '1px solid #ddd',
    borderRadius: 8, fontSize: 15, marginBottom: 20, boxSizing: 'border-box',
  },
  card: {
    background: '#fff', borderRadius: 10, padding: 20, marginBottom: 14,
    boxShadow: '0 2px 8px rgba(0,0,0,0.07)',
  },
  cardHeader: {
    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
  },
  email: { color: '#666', fontSize: 14 },
  detail: { fontSize: 14, color: '#555', marginTop: 6 },
  badge: {
    color: '#fff', fontSize: 12, padding: '3px 10px',
    borderRadius: 20, fontWeight: 600,
  },
  actions: { marginTop: 14 },
  deactivateBtn: {
    background: '#fdecea', color: '#c0392b', border: '1px solid #e74c3c',
    padding: '7px 14px', borderRadius: 6, cursor: 'pointer', fontWeight: 600,
  },
  reactivateBtn: {
    background: '#eafaf1', color: '#27ae60', border: '1px solid #27ae60',
    padding: '7px 14px', borderRadius: 6, cursor: 'pointer', fontWeight: 600,
  },
};