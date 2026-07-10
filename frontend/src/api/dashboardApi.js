import api from './axiosConfig';

export const getDashboardSummary = (month, year) =>
  api.get('/dashboard/summary', { params: { month, year } });
