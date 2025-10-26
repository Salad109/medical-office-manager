const LoadingSpinner = ({ message = 'Loading...' }) => (
  <div className="text-center py-5">
    <div className="spinner-border text-primary" role="status" aria-hidden="true" />
    <p className="mt-3 mb-0 text-muted">{message}</p>
  </div>
);

export default LoadingSpinner;
