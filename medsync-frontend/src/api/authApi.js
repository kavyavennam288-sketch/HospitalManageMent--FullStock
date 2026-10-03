import axiosInstance from './axiosInstance';

export const loginApi = (email, password) =>
  axiosInstance.post('/api/auth/login', { email, password });

export const registerApi = (data) =>
  axiosInstance.post('/api/auth/register', data);