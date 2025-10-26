const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

const buildUrl = (path) => `${API_BASE_URL}${path}`;

async function apiFetch(path, options = {}) {
  const token = localStorage.getItem('token');
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(buildUrl(path), {
    ...options,
    headers,
  });

  const payload = await parsePayload(response);

  if (!response.ok) {
    const message = payload?.message || payload?.error || response.statusText || 'Request failed';
    throw new Error(message);
  }

  return payload;
}

async function parsePayload(response) {
  const text = await response.text();
  if (!text) {
    return null;
  }
  try {
    return JSON.parse(text);
  } catch (error) {
    console.error('Failed to parse response JSON', error);
    return null;
  }
}

export async function login(credentials) {
  return apiFetch('/auth/login', {
    method: 'POST',
    body: JSON.stringify(credentials),
  });
}

export const getAvailableAppointments = () => apiFetch('/api/appointments/available');
export const bookAppointment = (appointmentId) =>
  apiFetch(`/api/appointments/${appointmentId}/book`, {
    method: 'POST',
  });

export const getDoctorVisits = () => apiFetch('/api/doctor/visits/today');
export const updateVisitNotes = (visitId, notes) =>
  apiFetch(`/api/doctor/visits/${visitId}/notes`, {
    method: 'PUT',
    body: JSON.stringify({ notes }),
  });

export const getPatients = () => apiFetch('/api/patients');
export const createPatient = (patient) =>
  apiFetch('/api/patients', {
    method: 'POST',
    body: JSON.stringify(patient),
  });
export const updatePatient = (id, patient) =>
  apiFetch(`/api/patients/${id}`, {
    method: 'PUT',
    body: JSON.stringify(patient),
  });
export const deletePatient = (id) =>
  apiFetch(`/api/patients/${id}`, {
    method: 'DELETE',
  });

export const getAppointments = () => apiFetch('/api/appointments');
export const createAppointment = (appointment) =>
  apiFetch('/api/appointments', {
    method: 'POST',
    body: JSON.stringify(appointment),
  });
export const updateAppointment = (id, appointment) =>
  apiFetch(`/api/appointments/${id}`, {
    method: 'PUT',
    body: JSON.stringify(appointment),
  });
export const deleteAppointment = (id) =>
  apiFetch(`/api/appointments/${id}`, {
    method: 'DELETE',
  });

export default {
  login,
  getAvailableAppointments,
  bookAppointment,
  getDoctorVisits,
  updateVisitNotes,
  getPatients,
  createPatient,
  updatePatient,
  deletePatient,
  getAppointments,
  createAppointment,
  updateAppointment,
  deleteAppointment,
};
