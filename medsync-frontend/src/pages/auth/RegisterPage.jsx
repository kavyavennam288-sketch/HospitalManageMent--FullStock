import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { registerApi } from '../../api/authApi';

export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: '', email: '', password: '',
    role: 'PATIENT', specialty: '', licenseNumber: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await registerApi(form);
      // After register, redirect to login (they get a token but
      // we keep the flow simple — login to get dashboard access)
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h1 style={styles.title}>🏥 MedSync</h1>
        <h2 style={styles.subtitle}>Create your account</h2>

        {error && <div style={styles.error}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div style={styles.field}>
            <label style={styles.label}>Full Name</label>
            <input style={styles.input} required
              value={form.fullName}
              onChange={(e) => setForm({ ...form, fullName: e.target.value })}
              placeholder="Dr. Ravi Kumar" />
          </div>

          <div style={styles.field}>
            <label style={styles.label}>Email</label>
            <input style={styles.input} type="email" required
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              placeholder="you@example.com" />
          </div>

          <div style={styles.field}>
            <label style={styles.label}>Password</label>
            <input style={styles.input} type="password" required
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              placeholder="Min 8 characters" />
          </div>

          <div style={styles.field}>
            <label style={styles.label}>Role</label>
            <select style={styles.input}
              value={form.role}
              onChange={(e) => setForm({ ...form, role: e.target.value })}>
              <option value="PATIENT">Patient</option>
              <option value="DOCTOR">Doctor</option>
              <option value="ADMIN">Admin</option>
            </select>
          </div>

          {/* Doctor-only fields — shown conditionally */}
          {form.role === 'DOCTOR' && (
            <>
              <div style={styles.field}>
                <label style={styles.label}>Specialty</label>
                <input style={styles.input}
                  value={form.specialty}
                  onChange={(e) => setForm({ ...form, specialty: e.target.value })}
                  placeholder="e.g. Cardiology" />
              </div>
              <div style={styles.field}>
                <label style={styles.label}>License Number</label>
                <input style={styles.input}
                  value={form.licenseNumber}
                  onChange={(e) => setForm({ ...form, licenseNumber: e.target.value })}
                  placeholder="e.g. MCI-12345" />
              </div>
            </>
          )}

          <button type="submit" style={styles.btn} disabled={loading}>
            {loading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>

        <p style={styles.footer}>
          Already have an account?{' '}
          <Link to="/login" style={styles.footerLink}>Sign in</Link>
        </p>
      </div>
    </div>
  );
}

const styles = {
  page: {
    minHeight: '100vh', display: 'flex',
    alignItems: 'center', justifyContent: 'center', background: '#f0f4f8',
  },
  card: {
    background: '#fff', borderRadius: 12, padding: '40px 36px',
    width: '100%', maxWidth: 440,
    boxShadow: '0 4px 24px rgba(0,0,0,0.08)',
  },
  title: { textAlign: 'center', color: '#1a73e8', marginBottom: 4 },
  subtitle: { textAlign: 'center', color: '#555', fontWeight: 400, marginBottom: 24 },
  error: {
    background: '#fdecea', color: '#c0392b', padding: '10px 14px',
    borderRadius: 6, marginBottom: 16, fontSize: 14,
  },
  field: { marginBottom: 16 },
  label: { display: 'block', marginBottom: 6, fontWeight: 500, color: '#333' },
  input: {
    width: '100%', padding: '10px 12px', border: '1px solid #ddd',
    borderRadius: 6, fontSize: 15, boxSizing: 'border-box',
  },
  btn: {
    width: '100%', padding: '12px', background: '#1a73e8',
    color: '#fff', border: 'none', borderRadius: 6,
    fontSize: 16, fontWeight: 600, cursor: 'pointer', marginTop: 8,
  },
  footer: { textAlign: 'center', marginTop: 20, color: '#555' },
  footerLink: { color: '#1a73e8', fontWeight: 600 },
};