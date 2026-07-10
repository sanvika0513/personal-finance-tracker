import api from './axiosConfig';

export const exportCsv = (month, year) =>
  api.get('/export/csv', { params: { month, year }, responseType: 'blob' });

export const exportPdf = (month, year) =>
  api.get('/export/pdf', { params: { month, year }, responseType: 'blob' });
