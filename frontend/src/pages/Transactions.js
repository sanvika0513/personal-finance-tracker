import React, { useEffect, useState } from 'react';
import {
  getTransactions, createTransaction, updateTransaction, deleteTransaction,
} from '../api/transactionApi';

const emptyForm = { type: 'EXPENSE', category: '', amount: '', date: '', description: '' };

const Transactions = () => {
  const [transactions, setTransactions] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const loadTransactions = async () => {
    setLoading(true);
    try {
      const res = await getTransactions();
      setTransactions(res.data);
    } catch (err) {
      setError('Failed to load transactions.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadTransactions(); }, []);

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
    const payload = { ...form, amount: parseFloat(form.amount) };
    try {
      if (editingId) {
        await updateTransaction(editingId, payload);
      } else {
        await createTransaction(payload);
      }
      resetForm();
      loadTransactions();
    } catch (err) {
      const details = err.response?.data?.details;
      setError(details ? details.join(', ') : (err.response?.data?.message || 'Failed to save transaction.'));
    }
  };

  const handleEdit = (t) => {
    setForm({ type: t.type, category: t.category, amount: t.amount, date: t.date, description: t.description || '' });
    setEditingId(t.id);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this transaction?')) return;
    try {
      await deleteTransaction(id);
      loadTransactions();
    } catch (err) {
      setError('Failed to delete transaction.');
    }
  };

  return (
    <div className="page-container">
      <h1>Transactions</h1>
      {error && <div className="error-banner">{error}</div>}

      <form className="inline-form" onSubmit={handleSubmit}>
        <select name="type" value={form.type} onChange={handleChange}>
          <option value="EXPENSE">Expense</option>
          <option value="INCOME">Income</option>
        </select>
        <input name="category" placeholder="Category" value={form.category} onChange={handleChange} required />
        <input name="amount" type="number" step="0.01" min="0.01" placeholder="Amount" value={form.amount} onChange={handleChange} required />
        <input name="date" type="date" value={form.date} onChange={handleChange} required />
        <input name="description" placeholder="Description (optional)" value={form.description} onChange={handleChange} />
        <button type="submit">{editingId ? 'Update' : 'Add'}</button>
        {editingId && <button type="button" onClick={resetForm}>Cancel</button>}
      </form>

      {loading ? <p>Loading...</p> : (
        <table className="data-table">
          <thead>
            <tr>
              <th>Date</th><th>Type</th><th>Category</th><th>Amount</th><th>Description</th><th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {transactions.map((t) => (
              <tr key={t.id}>
                <td>{t.date}</td>
                <td className={t.type === 'INCOME' ? 'income-text' : 'expense-text'}>{t.type}</td>
                <td>{t.category}</td>
                <td>${Number(t.amount).toFixed(2)}</td>
                <td>{t.description}</td>
                <td>
                  <button onClick={() => handleEdit(t)}>Edit</button>
                  <button onClick={() => handleDelete(t.id)}>Delete</button>
                </td>
              </tr>
            ))}
            {transactions.length === 0 && (
              <tr><td colSpan="6" style={{ textAlign: 'center' }}>No transactions yet.</td></tr>
            )}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default Transactions;
