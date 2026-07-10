import api from './axiosConfig';

export const getBudgets = () => api.get('/budgets');
export const getBudgetStatus = (month, year) =>
  api.get('/budgets/status', { params: { month, year } });
export const createBudget = (data) => api.post('/budgets', data);
export const updateBudget = (id, data) => api.put(`/budgets/${id}`, data);
export const deleteBudget = (id) => api.delete(`/budgets/${id}`);
