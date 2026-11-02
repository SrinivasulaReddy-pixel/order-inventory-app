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

export const updateProduct = createAsyncThunk('inventory/update', async ({ id, product }) => {
  const response = await axiosClient.put(`/inventory/${id}`, product);
  return response.data;
});

export const deleteProduct = createAsyncThunk('inventory/delete', async (id) => {
  await axiosClient.delete(`/inventory/${id}`);
  return id;
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
      })
      .addCase(updateProduct.fulfilled, (state, action) => {
        const idx = state.products.findIndex((p) => p.id === action.payload.id);
        if (idx !== -1) state.products[idx] = action.payload;
      })
      .addCase(deleteProduct.fulfilled, (state, action) => {
        state.products = state.products.filter((p) => p.id !== action.payload);
      });
  },
});

export default inventorySlice.reducer;
