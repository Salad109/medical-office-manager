import { useEffect, useState } from 'react';
import ErrorAlert from '../components/ErrorAlert';
import LoadingSpinner from '../components/LoadingSpinner';
import { getDoctorVisits, updateVisitNotes } from '../services/api';

const DoctorDashboard = () => {
  const [visits, setVisits] = useState([]);
  const [notesDraft, setNotesDraft] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [savingId, setSavingId] = useState(null);

  useEffect(() => {
    loadVisits();
  }, []);

  const loadVisits = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getDoctorVisits();
      const visitList = Array.isArray(data) ? data : [];
      setVisits(visitList);
      const draftNotes = visitList.reduce((draft, visit) => {
        draft[visit.id] = visit.notes || '';
        return draft;
      }, {});
      setNotesDraft(draftNotes);
    } catch (err) {
      setError(err.message || 'Failed to load visits.');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (visitId, value) => {
    setNotesDraft((prev) => ({ ...prev, [visitId]: value }));
  };

  const handleSave = async (visitId) => {
    setSavingId(visitId);
    setSuccess('');
    setError('');
    try {
      await updateVisitNotes(visitId, notesDraft[visitId] || '');
      setSuccess('Notes updated successfully.');
      await loadVisits();
    } catch (err) {
      setError(err.message || 'Unable to update notes.');
    } finally {
      setSavingId(null);
    }
  };

  return (
    <div className="glassy-card shadow-sm p-4">
      <div className="d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3 mb-4">
        <div>
          <h2 className="h4 fw-bold text-primary mb-1">Today&apos;s Visits</h2>
          <p className="text-muted mb-0">Review patients scheduled for today and capture medical notes.</p>
        </div>
        <button type="button" className="btn btn-outline-primary" onClick={loadVisits} disabled={loading}>
          Refresh
        </button>
      </div>
      {error && <ErrorAlert message={error} onClose={() => setError('')} />}
      {success && (
        <div className="alert alert-success alert-dismissible fade show" role="alert">
          {success}
          <button type="button" className="btn-close" onClick={() => setSuccess('')} aria-label="Close" />
        </div>
      )}
      {loading ? (
        <LoadingSpinner message="Loading visits" />
      ) : visits.length === 0 ? (
        <p className="text-center text-muted py-4">You have no visits scheduled for today.</p>
      ) : (
        <div className="table-responsive rounded-4 overflow-hidden shadow-sm">
          <table className="table align-middle mb-0">
            <thead className="table-light">
              <tr>
                <th>Patient</th>
                <th>Time</th>
                <th>Reason</th>
                <th style={{ width: '30%' }}>Notes</th>
                <th className="text-end" aria-label="Actions" />
              </tr>
            </thead>
            <tbody>
              {visits.map((visit) => (
                <tr key={visit.id}>
                  <td>
                    <div className="fw-semibold">{visit.patientName}</div>
                    <div className="text-muted small">{visit.patientIdentifier}</div>
                  </td>
                  <td>{new Date(visit.dateTime || visit.date || Date.now()).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</td>
                  <td className="text-muted">{visit.reason || 'Routine check'}</td>
                  <td>
                    <textarea
                      className="form-control"
                      rows={2}
                      value={notesDraft[visit.id] ?? ''}
                      onChange={(event) => handleChange(visit.id, event.target.value)}
                    />
                  </td>
                  <td className="text-end">
                    <button type="button" className="btn btn-primary btn-sm" onClick={() => handleSave(visit.id)} disabled={savingId === visit.id}>
                      {savingId === visit.id ? 'Saving…' : 'Save notes'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default DoctorDashboard;
