import { useEffect, useState } from 'react';
import {
  getAllResourcesApi, createResourceApi,
  updateResourceApi, deleteResourceApi,
} from '../../api/adminApi';
import Navbar from '../../components/Navbar';

export default function ResourceManager() {
  const [resources, setResources] = useState([]);
  const [form, setForm] = useState({ name: '', totalCount: '' });
  const [error, setError] = useState('');

  const fetchResources = () =>
    getAllResourcesApi().then((res) => setResources(res.data));

  useEffect(() => { fetchResources(); }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await createResourceApi(form.name, Number(form.totalCount));
      setForm({ name: '', totalCount: '' });
      fetchResources();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create resource.');
    }
  };

  const handleUpdateUsed = async (id, usedCount) => {
    try {
      await updateResourceApi(id, { usedCount: Number(usedCount) });
      fetchResources();
    } catch (err) {
      alert(err.response?.data?.message || 'Update failed.');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this resource?')) return;
    await deleteResourceApi(id);
    fetchResources();
  };

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2>Resource Manager</h2>

        {/* Create resource form */}
        <div style={styles.card}>
          <h3>Add New Resource</h3>
          {error && <div style={styles.error}>{error}</div>}
          <form onSubmit={handleCreate} style={styles.row}>
            <input style={styles.input} placeholder="Resource name (e.g. ICU Beds)"
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required />
            <input style={{ ...styles.input, width: 140 }} type="number"
              placeholder="Total count" min={1}
              value={form.totalCount}
              onChange={(e) => setForm({ ...form, totalCount: e.target.value })}
              required />
            <button type="submit" style={styles.btn}>+ Add</button>
          </form>
        </div>

        {/* Resource list */}
        {resources.map((r) => (
          <div key={r.id} style={styles.resourceCard}>
            <div style={styles.resourceHeader}>
              <strong>{r.name}</strong>
              <button style={styles.deleteBtn} onClick={() => handleDelete(r.id)}>
                Delete
              </button>
            </div>

            {/* Occupancy bar */}
            <div style={styles.barBg}>
              <div style={{
                ...styles.barFill,
                width: `${r.occupancyRate}%`,
                background: r.occupancyRate > 80 ? '#e74c3c'
                  : r.occupancyRate > 50 ? '#f39c12' : '#27ae60',
              }} />
            </div>

            <div style={styles.resourceStats}>
              <span>Used: <strong>{r.usedCount}</strong></span>
              <span>Available: <strong>{r.availableCount}</strong></span>
              <span>Total: <strong>{r.totalCount}</strong></span>
              <span>Occupancy: <strong>{r.occupancyRate}%</strong></span>
            </div>

            {/* Inline update used count */}
            <div style={styles.updateRow}>
              <span style={{ fontSize: 14 }}>Update used count:</span>
              <input type="number" min={0} max={r.totalCount}
                defaultValue={r.usedCount}
                style={{ ...styles.input, width: 80 }}
                onBlur={(e) => handleUpdateUsed(r.id, e.target.value)} />
              <span style={{ fontSize: 12, color: '#888' }}>(click away to save)</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

const styles = {
  page: { maxWidth: 800, margin: '0 auto', padding: '32px 16px' },
  card: {
    background: '#fff', borderRadius: 10, padding: 24, marginBottom: 20,
    boxShadow: '0 2px 8px rgba(0,0,0,0.07)',
  },
  row: { display: 'flex', gap: 12, alignItems: 'center', flexWrap: 'wrap' },
  input: {
    padding: '10px 12px', border: '1px solid #ddd',
    borderRadius: 6, fontSize: 15, flex: 1,
  },
  btn: {
    background: '#1a73e8', color: '#fff', border: 'none',
    padding: '10px 18px', borderRadius: 6, cursor: 'pointer', fontWeight: 600,
  },
  error: {
    background: '#fdecea', color: '#c0392b', padding: '10px 14px',
    borderRadius: 6, marginBottom: 12,
  },
  resourceCard: {
    background: '#fff', borderRadius: 10, padding: 20, marginBottom: 16,
    boxShadow: '0 2px 8px rgba(0,0,0,0.07)',
  },
  resourceHeader: {
    display: 'flex', justifyContent: 'space-between', marginBottom: 12,
  },
  barBg: {
    background: '#f0f0f0', borderRadius: 20, height: 10, marginBottom: 10,
  },
  barFill: { height: 10, borderRadius: 20, transition: 'width 0.4s' },
  resourceStats: {
    display: 'flex', gap: 20, fontSize: 14, color: '#555', marginBottom: 12,
  },
  updateRow: { display: 'flex', alignItems: 'center', gap: 10 },
  deleteBtn: {
    background: '#fdecea', color: '#c0392b', border: '1px solid #e74c3c',
    padding: '5px 12px', borderRadius: 6, cursor: 'pointer',
  },
};