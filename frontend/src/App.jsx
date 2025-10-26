import { Navigate, Route, Routes } from 'react-router-dom';
import DashboardLayout from './components/DashboardLayout';
import ProtectedRoute from './components/ProtectedRoute';
import DoctorDashboard from './pages/DoctorDashboard';
import LoginPage from './pages/LoginPage';
import PatientDashboard from './pages/PatientDashboard';
import ReceptionistDashboard from './pages/ReceptionistDashboard';

const roleBasedRoutes = [
  { path: '/patient', element: <PatientDashboard />, roles: ['PATIENT'] },
  { path: '/doctor', element: <DoctorDashboard />, roles: ['DOCTOR'] },
  { path: '/receptionist', element: <ReceptionistDashboard />, roles: ['RECEPTIONIST'] },
];

const App = () => (
  <Routes>
    <Route path="/login" element={<LoginPage />} />
    {roleBasedRoutes.map(({ path, element, roles }) => (
      <Route key={path} element={<ProtectedRoute allowedRoles={roles} />}>
        <Route element={<DashboardLayout />}>
          <Route path={path} element={element} />
        </Route>
      </Route>
    ))}
    <Route path="/" element={<Navigate to="/login" replace />} />
    <Route path="*" element={<Navigate to="/login" replace />} />
  </Routes>
);

export default App;
