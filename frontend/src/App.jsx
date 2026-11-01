import React from 'react';
import { Routes, Route, Navigate, Link } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ProductsPage from './pages/ProductsPage';
import OrdersPage from './pages/OrdersPage';
import { logout } from './features/auth/authSlice';

function ProtectedRoute({ children }) {
  const token = useSelector((state) => state.auth.token);
  return token ? children : <Navigate to="/login" replace />;
}

export default function App() {
  const dispatch = useDispatch();
  const { token, username, roles } = useSelector((state) => state.auth);

  return (
    <div>
      <nav style={{ padding: 10, borderBottom: '1px solid #ccc', marginBottom: 10 }}>
        <Link to="/products" style={{ marginRight: 10 }}>Inventory</Link>
        <Link to="/orders" style={{ marginRight: 10 }}>Orders</Link>
        {token ? (
          <span>
            Logged in as {username} ({roles.join(', ')}) —{' '}
            <button onClick={() => dispatch(logout())}>Logout</button>
          </span>
        ) : (
          <Link to="/login">Login</Link>
        )}
      </nav>

      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/products" element={<ProtectedRoute><ProductsPage /></ProtectedRoute>} />
        <Route path="/orders" element={<ProtectedRoute><OrdersPage /></ProtectedRoute>} />
        <Route path="*" element={<Navigate to="/products" replace />} />
      </Routes>
    </div>
  );
}
