import api from './axiosConfig';

export const getTransactions = (start, end) => {
  const params = {};
  if (start) params.start = start;
  if (end) params.end = end;
  return api.get('/transactions', { params });
};

export const createTransaction = (data) => api.post('/transactions', data);
export const updateTransaction = (id, data) => api.put(`/transactions/${id}`, data);
export const deleteTransaction = (id) => api.delete(`/transactions/${id}`);
