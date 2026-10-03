import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const user = await login(form.email, form.password);
      // Redirect to role-specific dashboard after login
      const redirectMap = {
        PATIENT: '/patient/dashboard',
        DOCTOR: '/doctor/dashboard',
        ADMIN: '/admin/dashboard',
      };
      navigate(redirectMap[user.role]);
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h1 style={styles.title}>🏥 MedSync</h1>
        <h2 style={styles.subtitle}>Sign in to your account</h2>

        {error && <div style={styles.error}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div style={styles.field}>
            <label style={styles.label}>Email</label>
            <input
              style={styles.input}
              type="email"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              placeholder="you@example.com"
              required
            />
          </div>
          <div style={styles.field}>
            <label style={styles.label}>Password</label>
            <input
              style={styles.input}
              type="password"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              placeholder="••••••••"
              required
            />
          </div>
          <button
            type="submit"
            style={styles.btn}
            disabled={loading}
          >
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        <p style={styles.footer}>
          Don't have an account?{' '}
          <Link to="/register" style={styles.footerLink}>Register here</Link>
        </p>
      </div>
    </div>
  );
}

const styles = {
  page: {
    minHeight: '100vh', display: 'flex',
    alignItems: 'center', justifyContent: 'center',
    background: '#f0f4f8',
  },
  card: {
    background: '#fff', borderRadius: 12, padding: '40px 36px',
    width: '100%', maxWidth: 420,
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