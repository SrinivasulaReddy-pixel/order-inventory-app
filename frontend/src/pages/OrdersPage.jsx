import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchMyOrders, fetchAllOrders, placeOrder, cancelOrder } from '../features/orders/ordersSlice';
import { fetchProducts } from '../features/inventory/inventorySlice';

export default function OrdersPage() {
  const dispatch = useDispatch();
  const { orders, error } = useSelector((state) => state.orders);
  const { products } = useSelector((state) => state.inventory);
  const roles = useSelector((state) => state.auth.roles) || [];
  const isAdmin = roles.includes('ROLE_ADMIN');

  const [productId, setProductId] = useState('');
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    dispatch(fetchProducts());
    if (isAdmin) {
      dispatch(fetchAllOrders());
    } else {
      dispatch(fetchMyOrders());
    }
  }, [dispatch, isAdmin]);

  const handlePlaceOrder = async (e) => {
    e.preventDefault();
    const product = products.find((p) => p.id === productId);
    if (!product) return;

    await dispatch(placeOrder({
      items: [{
        productId: product.id,
        productName: product.name,
        quantity: Number(quantity),
        price: product.price,
      }],
    }));
  };

  const username = useSelector((state) => state.auth.username);

  const handleCancel = async (orderId) => {
    if (window.confirm('Cancel this order?')) {
      await dispatch(cancelOrder(orderId));
    }
  };

  return (
    <div style={{ maxWidth: 700, margin: '20px auto' }}>
      <h2>{isAdmin ? 'All Orders' : 'My Orders'}</h2>

      {!isAdmin && (
        <form onSubmit={handlePlaceOrder} style={{ marginBottom: 20 }}>
          <select value={productId} onChange={(e) => setProductId(e.target.value)}>
            <option value="">Select product</option>
            {products.map((p) => (
              <option key={p.id} value={p.id}>{p.name} (stock: {p.stockQuantity})</option>
            ))}
          </select>
          <input type="number" min="1" value={quantity} onChange={(e) => setQuantity(e.target.value)} />
          <button type="submit" disabled={!productId}>Place Order</button>
        </form>
      )}

      {error && <p style={{ color: 'red' }}>{error}</p>}

      <table border="1" cellPadding="6" style={{ width: '100%' }}>
        <thead>
          <tr><th>Order ID</th><th>Customer</th><th>Status</th><th>Total</th><th>Items</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {orders.map((o) => {
            const canCancel = o.status === 'PLACED' && (isAdmin || o.customerUsername === username);
            return (
              <tr key={o.id}>
                <td>{o.id}</td>
                <td>{o.customerUsername}</td>
                <td>{o.status}</td>
                <td>{o.totalAmount}</td>
                <td>{o.items.map((i) => `${i.productName} x${i.quantity}`).join(', ')}</td>
                <td>{canCancel && <button onClick={() => handleCancel(o.id)}>Cancel</button>}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
