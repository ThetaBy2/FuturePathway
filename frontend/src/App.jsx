import { Navigate, Route, Routes } from 'react-router-dom';
import { homeFor, useAuth } from './auth.jsx';
import { RequireAuth } from './components.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import Student from './pages/Student.jsx';
import Company from './pages/Company.jsx';
import College from './pages/College.jsx';
import Admin from './pages/Admin.jsx';

function HomeRedirect() {
  const { user } = useAuth();
  return <Navigate to={user ? homeFor(user.role) : '/login'} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route
        path="/student"
        element={
          <RequireAuth roles={['STUDENT']}>
            <Student />
          </RequireAuth>
        }
      />
      <Route
        path="/company"
        element={
          <RequireAuth roles={['COMPANY']}>
            <Company />
          </RequireAuth>
        }
      />
      <Route
        path="/college"
        element={
          <RequireAuth roles={['COLLEGE']}>
            <College />
          </RequireAuth>
        }
      />
      <Route
        path="/admin"
        element={
          <RequireAuth roles={['ADMIN']}>
            <Admin />
          </RequireAuth>
        }
      />
      <Route path="/" element={<HomeRedirect />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
