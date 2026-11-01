import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchProducts, createProduct } from '../features/inventory/inventorySlice';

export default function ProductsPage() {
  const dispatch = useDispatch();
  const { products, status } = useSelector((state) => state.inventory);
  const roles = useSelector((state) => state.auth.roles) || [];
  const isAdmin = roles.includes('ROLE_ADMIN');

  const [form, setForm] = useState({ name: '', description: '', price: '', stockQuantity: '' });

  useEffect(() => {
    dispatch(fetchProducts());
  }, [dispatch]);

  const handleCreate = async (e) => {
    e.preventDefault();
    await dispatch(createProduct({
      ...form,
      price: Number(form.price),
      stockQuantity: Number(form.stockQuantity),
    }));
    setForm({ name: '', description: '', price: '', stockQuantity: '' });
  };

  return (
    <div style={{ maxWidth: 700, margin: '20px auto' }}>
      <h2>Inventory</h2>
      {status === 'loading' && <p>Loading...</p>}
      <table border="1" cellPadding="6" style={{ width: '100%' }}>
        <thead>
          <tr><th>ID</th><th>Name</th><th>Price</th><th>Stock</th></tr>
        </thead>
        <tbody>
          {products.map((p) => (
            <tr key={p.id}>
              <td>{p.id}</td><td>{p.name}</td><td>{p.price}</td><td>{p.stockQuantity}</td>
            </tr>
          ))}
        </tbody>
      </table>

      {isAdmin && (
        <>
          <h3>Add Product (Admin only)</h3>
          <form onSubmit={handleCreate}>
            <input placeholder="Name" value={form.name}
                   onChange={(e) => setForm({ ...form, name: e.target.value })} />
            <input placeholder="Description" value={form.description}
                   onChange={(e) => setForm({ ...form, description: e.target.value })} />
            <input placeholder="Price" value={form.price}
                   onChange={(e) => setForm({ ...form, price: e.target.value })} />
            <input placeholder="Stock Qty" value={form.stockQuantity}
                   onChange={(e) => setForm({ ...form, stockQuantity: e.target.value })} />
            <button type="submit">Add</button>
          </form>
        </>
      )}
    </div>
  );
}
