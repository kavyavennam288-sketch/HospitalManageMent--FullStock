import axiosInstance from './axiosInstance';

// Patient books an appointment
export const bookAppointmentApi = (doctorId, startTime) =>
  axiosInstance.post('/api/appointments', { doctorId, startTime });

// Patient views their own appointments
export const getMyAppointmentsApi = () =>
  axiosInstance.get('/api/appointments/my');

// Doctor views their schedule
export const getDoctorAppointmentsApi = () =>
  axiosInstance.get('/api/appointments/doctor');

// Cancel an appointment (patient or admin)
export const cancelAppointmentApi = (id) =>
  axiosInstance.delete(`/api/appointments/${id}`);