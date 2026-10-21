import { createContext, useContext, useEffect, useState } from 'react';
import { api, setAuthToken } from './api.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('fp-auth'));
    } catch {
      return null;
    }
  });

  useEffect(() => {
    setAuthToken(user?.token || null);
  }, [user]);

  const save = (u) => {
    setUser(u);
    if (u) localStorage.setItem('fp-auth', JSON.stringify(u));
    else localStorage.removeItem('fp-auth');
  };

  const login = async (email, password) => {
    const { data } = await api.post('/auth/login', { email, password });
    save(data);
    return data;
  };

  const register = async (path, body) => {
    const { data } = await api.post(path, body);
    save(data);
    return data;
  };

  const logout = () => save(null);

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);

export const homeFor = (role) =>
  ({ STUDENT: '/student', COMPANY: '/company', COLLEGE: '/college', ADMIN: '/admin' })[role] || '/login';

export const roleLabel = (role) =>
  ({ STUDENT: 'Student', COMPANY: 'Company', COLLEGE: 'College (TPO)', ADMIN: 'Portal Admin' })[role] || role;
