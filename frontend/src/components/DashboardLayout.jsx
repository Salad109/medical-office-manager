import { Outlet } from 'react-router-dom';
import TopNav from './TopNav';

const DashboardLayout = () => (
  <div className="min-vh-100 py-4">
    <div className="container-lg">
      <TopNav />
      <Outlet />
    </div>
  </div>
);

export default DashboardLayout;
