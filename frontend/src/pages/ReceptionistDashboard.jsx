import {useCallback, useEffect, useState} from 'react';
import ErrorAlert from '../components/ErrorAlert';
import LoadingSpinner from '../components/LoadingSpinner';
import {
    bookAppointment,
    cancelAppointment,
    createUser,
    getAppointments,
    getUsers,
    markAppointmentAsNoShow,
    updateUser,
} from '../services/api';

const createUserFormState = () => ({
    id: null,
    username: '',
    password: '',
    firstName: '',
    lastName: '',
    phoneNumber: '',
    pesel: '',
    role: 'PATIENT'
});

const createAppointmentFormState = () => ({
    patientId: '',
    date: '',
    time: '09:00',
});

const ReceptionistDashboard = () => {
    const [users, setUsers] = useState([]);
    const [appointments, setAppointments] = useState([]);
    const [userForm, setUserForm] = useState(createUserFormState);
    const [appointmentForm, setAppointmentForm] = useState(createAppointmentFormState);
    const [selectedDate, setSelectedDate] = useState('');
    const [loadingUsers, setLoadingUsers] = useState(true);
    const [loadingAppointments, setLoadingAppointments] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const handleError = (message) => {
        setError(message);
        setSuccess('');
    };

    const handleSuccess = (message) => {
        setSuccess(message);
        setError('');
    };

    const loadUsers = useCallback(async () => {
        setLoadingUsers(true);
        try {
            const data = await getUsers();
            // Handle paginated response
            const userList = data?.content || (Array.isArray(data) ? data : []);
            setUsers(userList);
        } catch (err) {
            handleError(err.message || 'Failed to load users.');
        } finally {
            setLoadingUsers(false);
        }
    }, []);

    const loadAppointments = useCallback(async () => {
        setLoadingAppointments(true);
        try {
            const data = await getAppointments(selectedDate);
            setAppointments(Array.isArray(data) ? data : []);
        } catch (err) {
            handleError(err.message || 'Failed to load appointments.');
        } finally {
            setLoadingAppointments(false);
        }
    }, [selectedDate]);

    useEffect(() => {
        const today = new Date().toISOString().split('T')[0];
        setSelectedDate(today);
        loadUsers();
    }, [loadUsers]);

    useEffect(() => {
        if (selectedDate) {
            loadAppointments();
        }
    }, [selectedDate, loadAppointments]);

    const handleUserSubmit = async (event) => {
        event.preventDefault();
        try {
            if (userForm.id) {
                await updateUser(userForm.id, userForm);
                handleSuccess('User updated successfully.');
            } else {
                await createUser(userForm);
                handleSuccess('User created successfully.');
            }
            setUserForm(createUserFormState());
            await loadUsers();
        } catch (err) {
            handleError(err.message || 'Unable to save user.');
        }
    };

    const handleAppointmentSubmit = async (event) => {
        event.preventDefault();
        try {
            await bookAppointment(appointmentForm);
            handleSuccess('Appointment created successfully.');
            setAppointmentForm(createAppointmentFormState());
            await loadAppointments();
        } catch (err) {
            handleError(err.message || 'Unable to create appointment.');
        }
    };

    const handleAppointmentCancel = async (id) => {
        if (!window.confirm('Cancel this appointment?')) {
            return;
        }
        try {
            await cancelAppointment(id);
            handleSuccess('Appointment cancelled.');
            await loadAppointments();
        } catch (err) {
            handleError(err.message || 'Unable to cancel appointment.');
        }
    };

    const handleMarkAsNoShow = async (id) => {
        if (!window.confirm('Mark this appointment as no-show?')) {
            return;
        }
        try {
            await markAppointmentAsNoShow(id);
            handleSuccess('Appointment marked as no-show.');
            await loadAppointments();
        } catch (err) {
            handleError(err.message || 'Unable to mark as no-show.');
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
                        <h2 className="h4 fw-bold text-primary mb-1">Users</h2>
                        <p className="text-muted mb-0">Create and manage user accounts (patients, doctors,
                            receptionists).</p>
                    </div>
                    <button type="button" className="btn btn-outline-primary" onClick={loadUsers}
                            disabled={loadingUsers}>
                        Refresh
                    </button>
                </div>
                <form className="row g-3 mb-4" onSubmit={handleUserSubmit}>
                    <div className="col-md-4">
                        <label className="form-label">Username*</label>
                        <input
                            type="text"
                            className="form-control"
                            value={userForm.username}
                            onChange={(e) => setUserForm((prev) => ({...prev, username: e.target.value}))}
                            required
                        />
                    </div>
                    <div className="col-md-4">
                        <label className="form-label">Password{userForm.id ? '' : '*'}</label>
                        <input
                            type="password"
                            className="form-control"
                            value={userForm.password}
                            onChange={(e) => setUserForm((prev) => ({...prev, password: e.target.value}))}
                            required={!userForm.id}
                            placeholder={userForm.id ? 'Leave blank to keep current' : ''}
                        />
                    </div>
                    <div className="col-md-4">
                        <label className="form-label">Role*</label>
                        <select
                            className="form-select"
                            value={userForm.role}
                            onChange={(e) => setUserForm((prev) => ({...prev, role: e.target.value}))}
                            required
                        >
                            <option value="PATIENT">Patient</option>
                            <option value="DOCTOR">Doctor</option>
                            <option value="RECEPTIONIST">Receptionist</option>
                        </select>
                    </div>
                    <div className="col-md-6">
                        <label className="form-label">First Name*</label>
                        <input
                            type="text"
                            className="form-control"
                            value={userForm.firstName}
                            onChange={(e) => setUserForm((prev) => ({...prev, firstName: e.target.value}))}
                            required
                        />
                    </div>
                    <div className="col-md-6">
                        <label className="form-label">Last Name*</label>
                        <input
                            type="text"
                            className="form-control"
                            value={userForm.lastName}
                            onChange={(e) => setUserForm((prev) => ({...prev, lastName: e.target.value}))}
                            required
                        />
                    </div>
                    <div className="col-md-6">
                        <label className="form-label">Phone Number*</label>
                        <input
                            type="tel"
                            className="form-control"
                            value={userForm.phoneNumber}
                            onChange={(e) => setUserForm((prev) => ({...prev, phoneNumber: e.target.value}))}
                            required
                        />
                    </div>
                    <div className="col-md-6">
                        <label
                            className="form-label">PESEL {userForm.role === 'PATIENT' ? '*' : '(Patient only)'}</label>
                        <input
                            type="text"
                            className="form-control"
                            value={userForm.pesel}
                            onChange={(e) => setUserForm((prev) => ({...prev, pesel: e.target.value}))}
                            required={userForm.role === 'PATIENT'}
                            maxLength="11"
                            placeholder="11 digits"
                        />
                    </div>
                    <div className="col-12 d-flex justify-content-between gap-2">
                        <button type="button" className="btn btn-outline-secondary"
                                onClick={() => setUserForm(createUserFormState())}>
                            Clear
                        </button>
                        <button type="submit" className="btn btn-primary">
                            {userForm.id ? 'Update user' : 'Add user'}
                        </button>
                    </div>
                </form>
                {loadingUsers ? (
                    <LoadingSpinner message="Loading users"/>
                ) : users.length === 0 ? (
                    <p className="text-center text-muted">No users yet.</p>
                ) : (
                    <div className="table-responsive rounded-4 overflow-hidden shadow-sm">
                        <table className="table align-middle">
                            <thead className="table-light">
                            <tr>
                                <th>Name</th>
                                <th>Username</th>
                                <th>Role</th>
                                <th>Phone</th>
                                <th className="text-end">Actions</th>
                            </tr>
                            </thead>
                            <tbody>
                            {users.map((user) => (
                                <tr key={user.id}>
                                    <td>
                                        <div className="fw-semibold">{user.firstName} {user.lastName}</div>
                                        <div className="text-muted small">ID: {user.id}</div>
                                    </td>
                                    <td>{user.username}</td>
                                    <td><span className="badge text-bg-secondary">{user.role}</span></td>
                                    <td>{user.phoneNumber}</td>
                                    <td className="text-end">
                                        <button
                                            type="button"
                                            className="btn btn-outline-primary btn-sm"
                                            onClick={() =>
                                                setUserForm({
                                                    id: user.id,
                                                    username: user.username || '',
                                                    password: '',
                                                    firstName: user.firstName || '',
                                                    lastName: user.lastName || '',
                                                    phoneNumber: user.phoneNumber || '',
                                                    pesel: user.pesel || '',
                                                    role: user.role || 'PATIENT',
                                                })
                                            }
                                        >
                                            Edit
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
                <div className="mb-4">
                    <label htmlFor="appointmentDate" className="form-label fw-semibold">View Date</label>
                    <input
                        type="date"
                        id="appointmentDate"
                        className="form-control"
                        value={selectedDate}
                        onChange={(e) => setSelectedDate(e.target.value)}
                    />
                </div>
                <form className="row g-3 mb-4" onSubmit={handleAppointmentSubmit}>
                    <div className="col-md-4">
                        <label className="form-label">Patient ID*</label>
                        <input
                            type="number"
                            className="form-control"
                            value={appointmentForm.patientId}
                            onChange={(e) => setAppointmentForm((prev) => ({
                                ...prev,
                                patientId: e.target.value
                            }))}
                            required
                        />
                    </div>
                    <div className="col-md-4">
                        <label className="form-label">Date*</label>
                        <input
                            type="date"
                            className="form-control"
                            value={appointmentForm.date}
                            onChange={(e) => setAppointmentForm((prev) => ({
                                ...prev,
                                date: e.target.value
                            }))}
                            required
                        />
                    </div>
                    <div className="col-md-4">
                        <label className="form-label">Time*</label>
                        <select
                            className="form-select"
                            value={appointmentForm.time}
                            onChange={(e) => setAppointmentForm((prev) => ({
                                ...prev,
                                time: e.target.value
                            }))}
                            required
                        >
                            <option value="09:00">09:00</option>
                            <option value="09:30">09:30</option>
                            <option value="10:00">10:00</option>
                            <option value="10:30">10:30</option>
                            <option value="11:00">11:00</option>
                            <option value="11:30">11:30</option>
                            <option value="12:00">12:00</option>
                            <option value="12:30">12:30</option>
                            <option value="13:00">13:00</option>
                            <option value="13:30">13:30</option>
                            <option value="14:00">14:00</option>
                            <option value="14:30">14:30</option>
                            <option value="15:00">15:00</option>
                            <option value="15:30">15:30</option>
                            <option value="16:00">16:00</option>
                            <option value="16:30">16:30</option>
                        </select>
                    </div>
                    <div className="col-12 d-flex justify-content-between gap-2">
                        <button type="button" className="btn btn-outline-secondary"
                                onClick={() => setAppointmentForm(createAppointmentFormState())}>
                            Clear
                        </button>
                        <button type="submit" className="btn btn-primary">
                            Book appointment
                        </button>
                    </div>
                </form>
                {loadingAppointments ? (
                    <LoadingSpinner message="Loading appointments"/>
                ) : appointments.length === 0 ? (
                    <p className="text-center text-muted">No appointments found for {selectedDate}.</p>
                ) : (
                    <div className="table-responsive rounded-4 overflow-hidden shadow-sm">
                        <table className="table align-middle">
                            <thead className="table-light">
                            <tr>
                                <th>Time</th>
                                <th>Patient</th>
                                <th>Phone</th>
                                <th>Status</th>
                                <th className="text-end">Actions</th>
                            </tr>
                            </thead>
                            <tbody>
                            {appointments.map((appointment) => (
                                <tr key={appointment.id}>
                                    <td className="fw-semibold">{appointment.time}</td>
                                    <td>
                                        <div className="fw-semibold">
                                            {appointment.patientFirstName} {appointment.patientLastName}
                                        </div>
                                        <div className="text-muted small">ID: {appointment.patientId}</div>
                                    </td>
                                    <td>{appointment.patientPhoneNumber}</td>
                                    <td>
                                        <span className={`badge ${
                                            appointment.status === 'COMPLETED' ? 'text-bg-success' :
                                                appointment.status === 'NO_SHOW' ? 'text-bg-danger' :
                                                    'text-bg-primary'
                                        }`}>
                                            {appointment.status}
                                        </span>
                                    </td>
                                    <td className="text-end">
                                        <div className="d-flex gap-2 justify-content-end">
                                            {appointment.status === 'SCHEDULED' && (
                                                <button
                                                    type="button"
                                                    className="btn btn-outline-warning btn-sm"
                                                    onClick={() => handleMarkAsNoShow(appointment.id)}
                                                >
                                                    No-Show
                                                </button>
                                            )}
                                            <button
                                                type="button"
                                                className="btn btn-outline-danger btn-sm"
                                                onClick={() => handleAppointmentCancel(appointment.id)}
                                                disabled={appointment.status === 'COMPLETED'}
                                            >
                                                Cancel
                                            </button>
                                        </div>
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
