import React, { useEffect, useState } from 'react';
import { getBudgets, createBudget, updateBudget, deleteBudget } from '../api/budgetApi';

const today = new Date();
const emptyForm = { category: '', monthlyLimit: '', month: today.getMonth() + 1, year: today.getFullYear() };

const Budgets = () => {
  const [budgets, setBudgets] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const loadBudgets = async () => {
    setLoading(true);
    try {
      const res = await getBudgets();
      setBudgets(res.data);
    } catch (err) {
      setError('Failed to load budgets.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadBudgets(); }, []);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const resetForm = () => {
    setForm(emptyForm);
    setEditingId(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const payload = {
      ...form,
      monthlyLimit: parseFloat(form.monthlyLimit),
      month: parseInt(form.month, 10),
      year: parseInt(form.year, 10),
    };
    try {
      if (editingId) {
        await updateBudget(editingId, payload);
      } else {
        await createBudget(payload);
      }
      resetForm();
      loadBudgets();
    } catch (err) {
      const details = err.response?.data?.details;
      setError(details ? details.join(', ') : (err.response?.data?.message || 'Failed to save budget.'));
    }
  };

  const handleEdit = (b) => {
    setForm({ category: b.category, monthlyLimit: b.monthlyLimit, month: b.month, year: b.year });
    setEditingId(b.id);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this budget?')) return;
    try {
      await deleteBudget(id);
      loadBudgets();
    } catch (err) {
      setError('Failed to delete budget.');
    }
  };

  return (
    <div className="page-container">
      <h1>Budgets</h1>
      {error && <div className="error-banner">{error}</div>}

      <form className="inline-form" onSubmit={handleSubmit}>
        <input name="category" placeholder="Category" value={form.category} onChange={handleChange} required />
        <input name="monthlyLimit" type="number" step="0.01" min="0.01" placeholder="Monthly Limit" value={form.monthlyLimit} onChange={handleChange} required />
        <input name="month" type="number" min="1" max="12" placeholder="Month" value={form.month} onChange={handleChange} required />
        <input name="year" type="number" placeholder="Year" value={form.year} onChange={handleChange} required />
        <button type="submit">{editingId ? 'Update' : 'Add'}</button>
        {editingId && <button type="button" onClick={resetForm}>Cancel</button>}
      </form>

      {loading ? <p>Loading...</p> : (
        <table className="data-table">
          <thead>
            <tr><th>Category</th><th>Monthly Limit</th><th>Month</th><th>Year</th><th>Actions</th></tr>
          </thead>
          <tbody>
            {budgets.map((b) => (
              <tr key={b.id}>
                <td>{b.category}</td>
                <td>${Number(b.monthlyLimit).toFixed(2)}</td>
                <td>{b.month}</td>
                <td>{b.year}</td>
                <td>
                  <button onClick={() => handleEdit(b)}>Edit</button>
                  <button onClick={() => handleDelete(b.id)}>Delete</button>
                </td>
              </tr>
            ))}
            {budgets.length === 0 && (
              <tr><td colSpan="5" style={{ textAlign: 'center' }}>No budgets set yet.</td></tr>
            )}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default Budgets;
