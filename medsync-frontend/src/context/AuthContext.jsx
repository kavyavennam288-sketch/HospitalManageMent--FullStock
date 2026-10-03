import { createContext, useContext, useState, useEffect } from 'react';
import { loginApi } from '../api/authApi';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  // loading = true while we check localStorage on first render.
  // Prevents a flash of the login page for already-logged-in users.

  useEffect(() => {
    // On app start, check if a token already exists in localStorage.
    // If yes, restore the user session without requiring a new login.
    const token = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');

    if (token && savedUser) {
      setUser(JSON.parse(savedUser));
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const response = await loginApi(email, password);
    const data = response.data;

    // Persist token and user info so the session survives page refresh
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(data));

    setUser(data);
    return data;   // caller uses data.role to decide where to redirect
  };

  const logout = () => {
    localStorage.clear();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
}

// Custom hook — any component calls useAuth() instead of
// useContext(AuthContext) directly. Cleaner and less to import.
export function useAuth() {
  return useContext(AuthContext);
}