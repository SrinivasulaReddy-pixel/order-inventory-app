import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, Link } from 'react-router-dom';
import { login } from '../features/auth/authSlice';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { status, error } = useSelector((state) => state.auth);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const result = await dispatch(login({ username, password }));
    if (!result.error) {
      navigate('/products');
    }
  };

  return (
    <div style={{ maxWidth: 360, margin: '60px auto' }}>
      <h2>Login</h2>
      <form onSubmit={handleSubmit}>
        <input placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} />
        <br />
        <input placeholder="Password" type="password" value={password}
               onChange={(e) => setPassword(e.target.value)} />
        <br />
        <button type="submit" disabled={status === 'loading'}>Login</button>
      </form>
      {error && <p style={{ color: 'red' }}>Login failed: {error}</p>}
      <p>No account? <Link to="/register">Register</Link></p>
    </div>
  );
}
