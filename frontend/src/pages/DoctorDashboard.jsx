import {useCallback, useEffect, useState} from 'react';
import ErrorAlert from '../components/ErrorAlert';
import LoadingSpinner from '../components/LoadingSpinner';
import {createVisit, getAppointments, getPatientWithVisits, updateVisitNotes} from '../services/api';

const DoctorDashboard = () => {
    const [view, setView] = useState('appointments'); // 'appointments' or 'patient'
    const [appointments, setAppointments] = useState([]);
    const [selectedAppointment, setSelectedAppointment] = useState(null);
    const [patientData, setPatientData] = useState(null);
    const [selectedDate, setSelectedDate] = useState('');
    const [loading, setLoading] = useState(true);
    const [loadingPatient, setLoadingPatient] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [visitNotes, setVisitNotes] = useState('');
    const [editingVisit, setEditingVisit] = useState(null);

    useEffect(() => {
        const today = new Date().toISOString().split('T')[0];
        setSelectedDate(today);
    }, []);

    const loadAppointments = useCallback(async () => {
        setLoading(true);
        setError('');
        try {
            const data = await getAppointments(selectedDate);
            setAppointments(Array.isArray(data) ? data : []);
        } catch (err) {
            setError(err.message || 'Failed to load appointments.');
        } finally {
            setLoading(false);
        }
    }, [selectedDate]);

    useEffect(() => {
        if (selectedDate) {
            loadAppointments();
        }
    }, [selectedDate, loadAppointments]);

    const handleAppointmentClick = async (appointment) => {
        setSelectedAppointment(appointment);
        setLoadingPatient(true);
        setError('');
        try {
            const data = await getPatientWithVisits(appointment.patientId);
            setPatientData(data);
            setVisitNotes('');
            setView('patient');
        } catch (err) {
            setError(err.message || 'Failed to load patient data.');
        } finally {
            setLoadingPatient(false);
        }
    };

    const handleBack = () => {
        setView('appointments');
        setSelectedAppointment(null);
        setPatientData(null);
        setVisitNotes('');
        setEditingVisit(null);
        setSuccess('');
        setError('');
    };

    const handleCompleteVisit = async () => {
        setError('');
        setSuccess('');
        try {
            await createVisit({
                appointmentId: selectedAppointment.id,
                notes: visitNotes
            });
            setSuccess('Visit completed successfully!');
            setVisitNotes('');
            // Reload patient data to show the new visit
            const data = await getPatientWithVisits(selectedAppointment.patientId);
            setPatientData(data);
            // Update appointment status locally
            setSelectedAppointment({...selectedAppointment, status: 'COMPLETED'});
        } catch (err) {
            setError(err.message || 'Failed to complete visit.');
        }
    };

    const handleEditVisit = (visit) => {
        setEditingVisit(visit);
        setVisitNotes(visit.notes || '');
    };

    const handleSaveVisitNotes = async () => {
        if (!editingVisit) return;
        setError('');
        setSuccess('');
        try {
            await updateVisitNotes(editingVisit.id, visitNotes);
            setSuccess('Visit notes updated successfully!');
            setEditingVisit(null);
            setVisitNotes('');
            // Reload patient data
            const data = await getPatientWithVisits(selectedAppointment.patientId);
            setPatientData(data);
        } catch (err) {
            setError(err.message || 'Failed to update visit notes.');
        }
    };

    const handleCancelEdit = () => {
        setEditingVisit(null);
        setVisitNotes('');
    };

    return (
        <div>
            {view === 'appointments' ? (
                // Appointments List View
                <div className="glassy-card shadow-sm p-4">
                    <div className="mb-4">
                        <h2 className="h4 fw-bold text-primary mb-1">Today's Appointments</h2>
                        <p className="text-muted mb-0">Select an appointment to view patient details and complete
                            visits.</p>
                    </div>

                    <div className="mb-4">
                        <label htmlFor="appointmentDate" className="form-label fw-semibold">Select Date</label>
                        <input
                            type="date"
                            id="appointmentDate"
                            className="form-control"
                            value={selectedDate}
                            onChange={(e) => setSelectedDate(e.target.value)}
                        />
                    </div>

                    {error && <ErrorAlert message={error} onClose={() => setError('')}/>}

                    {loading ? (
                        <LoadingSpinner message="Loading appointments"/>
                    ) : appointments.length === 0 ? (
                        <p className="text-center text-muted py-4">No appointments for {selectedDate}.</p>
                    ) : (
                        <div className="table-responsive rounded-4 overflow-hidden shadow-sm">
                            <table className="table align-middle mb-0">
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
                                    <tr key={appointment.id} style={{cursor: 'pointer'}}
                                        onClick={() => handleAppointmentClick(appointment)}>
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
                                            <button
                                                type="button"
                                                className="btn btn-primary btn-sm"
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    handleAppointmentClick(appointment);
                                                }}
                                            >
                                                View Details
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            ) : (
                // Patient Detail View
                <div className="d-grid gap-4">
                    <div className="glassy-card shadow-sm p-4">
                        <button type="button" className="btn btn-outline-primary mb-3" onClick={handleBack}>
                            ← Back to Appointments
                        </button>

                        {loadingPatient ? (
                            <LoadingSpinner message="Loading patient data"/>
                        ) : patientData ? (
                            <>
                                <div className="mb-4">
                                    <h2 className="h4 fw-bold text-primary mb-3">Patient Information</h2>
                                    <div className="row g-3">
                                        <div className="col-md-6">
                                            <strong>Name:</strong> {patientData.patient.firstName} {patientData.patient.lastName}
                                        </div>
                                        <div className="col-md-6">
                                            <strong>ID:</strong> {patientData.patient.id}
                                        </div>
                                        <div className="col-md-6">
                                            <strong>Phone:</strong> {patientData.patient.phoneNumber}
                                        </div>
                                        <div className="col-md-6">
                                            <strong>PESEL:</strong> {patientData.patient.pesel}
                                        </div>
                                    </div>
                                </div>

                                {/* Complete Visit Form - Only show if appointment is SCHEDULED */}
                                {selectedAppointment && selectedAppointment.status === 'SCHEDULED' && (
                                    <div className="card border-success mb-4">
                                        <div className="card-header bg-success text-white">
                                            <h5 className="mb-0">Complete Current Visit</h5>
                                        </div>
                                        <div className="card-body">
                                            <div className="mb-3">
                                                <strong>Appointment:</strong> {selectedAppointment.date} at {selectedAppointment.time}
                                            </div>
                                            {error && <ErrorAlert message={error} onClose={() => setError('')}/>}
                                            {success && (
                                                <div className="alert alert-success alert-dismissible fade show"
                                                     role="alert">
                                                    {success}
                                                    <button type="button" className="btn-close"
                                                            onClick={() => setSuccess('')} aria-label="Close"/>
                                                </div>
                                            )}
                                            <div className="mb-3">
                                                <label htmlFor="visitNotes" className="form-label fw-semibold">Visit
                                                    Notes</label>
                                                <textarea
                                                    id="visitNotes"
                                                    className="form-control"
                                                    rows={5}
                                                    value={visitNotes}
                                                    onChange={(e) => setVisitNotes(e.target.value)}
                                                    placeholder="Enter diagnosis, treatment plan, medications, etc."
                                                />
                                            </div>
                                            <button type="button" className="btn btn-success"
                                                    onClick={handleCompleteVisit}>
                                                Complete Visit
                                            </button>
                                        </div>
                                    </div>
                                )}

                                {/* Visit History */}
                                <div>
                                    <h5 className="mb-3">Visit History</h5>
                                    {error && !selectedAppointment?.status && (
                                        <ErrorAlert message={error} onClose={() => setError('')}/>
                                    )}
                                    {success && !selectedAppointment?.status && (
                                        <div className="alert alert-success alert-dismissible fade show" role="alert">
                                            {success}
                                            <button type="button" className="btn-close"
                                                    onClick={() => setSuccess('')} aria-label="Close"/>
                                        </div>
                                    )}
                                    {patientData.visits.length === 0 ? (
                                        <p className="text-muted">No previous visits recorded.</p>
                                    ) : (
                                        <div className="table-responsive rounded-4 overflow-hidden shadow-sm">
                                            <table className="table align-middle mb-0">
                                                <thead className="table-light">
                                                <tr>
                                                    <th>Date</th>
                                                    <th>Time</th>
                                                    <th>Notes</th>
                                                    <th className="text-end">Actions</th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                {patientData.visits.map((visit) => (
                                                    <tr key={visit.id}>
                                                        <td>{visit.appointmentDate}</td>
                                                        <td>{visit.appointmentTime}</td>
                                                        <td>
                                                            {editingVisit && editingVisit.id === visit.id ? (
                                                                <textarea
                                                                    className="form-control form-control-sm"
                                                                    rows={3}
                                                                    value={visitNotes}
                                                                    onChange={(e) => setVisitNotes(e.target.value)}
                                                                />
                                                            ) : (
                                                                <div
                                                                    style={{whiteSpace: 'pre-wrap'}}>{visit.notes || 'No notes'}</div>
                                                            )}
                                                        </td>
                                                        <td className="text-end">
                                                            {editingVisit && editingVisit.id === visit.id ? (
                                                                <div className="d-flex gap-2 justify-content-end">
                                                                    <button type="button"
                                                                            className="btn btn-success btn-sm"
                                                                            onClick={handleSaveVisitNotes}>
                                                                        Save
                                                                    </button>
                                                                    <button type="button"
                                                                            className="btn btn-secondary btn-sm"
                                                                            onClick={handleCancelEdit}>
                                                                        Cancel
                                                                    </button>
                                                                </div>
                                                            ) : (
                                                                <button type="button"
                                                                        className="btn btn-outline-primary btn-sm"
                                                                        onClick={() => handleEditVisit(visit)}>
                                                                    Edit Notes
                                                                </button>
                                                            )}
                                                        </td>
                                                    </tr>
                                                ))}
                                                </tbody>
                                            </table>
                                        </div>
                                    )}
                                </div>
                            </>
                        ) : null}
                    </div>
                </div>
            )}
        </div>
    );
};

export default DoctorDashboard;
