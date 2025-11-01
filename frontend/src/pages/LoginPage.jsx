import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import ErrorAlert from '../components/ErrorAlert';
import LoadingSpinner from '../components/LoadingSpinner';
import {useAuth} from '../context/AuthContext';

const LoginPage = () => {
    const navigate = useNavigate();
    const {login, authLoading, isAuthenticated, role} = useAuth();
    const [formData, setFormData] = useState({username: '', password: ''});
    const [error, setError] = useState('');

    useEffect(() => {
        if (isAuthenticated && role) {
            navigate(`/${role.toLowerCase()}`);
        }
    }, [isAuthenticated, role, navigate]);

    const handleChange = (event) => {
        const {name, value} = event.target;
        setFormData((prev) => ({...prev, [name]: value}));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError('');
        try {
            const data = await login(formData);
            navigate(`/${data.role.toLowerCase()}`);
        } catch (err) {
            setError(err.message || 'Unable to sign in.');
        }
    };

    return (
        <div className="min-vh-100 d-flex align-items-center justify-content-center py-5">
            <div className="auth-card glassy-card shadow-lg p-4 p-md-5 w-100" style={{maxWidth: '420px'}}>
                <div className="text-center mb-4">
                    <h1 className="h3 fw-bold text-primary">Welcome Back</h1>
                    <p className="text-muted mb-0">Sign in to manage your medical office tasks</p>
                </div>
                {error && <ErrorAlert message={error} onClose={() => setError('')}/>}
                <form onSubmit={handleSubmit} className="d-grid gap-3">
                    <div>
                        <label htmlFor="username" className="form-label text-muted text-uppercase small fw-semibold">
                            Username
                        </label>
                        <input
                            type="text"
                            className="form-control form-control-lg"
                            id="username"
                            name="username"
                            autoComplete="username"
                            value={formData.username}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div>
                        <label htmlFor="password" className="form-label text-muted text-uppercase small fw-semibold">
                            Password
                        </label>
                        <input
                            type="password"
                            className="form-control form-control-lg"
                            id="password"
                            name="password"
                            autoComplete="current-password"
                            value={formData.password}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <button type="submit" className="btn btn-primary btn-lg" disabled={authLoading}>
                        {authLoading ? 'Signing in…' : 'Login'}
                    </button>
                </form>
                {authLoading && <LoadingSpinner message="Authenticating"/>}
            </div>
        </div>
    );
};

export default LoginPage;
