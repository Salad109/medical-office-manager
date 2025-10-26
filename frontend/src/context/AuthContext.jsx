import { createContext, useContext, useMemo, useState } from 'react';
import { login as loginRequest } from '../services/api';

const AuthContext = createContext(undefined);

export const AuthProvider = ({ children }) => {
  const [authState, setAuthState] = useState(() => {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    return token && role ? { token, role } : null;
  });
  const [authLoading, setAuthLoading] = useState(false);

  const login = async (credentials) => {
    setAuthLoading(true);
    try {
      const data = await loginRequest(credentials);
      if (!data?.token || !data?.role) {
        throw new Error('Invalid login response. Token or role missing.');
      }
      localStorage.setItem('token', data.token);
      localStorage.setItem('role', data.role);
      setAuthState({ token: data.token, role: data.role });
      return data;
    } finally {
      setAuthLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    setAuthState(null);
  };

  const value = useMemo(
    () => ({
      token: authState?.token ?? null,
      role: authState?.role ?? null,
      isAuthenticated: Boolean(authState?.token),
      login,
      logout,
      authLoading,
    }),
    [authState, authLoading],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
