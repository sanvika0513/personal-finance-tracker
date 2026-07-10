import React, { useEffect, useState, useCallback } from 'react';
import {
  PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer,
  BarChart, Bar, XAxis, YAxis, CartesianGrid
} from 'recharts';
import { getDashboardSummary } from '../api/dashboardApi';
import { exportCsv, exportPdf } from '../api/exportApi';

const COLORS = ['#4f46e5', '#22c55e', '#f59e0b', '#ef4444', '#06b6d4', '#a855f7', '#ec4899', '#84cc16'];

const monthNames = ['January','February','March','April','May','June','July','August','September','October','November','December'];

const Dashboard = () => {
  const today = new Date();
  const [month, setMonth] = useState(today.getMonth() + 1);
  const [year, setYear] = useState(today.getFullYear());
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadSummary = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await getDashboardSummary(month, year);
      setSummary(res.data);
    } catch (err) {
      setError('Failed to load dashboard data.');
    } finally {
      setLoading(false);
    }
  }, [month, year]);

  useEffect(() => {
    loadSummary();
  }, [loadSummary]);

  const handleExport = async (type) => {
    try {
      const res = type === 'csv' ? await exportCsv(month, year) : await exportPdf(month, year);
      const blob = new Blob([res.data]);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `expense-report-${year}-${String(month).padStart(2, '0')}.${type}`;
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      alert('Export failed. Please try again.');
    }
  };

  const expensePieData = summary
    ? Object.entries(summary.expensesByCategory || {}).map(([name, value]) => ({ name, value }))
    : [];

  const budgetBarData = summary
    ? (summary.budgetStatuses || []).map((b) => ({
        category: b.category,
        Limit: b.monthlyLimit,
        Spent: b.spent,
      }))
    : [];

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>Dashboard</h1>
        <div className="filters">
          <select value={month} onChange={(e) => setMonth(Number(e.target.value))}>
            {monthNames.map((m, i) => <option key={m} value={i + 1}>{m}</option>)}
          </select>
          <select value={year} onChange={(e) => setYear(Number(e.target.value))}>
            {[year - 1, year, year + 1].map((y) => <option key={y} value={y}>{y}</option>)}
          </select>
          <button onClick={() => handleExport('csv')}>Export CSV</button>
          <button onClick={() => handleExport('pdf')}>Export PDF</button>
        </div>
      </div>

      {loading && <p>Loading...</p>}
      {error && <div className="error-banner">{error}</div>}

      {summary && !loading && (
        <>
          <div className="summary-cards">
            <div className="card income">
              <span>Total Income</span>
              <strong>${Number(summary.totalIncome).toFixed(2)}</strong>
            </div>
            <div className="card expense">
              <span>Total Expense</span>
              <strong>${Number(summary.totalExpense).toFixed(2)}</strong>
            </div>
            <div className={`card balance ${summary.balance < 0 ? 'negative' : ''}`}>
              <span>Balance</span>
              <strong>${Number(summary.balance).toFixed(2)}</strong>
            </div>
          </div>

          <div className="charts-grid">
            <div className="chart-box">
              <h3>Expenses by Category</h3>
              {expensePieData.length === 0 ? <p>No expense data for this month.</p> : (
                <ResponsiveContainer width="100%" height={300}>
                  <PieChart>
                    <Pie data={expensePieData} dataKey="value" nameKey="name" outerRadius={100} label>
                      {expensePieData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              )}
            </div>

            <div className="chart-box">
              <h3>Budget vs Spent</h3>
              {budgetBarData.length === 0 ? <p>No budgets set for this month.</p> : (
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={budgetBarData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="category" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Bar dataKey="Limit" fill="#4f46e5" />
                    <Bar dataKey="Spent" fill="#ef4444" />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </div>
          </div>

          <div className="budget-alerts">
            {(summary.budgetStatuses || []).filter(b => b.overBudget).map((b) => (
              <div key={b.category} className="alert-over-budget">
                ⚠️ You are over budget in <strong>{b.category}</strong>: spent ${Number(b.spent).toFixed(2)} of ${Number(b.monthlyLimit).toFixed(2)}
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
};

export default Dashboard;
