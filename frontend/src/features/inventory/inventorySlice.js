import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import axiosClient from '../../api/axiosClient';

export const fetchProducts = createAsyncThunk('inventory/fetchAll', async () => {
  const response = await axiosClient.get('/inventory');
  return response.data;
});

export const createProduct = createAsyncThunk('inventory/create', async (product) => {
  const response = await axiosClient.post('/inventory', product);
  return response.data;
});

const inventorySlice = createSlice({
  name: 'inventory',
  initialState: { products: [], status: 'idle', error: null },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchProducts.fulfilled, (state, action) => {
        state.products = action.payload;
        state.status = 'succeeded';
      })
      .addCase(createProduct.fulfilled, (state, action) => {
        state.products.push(action.payload);
      });
  },
});

export default inventorySlice.reducer;
