import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { getMyAppointmentsApi } from '../../api/appointmentApi';
import Navbar from '../../components/Navbar';

export default function PatientDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getMyAppointmentsApi()
      .then((res) => setAppointments(res.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  // Only show upcoming (not cancelled/completed) appointments
  const upcoming = appointments.filter(
    (a) => a.status === 'CONFIRMED' || a.status === 'PENDING'
  );

  const statusColors = {
    CONFIRMED: '#27ae60', PENDING: '#f39c12',
    CANCELLED: '#e74c3c', COMPLETED: '#2980b9',
  };

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2 style={styles.greeting}>Welcome back, {user?.fullName} 👋</h2>

        {/* Quick stats row */}
        <div style={styles.statsRow}>
          <div style={styles.statCard}>
            <div style={styles.statNum}>{upcoming.length}</div>
            <div style={styles.statLabel}>Upcoming</div>
          </div>
          <div style={styles.statCard}>
            <div style={styles.statNum}>
              {appointments.filter((a) => a.status === 'COMPLETED').length}
            </div>
            <div style={styles.statLabel}>Completed</div>
          </div>
          <div style={styles.statCard}>
            <div style={styles.statNum}>{appointments.length}</div>
            <div style={styles.statLabel}>Total</div>
          </div>
        </div>

        <div style={styles.section}>
          <div style={styles.sectionHeader}>
            <h3>Upcoming Appointments</h3>
            <button style={styles.btn} onClick={() => navigate('/patient/book')}>
              + Book Appointment
            </button>
          </div>

          {loading ? (
            <p>Loading...</p>
          ) : upcoming.length === 0 ? (
            <div style={styles.empty}>
              <p>No upcoming appointments.</p>
              <button style={styles.btn} onClick={() => navigate('/patient/book')}>
                Book your first appointment
              </button>
            </div>
          ) : (
            upcoming.map((apt) => (
              <div key={apt.id} style={styles.card}>
                <div>
                  <strong>Dr. {apt.doctorName}</strong>
                  <span style={styles.specialty}> · {apt.doctorSpecialty}</span>
                </div>
                <div style={styles.cardDetail}>
                  🕐 {new Date(apt.startTime).toLocaleString()}
                </div>
                <div style={styles.cardDetail}>🚪 Room: {apt.roomNumber}</div>
                <span style={{
                  ...styles.badge,
                  background: statusColors[apt.status]
                }}>
                  {apt.status}
                </span>
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
  greeting: { fontSize: 24, marginBottom: 24 },
  statsRow: { display: 'flex', gap: 16, marginBottom: 32 },
  statCard: {
    flex: 1, background: '#fff', borderRadius: 10,
    padding: '20px', textAlign: 'center',
    boxShadow: '0 2px 8px rgba(0,0,0,0.07)',
  },
  statNum: { fontSize: 32, fontWeight: 700, color: '#1a73e8' },
  statLabel: { color: '#666', marginTop: 4 },
  section: { background: '#fff', borderRadius: 10, padding: 24,
    boxShadow: '0 2px 8px rgba(0,0,0,0.07)' },
  sectionHeader: { display: 'flex', justifyContent: 'space-between',
    alignItems: 'center', marginBottom: 20 },
  card: {
    border: '1px solid #eee', borderRadius: 8, padding: 16, marginBottom: 12,
  },
  specialty: { color: '#666', fontSize: 14 },
  cardDetail: { color: '#555', fontSize: 14, marginTop: 4 },
  badge: {
    display: 'inline-block', color: '#fff', fontSize: 12,
    padding: '3px 10px', borderRadius: 20, marginTop: 8,
  },
  btn: {
    background: '#1a73e8', color: '#fff', border: 'none',
    padding: '10px 18px', borderRadius: 6, cursor: 'pointer', fontWeight: 600,
  },
  empty: { textAlign: 'center', padding: '32px 0', color: '#666' },
};