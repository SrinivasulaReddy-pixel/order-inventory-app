import React, { useState } from 'react';
import { useDispatch } from 'react-redux';
import { useNavigate, Link } from 'react-router-dom';
import { register } from '../features/auth/authSlice';

export default function RegisterPage() {
  const [form, setForm] = useState({ username: '', password: '', email: '', role: 'CUSTOMER' });
  const [message, setMessage] = useState('');
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    const result = await dispatch(register(form));
    if (!result.error) {
      setMessage('Registered! Redirecting to login...');
      setTimeout(() => navigate('/login'), 1000);
    } else {
      setMessage('Registration failed: ' + result.error.message);
    }
  };

  return (
    <div style={{ maxWidth: 360, margin: '60px auto' }}>
      <h2>Register</h2>
      <form onSubmit={handleSubmit}>
        <input name="username" placeholder="Username" value={form.username} onChange={handleChange} />
        <br />
        <input name="email" placeholder="Email" value={form.email} onChange={handleChange} />
        <br />
        <input name="password" type="password" placeholder="Password" value={form.password} onChange={handleChange} />
        <br />
        <select name="role" value={form.role} onChange={handleChange}>
          <option value="CUSTOMER">Customer</option>
          <option value="ADMIN">Admin</option>
        </select>
        <br />
        <button type="submit">Register</button>
      </form>
      {message && <p>{message}</p>}
      <p>Already have an account? <Link to="/login">Login</Link></p>
    </div>
  );
}
