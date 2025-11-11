import {useCallback, useEffect, useState} from 'react';
import ErrorAlert from '../components/ErrorAlert';
import LoadingSpinner from '../components/LoadingSpinner';
import {bookAppointment, cancelAppointment, getAvailableAppointments, getPatientAppointments} from '../services/api';
import {useAuth} from '../context/AuthContext';

const PatientDashboard = () => {
    const {userId} = useAuth();
    const [availableSlots, setAvailableSlots] = useState([]);
    const [myAppointments, setMyAppointments] = useState([]);
    const [selectedDate, setSelectedDate] = useState('');
    const [loading, setLoading] = useState(false);
    const [loadingAppointments, setLoadingAppointments] = useState(true);
    const [error, setError] = useState('');
    const [successMessage, setSuccessMessage] = useState('');

    const loadMyAppointments = useCallback(async () => {
        if (!userId) return;
        setLoadingAppointments(true);
        try {
            const data = await getPatientAppointments(userId);
            setMyAppointments(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error('Failed to load appointments:', err);
        } finally {
            setLoadingAppointments(false);
        }
    }, [userId]);

    const loadAvailableSlots = useCallback(async () => {
        setLoading(true);
        setError('');
        try {
            const data = await getAvailableAppointments(selectedDate);
            setAvailableSlots(Array.isArray(data) ? data : []);
        } catch (err) {
            setError(err.message || 'Failed to load available slots.');
        } finally {
            setLoading(false);
        }
    }, [selectedDate]);

    // Load patient's appointments on mount
    useEffect(() => {
        loadMyAppointments();
    }, [userId, loadMyAppointments]);

    // Set default date to today
    useEffect(() => {
        const today = new Date().toISOString().split('T')[0];
        setSelectedDate(today);
    }, []);

    // Load available slots when date changes
    useEffect(() => {
        if (selectedDate) {
            loadAvailableSlots();
        }
    }, [selectedDate, loadAvailableSlots]);

    const handleBook = async (time) => {
        setError('');
        setSuccessMessage('');
        try {
            await bookAppointment({
                patientId: userId,
                date: selectedDate,
                time: time,
            });
            setSuccessMessage(`Appointment booked successfully for ${selectedDate} at ${time}.`);
            await loadAvailableSlots();
            await loadMyAppointments();
        } catch (err) {
            setError(err.message || 'Unable to book appointment.');
        }
    };

    const handleCancel = async (appointmentId) => {
        if (!window.confirm('Are you sure you want to cancel this appointment?')) {
            return;
        }
        setError('');
        setSuccessMessage('');
        try {
            await cancelAppointment(appointmentId);
            setSuccessMessage('Appointment cancelled successfully.');
            await loadMyAppointments();
        } catch (err) {
            setError(err.message || 'Unable to cancel appointment.');
        }
    };

    return (
        <div className="d-grid gap-4">
            {/* My Appointments Section */}
            <div className="glassy-card shadow-sm p-4">
                <div className="mb-4">
                    <h2 className="h4 fw-bold text-primary mb-1">My Appointments</h2>
                    <p className="text-muted mb-0">View and manage your scheduled appointments.</p>
                </div>

                {error && <ErrorAlert message={error} onClose={() => setError('')}/>}
                {successMessage && (
                    <div className="alert alert-success alert-dismissible fade show" role="alert">
                        {successMessage}
                        <button type="button" className="btn-close" onClick={() => setSuccessMessage('')}
                                aria-label="Close"/>
                    </div>
                )}

                {loadingAppointments ? (
                    <LoadingSpinner message="Loading your appointments"/>
                ) : myAppointments.length === 0 ? (
                    <p className="text-center text-muted py-4">You have no scheduled appointments.</p>
                ) : (
                    <div className="table-responsive rounded-4 overflow-hidden shadow-sm">
                        <table className="table align-middle mb-0">
                            <thead className="table-light">
                            <tr>
                                <th>Date</th>
                                <th>Time</th>
                                <th>Status</th>
                                <th className="text-end">Actions</th>
                            </tr>
                            </thead>
                            <tbody>
                            {myAppointments.map((appointment) => (
                                <tr key={appointment.id}>
                                    <td>{appointment.date}</td>
                                    <td className="fw-semibold">{appointment.time}</td>
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
                                        <button
                                            type="button"
                                            className="btn btn-outline-danger btn-sm"
                                            onClick={() => handleCancel(appointment.id)}
                                            disabled={appointment.status !== 'SCHEDULED'}
                                        >
                                            Cancel
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            {/* Book Appointment Section */}
            <div className="glassy-card shadow-sm p-4">
                <div className="mb-4">
                    <h2 className="h4 fw-bold text-primary mb-1">Book an Appointment</h2>
                    <p className="text-muted mb-0">Select a date to view available time slots.</p>
                </div>

                <div className="mb-4">
                    <label htmlFor="dateSelect" className="form-label fw-semibold">Select Date</label>
                    <input
                        type="date"
                        id="dateSelect"
                        className="form-control"
                        value={selectedDate}
                        onChange={(e) => setSelectedDate(e.target.value)}
                        min={new Date().toISOString().split('T')[0]}
                    />
                </div>

                {loading ? (
                    <LoadingSpinner message="Loading available slots"/>
                ) : availableSlots.length === 0 ? (
                    <p className="text-center text-muted py-4">
                        No time slots available for {selectedDate}. Please select another date.
                    </p>
                ) : (
                    <div>
                        <h5 className="mb-3">Available Time Slots</h5>
                        <div className="row g-2">
                            {availableSlots.map((time) => (
                                <div className="col-6 col-md-4 col-lg-3" key={time}>
                                    <button
                                        type="button"
                                        className="btn btn-outline-primary w-100"
                                        onClick={() => handleBook(time)}
                                    >
                                        {time}
                                    </button>
                                </div>
                            ))}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default PatientDashboard;
