import React, { createContext, useState, useContext, useEffect } from 'react';
import { loginUser, registerUser } from '../api/authApi';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    const token = localStorage.getItem('token');
    if (storedUser && token) {
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  const login = async (credentials) => {
    const response = await loginUser(credentials);
    const { token, username, email, userId } = response.data;
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify({ username, email, userId }));
    setUser({ username, email, userId });
    return response.data;
  };

  const register = async (payload) => {
    const response = await registerUser(payload);
    const { token, username, email, userId } = response.data;
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify({ username, email, userId }));
    setUser({ username, email, userId });
    return response.data;
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
