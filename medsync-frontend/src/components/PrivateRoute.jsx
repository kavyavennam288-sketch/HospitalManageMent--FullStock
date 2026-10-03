import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

// allowedRoles = ["PATIENT"] or ["DOCTOR"] or ["ADMIN"] or ["PATIENT", "ADMIN"]
// If no role matches, redirect to their correct dashboard instead of login.
export default function PrivateRoute({ children, allowedRoles }) {
  const { user, loading } = useAuth();

  // While checking localStorage, render nothing to avoid a flash
  if (loading) return <div className="loading">Loading...</div>;

  // Not logged in at all → send to login
  if (!user) return <Navigate to="/login" replace />;

  // Logged in but wrong role → send to their own dashboard
  if (allowedRoles && !allowedRoles.includes(user.role)) {
    const redirectMap = {
      PATIENT: '/patient/dashboard',
      DOCTOR: '/doctor/dashboard',
      ADMIN: '/admin/dashboard',
    };
    return <Navigate to={redirectMap[user.role] || '/login'} replace />;
  }

  return children;
}