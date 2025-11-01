import {useNavigate} from 'react-router-dom';
import {useAuth} from '../context/AuthContext';

const roleLabels = {
    PATIENT: 'Patient',
    DOCTOR: 'Doctor',
    RECEPTIONIST: 'Receptionist',
};

const TopNav = () => {
    const navigate = useNavigate();
    const {role, logout} = useAuth();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <nav className="navbar navbar-expand-lg glassy-nav shadow-sm mb-4">
            <div className="container-fluid">
                <span className="navbar-brand fw-semibold text-primary">Medical Office Manager</span>
                {role && (
                    <div className="d-flex align-items-center gap-3">
            <span className="badge text-bg-light text-primary-emphasis">
              {roleLabels[role] || role}
            </span>
                        <button type="button" className="btn btn-outline-primary btn-sm" onClick={handleLogout}>
                            Logout
                        </button>
                    </div>
                )}
            </div>
        </nav>
    );
};

export default TopNav;
