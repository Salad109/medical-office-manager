import {createContext, useContext, useMemo, useState} from 'react';
import {login as loginRequest} from '../services/api';

const AuthContext = createContext(undefined);

// Helper function to decode JWT and extract userId
const getUserIdFromToken = (token) => {
    if (!token) return null;
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.userId || null;
    } catch (error) {
        console.error('Failed to decode token:', error);
        return null;
    }
};

export const AuthProvider = ({children}) => {
    const [authState, setAuthState] = useState(() => {
        const token = localStorage.getItem('token');
        const role = localStorage.getItem('role');
        if (token && role) {
            const userId = getUserIdFromToken(token);
            return {token, role, userId};
        }
        return null;
    });
    const [authLoading, setAuthLoading] = useState(false);

    const login = async (credentials) => {
        setAuthLoading(true);
        try {
            const data = await loginRequest(credentials);
            if (!data?.token || !data?.role) {
                throw new Error('Invalid login response. Token or role missing.');
            }
            const userId = getUserIdFromToken(data.token);
            localStorage.setItem('token', data.token);
            localStorage.setItem('role', data.role);
            setAuthState({token: data.token, role: data.role, userId});
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
            userId: authState?.userId ?? null,
            isAuthenticated: Boolean(authState?.token),
            login,
            logout,
            authLoading,
        }),
        [authState, authLoading],
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};
