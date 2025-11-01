import {useEffect, useState} from 'react';
import ErrorAlert from '../components/ErrorAlert';
import LoadingSpinner from '../components/LoadingSpinner';
import {
    createAppointment,
    createPatient,
    deleteAppointment,
    deletePatient,
    getAppointments,
    getPatients,
    updateAppointment,
    updatePatient,
} from '../services/api';

const createPatientFormState = () => ({id: null, firstName: '', lastName: '', email: '', phone: ''});
const createAppointmentFormState = () => ({
    id: null,
    patientId: '',
    doctorId: '',
    dateTime: '',
    reason: '',
});

const ReceptionistDashboard = () => {
    const [patients, setPatients] = useState([]);
    const [appointments, setAppointments] = useState([]);
    const [patientForm, setPatientForm] = useState(createPatientFormState);
    const [appointmentForm, setAppointmentForm] = useState(createAppointmentFormState);
    const [loadingPatients, setLoadingPatients] = useState(true);
    const [loadingAppointments, setLoadingAppointments] = useState(true);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    useEffect(() => {
        loadPatients();
        loadAppointments();
    }, []);

    const handleError = (message) => {
        setError(message);
        setSuccess('');
    };

    const handleSuccess = (message) => {
        setSuccess(message);
        setError('');
    };

    const loadPatients = async () => {
        setLoadingPatients(true);
        try {
            const data = await getPatients();
            setPatients(Array.isArray(data) ? data : []);
        } catch (err) {
            handleError(err.message || 'Failed to load patients.');
        } finally {
            setLoadingPatients(false);
        }
    };

    const loadAppointments = async () => {
        setLoadingAppointments(true);
        try {
            const data = await getAppointments();
            setAppointments(Array.isArray(data) ? data : []);
        } catch (err) {
            handleError(err.message || 'Failed to load appointments.');
        } finally {
            setLoadingAppointments(false);
        }
    };

    const handlePatientSubmit = async (event) => {
        event.preventDefault();
        try {
            if (patientForm.id) {
                await updatePatient(patientForm.id, patientForm);
                handleSuccess('Patient updated successfully.');
            } else {
                await createPatient(patientForm);
                handleSuccess('Patient created successfully.');
            }
            setPatientForm(createPatientFormState());
            await loadPatients();
        } catch (err) {
            handleError(err.message || 'Unable to save patient.');
        }
    };

    const handlePatientDelete = async (id) => {
        if (!window.confirm('Delete this patient?')) {
            return;
        }
        try {
            await deletePatient(id);
            handleSuccess('Patient removed.');
            await loadPatients();
        } catch (err) {
            handleError(err.message || 'Unable to delete patient.');
        }
    };

    const handleAppointmentSubmit = async (event) => {
        event.preventDefault();
        try {
            if (appointmentForm.id) {
                await updateAppointment(appointmentForm.id, appointmentForm);
                handleSuccess('Appointment updated successfully.');
            } else {
                await createAppointment(appointmentForm);
                handleSuccess('Appointment created successfully.');
            }
            setAppointmentForm(createAppointmentFormState());
            await loadAppointments();
        } catch (err) {
            handleError(err.message || 'Unable to save appointment.');
        }
    };

    const handleAppointmentDelete = async (id) => {
        if (!window.confirm('Delete this appointment?')) {
            return;
        }
        try {
            await deleteAppointment(id);
            handleSuccess('Appointment removed.');
            await loadAppointments();
        } catch (err) {
            handleError(err.message || 'Unable to delete appointment.');
        }
    };

    return (
        <div className="d-grid gap-4">
            {(error || success) && (
                <div className="glassy-card shadow-sm p-3">
                    {error && <ErrorAlert message={error} onClose={() => setError('')}/>}
                    {success && !error && (
                        <div className="alert alert-success alert-dismissible fade show" role="alert">
                            {success}
                            <button type="button" className="btn-close" onClick={() => setSuccess('')}
                                    aria-label="Close"/>
                        </div>
                    )}
                </div>
            )}
            <div className="glassy-card shadow-sm p-4">
                <div
                    className="d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3 mb-4">
                    <div>
                        <h2 className="h4 fw-bold text-primary mb-1">Patients</h2>
                        <p className="text-muted mb-0">Create, update, or remove patient records.</p>
                    </div>
                    <button type="button" className="btn btn-outline-primary" onClick={loadPatients}
                            disabled={loadingPatients}>
                        Refresh
                    </button>
                </div>
                <form className="row g-3 mb-4" onSubmit={handlePatientSubmit}>
                    <div className="col-md-6">
                        <label className="form-label">First name</label>
                        <input
                            type="text"
                            className="form-control"
                            value={patientForm.firstName}
                            onChange={(event) => setPatientForm((prev) => ({...prev, firstName: event.target.value}))}
                            required
                        />
                    </div>
                    <div className="col-md-6">
                        <label className="form-label">Last name</label>
                        <input
                            type="text"
                            className="form-control"
                            value={patientForm.lastName}
                            onChange={(event) => setPatientForm((prev) => ({...prev, lastName: event.target.value}))}
                            required
                        />
                    </div>
                    <div className="col-md-6">
                        <label className="form-label">Email</label>
                        <input
                            type="email"
                            className="form-control"
                            value={patientForm.email}
                            onChange={(event) => setPatientForm((prev) => ({...prev, email: event.target.value}))}
                            required
                        />
                    </div>
                    <div className="col-md-6">
                        <label className="form-label">Phone</label>
                        <input
                            type="tel"
                            className="form-control"
                            value={patientForm.phone}
                            onChange={(event) => setPatientForm((prev) => ({...prev, phone: event.target.value}))}
                            required
                        />
                    </div>
                    <div className="col-12 d-flex justify-content-between gap-2">
                        <button type="button" className="btn btn-outline-secondary"
                                onClick={() => setPatientForm(createPatientFormState())}>
                            Clear
                        </button>
                        <button type="submit" className="btn btn-primary">
                            {patientForm.id ? 'Update patient' : 'Add patient'}
                        </button>
                    </div>
                </form>
                {loadingPatients ? (
                    <LoadingSpinner message="Loading patients"/>
                ) : patients.length === 0 ? (
                    <p className="text-center text-muted">No patients yet.</p>
                ) : (
                    <div className="table-responsive rounded-4 overflow-hidden shadow-sm">
                        <table className="table align-middle">
                            <thead className="table-light">
                            <tr>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Phone</th>
                                <th className="text-end">Actions</th>
                            </tr>
                            </thead>
                            <tbody>
                            {patients.map((patient) => (
                                <tr key={patient.id}>
                                    <td>
                                        <div className="fw-semibold">{patient.firstName} {patient.lastName}</div>
                                        <div className="text-muted small">#{patient.id}</div>
                                    </td>
                                    <td>{patient.email}</td>
                                    <td>{patient.phone}</td>
                                    <td className="text-end d-flex gap-2 justify-content-end">
                                        <button
                                            type="button"
                                            className="btn btn-outline-primary btn-sm"
                                            onClick={() =>
                                                setPatientForm({
                                                    id: patient.id,
                                                    firstName: patient.firstName || '',
                                                    lastName: patient.lastName || '',
                                                    email: patient.email || '',
                                                    phone: patient.phone || '',
                                                })
                                            }
                                        >
                                            Edit
                                        </button>
                                        <button type="button" className="btn btn-outline-danger btn-sm"
                                                onClick={() => handlePatientDelete(patient.id)}>
                                            Delete
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
            <div className="glassy-card shadow-sm p-4">
                <div
                    className="d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3 mb-4">
                    <div>
                        <h2 className="h4 fw-bold text-primary mb-1">Appointments</h2>
                        <p className="text-muted mb-0">Manage bookings for every doctor.</p>
                    </div>
                    <button type="button" className="btn btn-outline-primary" onClick={loadAppointments}
                            disabled={loadingAppointments}>
                        Refresh
                    </button>
                </div>
                <form className="row g-3 mb-4" onSubmit={handleAppointmentSubmit}>
                    <div className="col-md-6">
                        <label className="form-label">Patient ID</label>
                        <input
                            type="number"
                            className="form-control"
                            value={appointmentForm.patientId}
                            onChange={(event) => setAppointmentForm((prev) => ({
                                ...prev,
                                patientId: event.target.value
                            }))}
                            required
                        />
                    </div>
                    <div className="col-md-6">
                        <label className="form-label">Doctor ID</label>
                        <input
                            type="number"
                            className="form-control"
                            value={appointmentForm.doctorId}
                            onChange={(event) => setAppointmentForm((prev) => ({
                                ...prev,
                                doctorId: event.target.value
                            }))}
                            required
                        />
                    </div>
                    <div className="col-md-6">
                        <label className="form-label">Date &amp; time</label>
                        <input
                            type="datetime-local"
                            className="form-control"
                            value={appointmentForm.dateTime}
                            onChange={(event) => setAppointmentForm((prev) => ({
                                ...prev,
                                dateTime: event.target.value
                            }))}
                            required
                        />
                    </div>
                    <div className="col-md-6">
                        <label className="form-label">Reason</label>
                        <input
                            type="text"
                            className="form-control"
                            value={appointmentForm.reason}
                            onChange={(event) => setAppointmentForm((prev) => ({...prev, reason: event.target.value}))}
                        />
                    </div>
                    <div className="col-12 d-flex justify-content-between gap-2">
                        <button type="button" className="btn btn-outline-secondary"
                                onClick={() => setAppointmentForm(createAppointmentFormState())}>
                            Clear
                        </button>
                        <button type="submit" className="btn btn-primary">
                            {appointmentForm.id ? 'Update appointment' : 'Add appointment'}
                        </button>
                    </div>
                </form>
                {loadingAppointments ? (
                    <LoadingSpinner message="Loading appointments"/>
                ) : appointments.length === 0 ? (
                    <p className="text-center text-muted">No appointments found.</p>
                ) : (
                    <div className="table-responsive rounded-4 overflow-hidden shadow-sm">
                        <table className="table align-middle">
                            <thead className="table-light">
                            <tr>
                                <th>Patient</th>
                                <th>Doctor</th>
                                <th>Date</th>
                                <th>Reason</th>
                                <th className="text-end">Actions</th>
                            </tr>
                            </thead>
                            <tbody>
                            {appointments.map((appointment) => (
                                <tr key={appointment.id}>
                                    <td>
                                        <div
                                            className="fw-semibold">{appointment.patientName || appointment.patientId}</div>
                                        <div className="text-muted small">#{appointment.patientId}</div>
                                    </td>
                                    <td>
                                        <div
                                            className="fw-semibold">{appointment.doctorName || appointment.doctorId}</div>
                                        <div className="text-muted small">#{appointment.doctorId}</div>
                                    </td>
                                    <td>{new Date(appointment.dateTime || appointment.date || Date.now()).toLocaleString()}</td>
                                    <td className="text-muted">{appointment.reason || 'General visit'}</td>
                                    <td className="text-end d-flex gap-2 justify-content-end">
                                        <button
                                            type="button"
                                            className="btn btn-outline-primary btn-sm"
                                            onClick={() => {
                                                const isoDate = appointment.dateTime || appointment.date || '';
                                                const normalized = isoDate ? isoDate.replace(' ', 'T').slice(0, 16) : '';
                                                setAppointmentForm({
                                                    id: appointment.id,
                                                    patientId: String(appointment.patientId ?? ''),
                                                    doctorId: String(appointment.doctorId ?? ''),
                                                    dateTime: normalized,
                                                    reason: appointment.reason || '',
                                                });
                                            }}
                                        >
                                            Edit
                                        </button>
                                        <button type="button" className="btn btn-outline-danger btn-sm"
                                                onClick={() => handleAppointmentDelete(appointment.id)}>
                                            Delete
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
};

export default ReceptionistDashboard;
