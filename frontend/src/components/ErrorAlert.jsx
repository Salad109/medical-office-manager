const ErrorAlert = ({message, onClose}) => {
    if (!message) {
        return null;
    }

    return (
        <div className="alert alert-danger alert-dismissible fade show" role="alert">
            {message}
            {onClose && (
                <button type="button" className="btn-close" onClick={onClose} aria-label="Close"/>
            )}
        </div>
    );
};

export default ErrorAlert;
