import { useEffect, useState } from 'react';
import ErrorAlert from '../components/ErrorAlert';
import LoadingSpinner from '../components/LoadingSpinner';
import { bookAppointment, getAvailableAppointments } from '../services/api';

const PatientDashboard = () => {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  useEffect(() => {
    loadAppointments();
  }, []);

  const loadAppointments = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getAvailableAppointments();
      setAppointments(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || 'Failed to load appointments.');
    } finally {
      setLoading(false);
    }
  };

  const handleBook = async (appointmentId) => {
    setError('');
    setSuccessMessage('');
    try {
      await bookAppointment(appointmentId);
      setSuccessMessage('Appointment booked successfully.');
      await loadAppointments();
    } catch (err) {
      setError(err.message || 'Unable to book appointment.');
    }
  };

  return (
    <div className="glassy-card shadow-sm p-4">
      <div className="d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3 mb-4">
        <div>
          <h2 className="h4 fw-bold text-primary mb-1">Available Appointments</h2>
          <p className="text-muted mb-0">Browse open slots and reserve your visit in one tap.</p>
        </div>
        <button type="button" className="btn btn-outline-primary" onClick={loadAppointments} disabled={loading}>
          Refresh
        </button>
      </div>
      {error && <ErrorAlert message={error} onClose={() => setError('')} />}
      {successMessage && (
        <div className="alert alert-success alert-dismissible fade show" role="alert">
          {successMessage}
          <button type="button" className="btn-close" onClick={() => setSuccessMessage('')} aria-label="Close" />
        </div>
      )}
      {loading ? (
        <LoadingSpinner message="Loading appointments" />
      ) : appointments.length === 0 ? (
        <p className="text-center text-muted py-4">No appointments available right now. Please check back soon.</p>
      ) : (
        <div className="row g-3">
          {appointments.map((appointment) => (
            <div className="col-md-6" key={appointment.id}>
              <div className="card border-0 shadow-sm h-100">
                <div className="card-body d-flex flex-column">
                  <div className="d-flex justify-content-between align-items-center mb-2">
                    <span className="badge text-bg-primary-subtle text-primary fw-semibold">
                      {appointment.specialty || 'General'}
                    </span>
                    <span className="text-muted small">{appointment.location || 'In person'}</span>
                  </div>
                  <h5 className="card-title mb-2">{appointment.doctorName || 'Doctor'}</h5>
                  <p className="card-text text-muted mb-3">
                    {new Date(appointment.dateTime || appointment.date || Date.now()).toLocaleString()}
                  </p>
                  <p className="text-muted small flex-grow-1">{appointment.notes || 'Routine visit'}</p>
                  <button type="button" className="btn btn-primary w-100 mt-3" onClick={() => handleBook(appointment.id)}>
                    Book visit
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default PatientDashboard;
