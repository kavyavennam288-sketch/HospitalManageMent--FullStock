import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getDoctorAppointmentsApi } from '../../api/appointmentApi';
import { useAuth } from '../../context/AuthContext';
import Navbar from '../../components/Navbar';

export default function DoctorDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getDoctorAppointmentsApi()
      .then((res) => setAppointments(res.data))
      .finally(() => setLoading(false));
  }, []);

  // Filter to today's appointments only
  const today = new Date().toDateString();
  const todayApts = appointments.filter(
    (a) => new Date(a.startTime).toDateString() === today
      && a.status !== 'CANCELLED'
  );

  const statusColors = {
    CONFIRMED: '#27ae60', PENDING: '#f39c12',
    CANCELLED: '#e74c3c', COMPLETED: '#2980b9',
  };

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2>Welcome, {user?.fullName} 👨‍⚕️</h2>

        <div style={styles.statsRow}>
          <div style={styles.statCard}>
            <div style={styles.statNum}>{todayApts.length}</div>
            <div style={styles.statLabel}>Today's Appointments</div>
          </div>
          <div style={styles.statCard}>
            <div style={styles.statNum}>
              {appointments.filter((a) => a.status === 'CONFIRMED').length}
            </div>
            <div style={styles.statLabel}>Confirmed Total</div>
          </div>
          <div style={styles.statCard}>
            <div style={styles.statNum}>
              {appointments.filter((a) => a.status === 'COMPLETED').length}
            </div>
            <div style={styles.statLabel}>Completed</div>
          </div>
        </div>

        <div style={styles.section}>
          <div style={styles.sectionHeader}>
            <h3>Today's Schedule</h3>
            <button style={styles.btn} onClick={() => navigate('/doctor/schedule')}>
              Manage Schedule
            </button>
          </div>

          {loading ? <p>Loading...</p> : todayApts.length === 0 ? (
            <p style={{ color: '#666' }}>No appointments today.</p>
          ) : (
            todayApts
              .sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
              .map((apt) => (
                <div key={apt.id} style={styles.card}>
                  <div style={styles.cardHeader}>
                    <strong>{apt.patientName}</strong>
                    <span style={{
                      ...styles.badge,
                      background: statusColors[apt.status],
                    }}>{apt.status}</span>
                  </div>
                  <div style={styles.detail}>
                    🕐 {new Date(apt.startTime).toLocaleTimeString()} –{' '}
                    {new Date(apt.endTime).toLocaleTimeString()}
                  </div>
                  <div style={styles.detail}>🚪 {apt.roomNumber}</div>
                </div>
              ))
          )}
        </div>
      </div>
    </div>
  );
}

const styles = {
  page: { maxWidth: 800, margin: '0 auto', padding: '32px 16px' },
  statsRow: { display: 'flex', gap: 16, marginBottom: 28 },
  statCard: {
    flex: 1, background: '#fff', borderRadius: 10, padding: 20,
    textAlign: 'center', boxShadow: '0 2px 8px rgba(0,0,0,0.07)',
  },
  statNum: { fontSize: 32, fontWeight: 700, color: '#1a73e8' },
  statLabel: { color: '#666', marginTop: 4 },
  section: {
    background: '#fff', borderRadius: 10, padding: 24,
    boxShadow: '0 2px 8px rgba(0,0,0,0.07)',
  },
  sectionHeader: {
    display: 'flex', justifyContent: 'space-between',
    alignItems: 'center', marginBottom: 20,
  },
  card: { border: '1px solid #eee', borderRadius: 8, padding: 16, marginBottom: 12 },
  cardHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  detail: { color: '#555', fontSize: 14, marginTop: 6 },
  badge: {
    color: '#fff', fontSize: 12, padding: '3px 10px',
    borderRadius: 20, fontWeight: 600,
  },
  btn: {
    background: '#1a73e8', color: '#fff', border: 'none',
    padding: '10px 18px', borderRadius: 6, cursor: 'pointer', fontWeight: 600,
  },
};