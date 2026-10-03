import { useState } from 'react';
import { addTimeSlotApi } from '../../api/doctorApi';
import Navbar from '../../components/Navbar';

const DAYS = ['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY'];

export default function ManageSchedule() {
  const [form, setForm] = useState({
    dayOfWeek: 'MONDAY', startTime: '09:00', endTime: '09:30',
  });
  const [slots, setSlots] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleAddSlot = async (e) => {
    e.preventDefault();
    setError(''); setSuccess('');
    try {
      const res = await addTimeSlotApi(
        form.dayOfWeek, form.startTime, form.endTime
      );
      setSlots((prev) => [...prev, res.data]);
      setSuccess(`Slot added: ${form.dayOfWeek} ${form.startTime}–${form.endTime}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add slot.');
    }
  };

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2>Manage My Schedule</h2>
        <p style={styles.subtitle}>
          Add recurring weekly slots. Patients can book into these times.
        </p>

        <div style={styles.card}>
          <h3>Add Time Slot</h3>

          {error && <div style={styles.error}>{error}</div>}
          {success && <div style={styles.success}>{success}</div>}

          <form onSubmit={handleAddSlot}>
            <div style={styles.row}>
              <div style={styles.field}>
                <label style={styles.label}>Day of Week</label>
                <select style={styles.input}
                  value={form.dayOfWeek}
                  onChange={(e) => setForm({ ...form, dayOfWeek: e.target.value })}>
                  {DAYS.map((d) => <option key={d}>{d}</option>)}
                </select>
              </div>
              <div style={styles.field}>
                <label style={styles.label}>Start Time</label>
                <input style={styles.input} type="time"
                  value={form.startTime}
                  onChange={(e) => setForm({ ...form, startTime: e.target.value })} />
              </div>
              <div style={styles.field}>
                <label style={styles.label}>End Time</label>
                <input style={styles.input} type="time"
                  value={form.endTime}
                  onChange={(e) => setForm({ ...form, endTime: e.target.value })} />
              </div>
            </div>
            <button type="submit" style={styles.btn}>+ Add Slot</button>
          </form>
        </div>

        {slots.length > 0 && (
          <div style={styles.card}>
            <h3>Added This Session</h3>
            {slots.map((slot, i) => (
              <div key={i} style={styles.slotRow}>
                <span>📅 {slot.dayOfWeek}</span>
                <span>🕐 {slot.startTime} – {slot.endTime}</span>
                <span style={{ color: '#27ae60' }}>✓ Active</span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

const styles = {
  page: { maxWidth: 700, margin: '0 auto', padding: '32px 16px' },
  subtitle: { color: '#666', marginBottom: 24 },
  card: {
    background: '#fff', borderRadius: 10, padding: 24, marginBottom: 20,
    boxShadow: '0 2px 8px rgba(0,0,0,0.07)',
  },
  row: { display: 'flex', gap: 16, marginBottom: 16 },
  field: { flex: 1 },
  label: { display: 'block', marginBottom: 6, fontWeight: 500 },
  input: {
    width: '100%', padding: '10px 12px', border: '1px solid #ddd',
    borderRadius: 6, fontSize: 15, boxSizing: 'border-box',
  },
  btn: {
    background: '#1a73e8', color: '#fff', border: 'none',
    padding: '10px 20px', borderRadius: 6, cursor: 'pointer', fontWeight: 600,
  },
  error: {
    background: '#fdecea', color: '#c0392b', padding: '10px 14px',
    borderRadius: 6, marginBottom: 16,
  },
  success: {
    background: '#eafaf1', color: '#27ae60', padding: '10px 14px',
    borderRadius: 6, marginBottom: 16,
  },
  slotRow: {
    display: 'flex', justifyContent: 'space-between',
    padding: '10px 0', borderBottom: '1px solid #f0f0f0',
  },
};