import { useEffect, useState } from 'react';
import { getDashboardStatsApi } from '../../api/adminApi';
import Navbar from '../../components/Navbar';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, Legend,
  PieChart, Pie, Cell, ResponsiveContainer,
} from 'recharts';

const PIE_COLORS = ['#1a73e8','#27ae60','#f39c12','#e74c3c'];

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getDashboardStatsApi()
      .then((res) => setStats(res.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div><Navbar /><p style={{ padding: 32 }}>Loading...</p></div>;
  if (!stats) return <div><Navbar /><p style={{ padding: 32 }}>Failed to load stats.</p></div>;

  // Shape data for Recharts PieChart — appointment status breakdown
  const pieData = [
    { name: 'Confirmed', value: stats.confirmedAppointments },
    { name: 'Completed', value: stats.completedAppointments },
    { name: 'Pending', value: stats.pendingAppointments },
    { name: 'Cancelled', value: stats.cancelledAppointments },
  ].filter((d) => d.value > 0);

  // resourceStats already in Recharts-friendly format from the backend
  const barData = stats.resourceStats || [];

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2>Admin Dashboard</h2>

        {/* ── KPI Cards ── */}
        <div style={styles.kpiRow}>
          {[
            { label: 'Total Appointments', value: stats.totalAppointments, color: '#1a73e8' },
            { label: "Today's Appointments", value: stats.todaysAppointmentCount, color: '#27ae60' },
            { label: 'Active Patients', value: stats.totalActivePatients, color: '#f39c12' },
            { label: 'Active Doctors', value: stats.totalActiveDoctors, color: '#9b59b6' },
          ].map((kpi) => (
            <div key={kpi.label} style={styles.kpiCard}>
              <div style={{ ...styles.kpiNum, color: kpi.color }}>{kpi.value}</div>
              <div style={styles.kpiLabel}>{kpi.label}</div>
            </div>
          ))}
        </div>

        <div style={styles.chartsRow}>
          {/* ── Resource Occupancy Bar Chart ── */}
          <div style={styles.chartCard}>
            <h3>Resource Occupancy</h3>
            {barData.length === 0 ? (
              <p style={{ color: '#999' }}>No resources added yet.</p>
            ) : (
              <ResponsiveContainer width="100%" height={260}>
                <BarChart data={barData}>
                  <XAxis dataKey="name" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="used" fill="#e74c3c" name="In Use" />
                  <Bar dataKey="available" fill="#27ae60" name="Available" />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>

          {/* ── Appointment Status Pie Chart ── */}
          <div style={styles.chartCard}>
            <h3>Appointment Breakdown</h3>
            {pieData.length === 0 ? (
              <p style={{ color: '#999' }}>No appointments yet.</p>
            ) : (
              <ResponsiveContainer width="100%" height={260}>
                <PieChart>
                  <Pie data={pieData} dataKey="value" nameKey="name"
                    cx="50%" cy="50%" outerRadius={90} label>
                    {pieData.map((_, i) => (
                      <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

const styles = {
  page: { maxWidth: 1000, margin: '0 auto', padding: '32px 16px' },
  kpiRow: { display: 'flex', gap: 16, marginBottom: 28, flexWrap: 'wrap' },
  kpiCard: {
    flex: '1 1 180px', background: '#fff', borderRadius: 10, padding: 20,
    textAlign: 'center', boxShadow: '0 2px 8px rgba(0,0,0,0.07)',
  },
  kpiNum: { fontSize: 36, fontWeight: 700 },
  kpiLabel: { color: '#666', marginTop: 4, fontSize: 14 },
  chartsRow: { display: 'flex', gap: 20, flexWrap: 'wrap' },
  chartCard: {
    flex: '1 1 400px', background: '#fff', borderRadius: 10, padding: 24,
    boxShadow: '0 2px 8px rgba(0,0,0,0.07)',
  },
};