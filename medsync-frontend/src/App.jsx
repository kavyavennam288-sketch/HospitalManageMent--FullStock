import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import PrivateRoute from './components/PrivateRoute';

// Auth pages
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';

// Patient pages
import PatientDashboard from './pages/patient/PatientDashboard';
import BookAppointment from './pages/patient/BookAppointment';
import MyAppointments from './pages/patient/MyAppointments';

// Doctor pages
import DoctorDashboard from './pages/doctor/DoctorDashboard';
import ManageSchedule from './pages/doctor/ManageSchedule';

// Admin pages
import AdminDashboard from './pages/admin/AdminDashboard';
import ResourceManager from './pages/admin/ResourceManager';
import UserManager from './pages/admin/UserManager';

// Redirects logged-in users to their dashboard from the root URL
function RootRedirect() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  const map = {
    PATIENT: '/patient/dashboard',
    DOCTOR: '/doctor/dashboard',
    ADMIN: '/admin/dashboard',
  };
  return <Navigate to={map[user.role]} replace />;
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>

          {/* Root redirect */}
          <Route path="/" element={<RootRedirect />} />

          {/* Public routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Patient routes */}
          <Route path="/patient/dashboard" element={
            <PrivateRoute allowedRoles={['PATIENT']}>
              <PatientDashboard />
            </PrivateRoute>
          } />
          <Route path="/patient/book" element={
            <PrivateRoute allowedRoles={['PATIENT']}>
              <BookAppointment />
            </PrivateRoute>
          } />
          <Route path="/patient/appointments" element={
            <PrivateRoute allowedRoles={['PATIENT']}>
              <MyAppointments />
            </PrivateRoute>
          } />

          {/* Doctor routes */}
          <Route path="/doctor/dashboard" element={
            <PrivateRoute allowedRoles={['DOCTOR']}>
              <DoctorDashboard />
            </PrivateRoute>
          } />
          <Route path="/doctor/schedule" element={
            <PrivateRoute allowedRoles={['DOCTOR']}>
              <ManageSchedule />
            </PrivateRoute>
          } />

          {/* Admin routes */}
          <Route path="/admin/dashboard" element={
            <PrivateRoute allowedRoles={['ADMIN']}>
              <AdminDashboard />
            </PrivateRoute>
          } />
          <Route path="/admin/resources" element={
            <PrivateRoute allowedRoles={['ADMIN']}>
              <ResourceManager />
            </PrivateRoute>
          } />
          <Route path="/admin/patients" element={
            <PrivateRoute allowedRoles={['ADMIN']}>
              <UserManager />
            </PrivateRoute>
          } />

          {/* Catch-all */}
          <Route path="*" element={<Navigate to="/" replace />} />

        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}