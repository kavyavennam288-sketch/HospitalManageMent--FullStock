import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAllDoctorsApi, getAvailableSlotsApi } from '../../api/doctorApi';
import { bookAppointmentApi } from '../../api/appointmentApi';
import Navbar from '../../components/Navbar';

export default function BookAppointment() {
  const navigate = useNavigate();

  // Step 1: pick a doctor
  // Step 2: pick a date and see available slots
  // Step 3: confirm booking
  const [step, setStep] = useState(1);

  const [doctors, setDoctors] = useState([]);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [date, setDate] = useState('');
  const [slots, setSlots] = useState([]);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  // Load doctors on mount
  useEffect(() => {
    getAllDoctorsApi().then((res) => setDoctors(res.data));
  }, []);

  const handleFetchSlots = async () => {
    if (!date) return;
    setLoading(true);
    setSlots([]);
    setError('');
    try {
      const res = await getAvailableSlotsApi(selectedDoctor.id, date);
      setSlots(res.data);
      if (res.data.length === 0) {
        setError('No available slots on this date. Try another day.');
      }
      setStep(2);
    } catch {
      setError('Failed to load slots.');
    } finally {
      setLoading(false);
    }
  };

  const handleBook = async () => {
    setLoading(true);
    setError('');
    try {
      // slotDateTime is the exact ISO datetime from the slot response
      // e.g. "2024-12-25T09:00:00"
      await bookAppointmentApi(selectedDoctor.id, selectedSlot.slotDateTime);
      setSuccess(true);
      setStep(3);
    } catch (err) {
      setError(err.response?.data?.message || 'Booking failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2>Book an Appointment</h2>

        {/* Step indicator */}
        <div style={styles.steps}>
          {['Select Doctor', 'Choose Slot', 'Confirmed'].map((s, i) => (
            <div key={i} style={{
              ...styles.step,
              color: step === i + 1 ? '#1a73e8' : step > i + 1 ? '#27ae60' : '#999',
              fontWeight: step === i + 1 ? 700 : 400,
            }}>
              {step > i + 1 ? '✓' : i + 1}. {s}
            </div>
          ))}
        </div>

        {error && <div style={styles.error}>{error}</div>}

        {/* ── Step 1: Pick a doctor ── */}
        {step === 1 && (
          <div>
            <div style={styles.field}>
              <label style={styles.label}>Select Date</label>
              <input style={styles.input} type="date"
                value={date}
                min={new Date().toISOString().split('T')[0]}
                onChange={(e) => setDate(e.target.value)} />
            </div>

            <h3 style={{ marginBottom: 12 }}>Available Doctors</h3>
            {doctors.map((doc) => (
              <div key={doc.id}
                style={{
                  ...styles.doctorCard,
                  border: selectedDoctor?.id === doc.id
                    ? '2px solid #1a73e8'
                    : '1px solid #eee',
                }}
                onClick={() => setSelectedDoctor(doc)}>
                <strong>{doc.fullName}</strong>
                <span style={styles.specialty}> · {doc.specialty}</span>
                {doc.qualifications && (
                  <div style={styles.small}>{doc.qualifications}</div>
                )}
              </div>
            ))}

            <button style={styles.btn}
              disabled={!selectedDoctor || !date || loading}
              onClick={handleFetchSlots}>
              {loading ? 'Loading slots...' : 'See Available Slots →'}
            </button>
          </div>
        )}

        {/* ── Step 2: Pick a slot ── */}
        {step === 2 && (
          <div>
            <p style={styles.summary}>
              <strong>Doctor:</strong> {selectedDoctor.fullName} ·
              <strong> Date:</strong> {date}
            </p>
            <h3>Available Slots</h3>
            <div style={styles.slotGrid}>
              {slots.map((slot) => (
                <div key={slot.id}
                  style={{
                    ...styles.slotChip,
                    background: selectedSlot?.id === slot.id ? '#1a73e8' : '#f0f4f8',
                    color: selectedSlot?.id === slot.id ? '#fff' : '#333',
                  }}
                  onClick={() => setSelectedSlot(slot)}>
                  {slot.startTime} – {slot.endTime}
                </div>
              ))}
            </div>

            <div style={{ display: 'flex', gap: 12, marginTop: 20 }}>
              <button style={styles.secondaryBtn} onClick={() => setStep(1)}>
                ← Back
              </button>
              <button style={styles.btn}
                disabled={!selectedSlot || loading}
                onClick={handleBook}>
                {loading ? 'Booking...' : 'Confirm Booking'}
              </button>
            </div>
          </div>
        )}

        {/* ── Step 3: Success ── */}
        {step === 3 && success && (
          <div style={styles.successBox}>
            <div style={styles.successIcon}>✅</div>
            <h3>Appointment Confirmed!</h3>
            <p>Your appointment with <strong>{selectedDoctor.fullName}</strong></p>
            <p>on <strong>{date}</strong> at <strong>{selectedSlot.startTime}</strong></p>
            <p style={{ color: '#555', marginTop: 8 }}>
              A confirmation email has been sent to your inbox.
            </p>
            <div style={{ display: 'flex', gap: 12, marginTop: 20, justifyContent: 'center' }}>
              <button style={styles.btn} onClick={() => navigate('/patient/appointments')}>
                View My Appointments
              </button>
              <button style={styles.secondaryBtn} onClick={() => {
                setStep(1); setSelectedDoctor(null);
                setDate(''); setSlots([]); setSelectedSlot(null); setSuccess(false);
              }}>
                Book Another
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

const styles = {
  page: { maxWidth: 700, margin: '0 auto', padding: '32px 16px' },
  steps: { display: 'flex', gap: 24, marginBottom: 28 },
  step: { fontSize: 15 },
  error: {
    background: '#fdecea', color: '#c0392b', padding: '10px 14px',
    borderRadius: 6, marginBottom: 16,
  },
  field: { marginBottom: 16 },
  label: { display: 'block', marginBottom: 6, fontWeight: 500 },
  input: {
    padding: '10px 12px', border: '1px solid #ddd', borderRadius: 6,
    fontSize: 15, width: '100%', boxSizing: 'border-box',
  },
  doctorCard: {
    padding: 14, borderRadius: 8, marginBottom: 10, cursor: 'pointer',
    background: '#fff', transition: 'border 0.2s',
  },
  specialty: { color: '#666', fontSize: 14 },
  small: { fontSize: 13, color: '#888', marginTop: 4 },
  slotGrid: { display: 'flex', flexWrap: 'wrap', gap: 10, marginTop: 12 },
  slotChip: {
    padding: '10px 16px', borderRadius: 6, cursor: 'pointer',
    fontWeight: 500, fontSize: 14,
  },
  summary: { background: '#f0f4f8', padding: 12, borderRadius: 6, marginBottom: 16 },
  btn: {
    background: '#1a73e8', color: '#fff', border: 'none',
    padding: '11px 22px', borderRadius: 6, cursor: 'pointer', fontWeight: 600,
  },
  secondaryBtn: {
    background: '#f0f4f8', color: '#333', border: '1px solid #ddd',
    padding: '11px 22px', borderRadius: 6, cursor: 'pointer', fontWeight: 600,
  },
  successBox: {
    textAlign: 'center', padding: '40px 20px',
    background: '#fff', borderRadius: 10,
    boxShadow: '0 2px 8px rgba(0,0,0,0.07)',
  },
  successIcon: { fontSize: 48, marginBottom: 12 },
};