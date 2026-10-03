import { useEffect, useState } from 'react';
import { getMyAppointmentsApi, cancelAppointmentApi } from '../../api/appointmentApi';
import Navbar from '../../components/Navbar';

export default function MyAppointments() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchAppointments = () => {
    getMyAppointmentsApi()
      .then((res) => setAppointments(res.data))
      .finally(() => setLoading(false));
  };

  useEffect(fetchAppointments, []);

  const handleCancel = async (id) => {
    if (!window.confirm('Cancel this appointment?')) return;
    try {
      await cancelAppointmentApi(id);
      fetchAppointments();   // refresh list
    } catch (err) {
      alert(err.response?.data?.message || 'Cancel failed.');
    }
  };

  const statusColors = {
    CONFIRMED: '#27ae60', PENDING: '#f39c12',
    CANCELLED: '#e74c3c', COMPLETED: '#2980b9',
  };

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2>My Appointments</h2>

        {loading ? <p>Loading...</p> : appointments.length === 0 ? (
          <p style={{ color: '#666' }}>No appointments found.</p>
        ) : (
          appointments.map((apt) => (
            <div key={apt.id} style={styles.card}>
              <div style={styles.cardHeader}>
                <div>
                  <strong>Dr. {apt.doctorName}</strong>
                  <span style={styles.specialty}> · {apt.doctorSpecialty}</span>
                </div>
                <span style={{
                  ...styles.badge,
                  background: statusColors[apt.status]
                }}>
                  {apt.status}
                </span>
              </div>
              <div style={styles.detail}>
                🕐 {new Date(apt.startTime).toLocaleString()} –{' '}
                {new Date(apt.endTime).toLocaleTimeString()}
              </div>
              <div style={styles.detail}>🚪 {apt.roomNumber}</div>

              {/* Only show cancel button for active appointments */}
              {(apt.status === 'CONFIRMED' || apt.status === 'PENDING') && (
                <button
                  style={styles.cancelBtn}
                  onClick={() => handleCancel(apt.id)}>
                  Cancel Appointment
                </button>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
}

const styles = {
  page: { maxWidth: 700, margin: '0 auto', padding: '32px 16px' },
  card: {
    background: '#fff', borderRadius: 10, padding: 20, marginBottom: 16,
    boxShadow: '0 2px 8px rgba(0,0,0,0.07)',
  },
  cardHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  specialty: { color: '#666', fontSize: 14 },
  detail: { color: '#555', fontSize: 14, marginTop: 6 },
  badge: {
    color: '#fff', fontSize: 12, padding: '3px 10px',
    borderRadius: 20, fontWeight: 600,
  },
  cancelBtn: {
    marginTop: 12, background: '#fdecea', color: '#c0392b',
    border: '1px solid #e74c3c', padding: '7px 14px',
    borderRadius: 6, cursor: 'pointer', fontWeight: 600,
  },
};