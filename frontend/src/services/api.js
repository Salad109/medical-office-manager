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

// Auth API
export async function login(credentials) {
    return apiFetch('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify(credentials),
    });
}

// Appointments API
export const getAvailableAppointments = (date) =>
    apiFetch(`/api/appointments/available?date=${date}`);

export const bookAppointment = (appointmentData) =>
    apiFetch('/api/appointments', {
        method: 'POST',
        body: JSON.stringify(appointmentData),
    });

export const getAppointments = (date) =>
    apiFetch(`/api/appointments/existing?date=${date}`);

export const getPatientAppointments = (patientId) =>
    apiFetch(`/api/appointments/patient/${patientId}`);

export const markAppointmentAsNoShow = (id) =>
    apiFetch(`/api/appointments/${id}/mark-no-show`, {
        method: 'POST',
    });

export const cancelAppointment = (id) =>
    apiFetch(`/api/appointments/${id}`, {
        method: 'DELETE',
    });

// Visits API (Doctor)
export const createVisit = (visitData) =>
    apiFetch('/api/visits', {
        method: 'POST',
        body: JSON.stringify(visitData),
    });

export const updateVisitNotes = (visitId, notes) =>
    apiFetch(`/api/visits/${visitId}`, {
        method: 'PUT',
        body: JSON.stringify({notes}),
    });

// Users API (Receptionist/Doctor)
export const getUsers = () => apiFetch('/api/users');

export const getPatientWithVisits = (patientId) =>
    apiFetch(`/api/users/${patientId}/with-visits`);

export const createUser = (userData) =>
    apiFetch('/api/users', {
        method: 'POST',
        body: JSON.stringify(userData),
    });

export const updateUser = (id, userData) =>
    apiFetch(`/api/users/${id}`, {
        method: 'PUT',
        body: JSON.stringify(userData),
    });

export default {
    login,
    getAvailableAppointments,
    bookAppointment,
    getAppointments,
    getPatientAppointments,
    markAppointmentAsNoShow,
    cancelAppointment,
    createVisit,
    updateVisitNotes,
    getUsers,
    getPatientWithVisits,
    createUser,
    updateUser,
};
