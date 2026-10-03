import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // Nav links differ by role
  const navLinks = {
    PATIENT: [
      { to: '/patient/dashboard', label: 'Dashboard' },
      { to: '/patient/book', label: 'Book Appointment' },
      { to: '/patient/appointments', label: 'My Appointments' },
    ],
    DOCTOR: [
      { to: '/doctor/dashboard', label: 'Dashboard' },
      { to: '/doctor/schedule', label: 'My Schedule' },
    ],
    ADMIN: [
      { to: '/admin/dashboard', label: 'Dashboard' },
      { to: '/admin/resources', label: 'Resources' },
      { to: '/admin/patients', label: 'Patients' },
    ],
  };

  const links = user ? navLinks[user.role] || [] : [];

  return (
    <nav style={styles.nav}>
      <span style={styles.brand}>🏥 MedSync</span>
      <div style={styles.links}>
        {links.map((link) => (
          <Link key={link.to} to={link.to} style={styles.link}>
            {link.label}
          </Link>
        ))}
      </div>
      {user && (
        <div style={styles.userSection}>
          <span style={styles.userName}>
            {user.fullName} ({user.role})
          </span>
          <button onClick={handleLogout} style={styles.logoutBtn}>
            Logout
          </button>
        </div>
      )}
    </nav>
  );
}

const styles = {
  nav: {
    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
    padding: '0 24px', height: 60, background: '#1a73e8', color: '#fff',
  },
  brand: { fontSize: 20, fontWeight: 700 },
  links: { display: 'flex', gap: 24 },
  link: { color: '#fff', textDecoration: 'none', fontWeight: 500 },
  userSection: { display: 'flex', alignItems: 'center', gap: 12 },
  userName: { fontSize: 14 },
  logoutBtn: {
    background: 'rgba(255,255,255,0.2)', border: 'none',
    color: '#fff', padding: '6px 14px', borderRadius: 6,
    cursor: 'pointer', fontWeight: 500,
  },
};