import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import axiosClient from '../../api/axiosClient';

export const fetchMyOrders = createAsyncThunk('orders/fetchMine', async () => {
  const response = await axiosClient.get('/orders/mine');
  return response.data;
});

export const fetchAllOrders = createAsyncThunk('orders/fetchAll', async () => {
  const response = await axiosClient.get('/orders');
  return response.data;
});

export const placeOrder = createAsyncThunk('orders/place', async (orderRequest) => {
  const response = await axiosClient.post('/orders', orderRequest);
  return response.data;
});

export const cancelOrder = createAsyncThunk('orders/cancel', async (orderId) => {
  const response = await axiosClient.put(`/orders/${orderId}/cancel`);
  return response.data;
});

const ordersSlice = createSlice({
  name: 'orders',
  initialState: { orders: [], status: 'idle', error: null },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchMyOrders.fulfilled, (state, action) => {
        state.orders = action.payload;
        state.status = 'succeeded';
      })
      .addCase(fetchAllOrders.fulfilled, (state, action) => {
        state.orders = action.payload;
        state.status = 'succeeded';
      })
      .addCase(placeOrder.fulfilled, (state, action) => {
        state.orders.push(action.payload);
      })
      .addCase(placeOrder.rejected, (state, action) => {
        state.error = action.error.message;
      })
      .addCase(cancelOrder.fulfilled, (state, action) => {
        const idx = state.orders.findIndex((o) => o.id === action.payload.id);
        if (idx !== -1) state.orders[idx] = action.payload;
      });
  },
});

export default ordersSlice.reducer;
