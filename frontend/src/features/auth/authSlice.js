import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import axios from 'axios';

const authBase = axios.create({ baseURL: 'http://localhost:8080/api' });

const persisted = JSON.parse(localStorage.getItem('auth') || 'null');

const initialState = persisted || {
  token: null,
  username: null,
  roles: [],
  status: 'idle',
  error: null,
};

export const login = createAsyncThunk('auth/login', async ({ username, password }) => {
  const response = await authBase.post('/auth/login', { username, password });
  return response.data; // { token, username, roles }
});

export const register = createAsyncThunk(
  'auth/register',
  async ({ username, password, email, role }) => {
    await authBase.post('/auth/register', { username, password, email, role });
    return true;
  }
);

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    logout(state) {
      state.token = null;
      state.username = null;
      state.roles = [];
      localStorage.removeItem('auth');
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(login.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(login.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.token = action.payload.token;
        state.username = action.payload.username;
        state.roles = action.payload.roles;
        localStorage.setItem('auth', JSON.stringify(state));
      })
      .addCase(login.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.error.message;
      });
  },
});

export const { logout } = authSlice.actions;
export default authSlice.reducer;
