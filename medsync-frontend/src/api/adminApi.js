import axiosInstance from './axiosInstance';

// Dashboard
export const getDashboardStatsApi = () =>
  axiosInstance.get('/api/dashboard/stats');

// Resources
export const getAllResourcesApi = () =>
  axiosInstance.get('/api/resources');

export const createResourceApi = (name, totalCount) =>
  axiosInstance.post('/api/resources', null, { params: { name, totalCount } });

export const updateResourceApi = (id, data) =>
  axiosInstance.put(`/api/resources/${id}`, data);

export const deleteResourceApi = (id) =>
  axiosInstance.delete(`/api/resources/${id}`);

// Patients
export const getAllPatientsApi = () =>
  axiosInstance.get('/api/patients');

export const softDeletePatientApi = (id) =>
  axiosInstance.delete(`/api/patients/${id}`);

export const reactivatePatientApi = (id) =>
  axiosInstance.put(`/api/patients/${id}/reactivate`);