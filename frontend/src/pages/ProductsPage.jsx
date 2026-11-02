import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchProducts, createProduct, updateProduct, deleteProduct } from '../features/inventory/inventorySlice';

const emptyForm = { name: '', description: '', price: '', stockQuantity: '' };

export default function ProductsPage() {
  const dispatch = useDispatch();
  const { products, status } = useSelector((state) => state.inventory);
  const roles = useSelector((state) => state.auth.roles) || [];
  const isAdmin = roles.includes('ROLE_ADMIN');

  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null); // null = creating, otherwise editing this product id

  useEffect(() => {
    dispatch(fetchProducts());
  }, [dispatch]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const payload = {
      ...form,
      price: Number(form.price),
      stockQuantity: Number(form.stockQuantity),
    };
    if (editingId) {
      await dispatch(updateProduct({ id: editingId, product: payload }));
    } else {
      await dispatch(createProduct(payload));
    }
    setForm(emptyForm);
    setEditingId(null);
  };

  const startEdit = (p) => {
    setEditingId(p.id);
    setForm({ name: p.name, description: p.description || '', price: p.price, stockQuantity: p.stockQuantity });
  };

  const cancelEdit = () => {
    setEditingId(null);
    setForm(emptyForm);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Delete this product?')) {
      await dispatch(deleteProduct(id));
    }
  };

  return (
    <div style={{ maxWidth: 700, margin: '20px auto' }}>
      <h2>Inventory</h2>
      {status === 'loading' && <p>Loading...</p>}
      <table border="1" cellPadding="6" style={{ width: '100%' }}>
        <thead>
          <tr>
            <th>ID</th><th>Name</th><th>Price</th><th>Stock</th>
            {isAdmin && <th>Actions</th>}
          </tr>
        </thead>
        <tbody>
          {products.map((p) => (
            <tr key={p.id}>
              <td>{p.id}</td><td>{p.name}</td><td>{p.price}</td><td>{p.stockQuantity}</td>
              {isAdmin && (
                <td>
                  <button onClick={() => startEdit(p)}>Edit</button>{' '}
                  <button onClick={() => handleDelete(p.id)}>Delete</button>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>

      {isAdmin && (
        <>
          <h3>{editingId ? `Edit Product #${editingId}` : 'Add Product'} (Admin only)</h3>
          <form onSubmit={handleSubmit}>
            <input placeholder="Name" value={form.name}
                   onChange={(e) => setForm({ ...form, name: e.target.value })} />
            <input placeholder="Description" value={form.description}
                   onChange={(e) => setForm({ ...form, description: e.target.value })} />
            <input placeholder="Price" value={form.price}
                   onChange={(e) => setForm({ ...form, price: e.target.value })} />
            <input placeholder="Stock Qty" value={form.stockQuantity}
                   onChange={(e) => setForm({ ...form, stockQuantity: e.target.value })} />
            <button type="submit">{editingId ? 'Update' : 'Add'}</button>
            {editingId && <button type="button" onClick={cancelEdit}>Cancel</button>}
          </form>
        </>
      )}
    </div>
  );
}
