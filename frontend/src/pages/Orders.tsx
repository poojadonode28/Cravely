
import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../api/axios";

interface OrderItem {
    menuItemId: number;
    name: string;
    price: number;
    quantity: number;
}

interface Order {
    id: number;
    userId: number;
    restaurantId: number;
    deliveryPartnerId: number | null;
    items: OrderItem[];
    totalAmount: number;
    status: string;
    createdAt: string;
    version: number;
}

export default function Orders() {
    const navigate = useNavigate();

    const [orders, setOrders] = useState<Order[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        if (!localStorage.getItem("token")) {
            navigate("/login", {
                state: { from: "/orders" },
                replace: true,
            });
            return;
        }

        const fetchOrders = async () => {
            try {
                setLoading(true);
                setError("");

                const response = await api.get<Order[]>(
                    "/orders/my-orders"
                );

                setOrders(response.data);
            } catch (err: any) {
                console.error("Failed to fetch orders:", err);

                if (err.response?.status === 401) {
                    navigate("/login", {
                        state: { from: "/orders" },
                        replace: true,
                    });
                } else if (err.response?.status === 403) {
                    setError(
                        "Please log in with a customer account to view your orders."
                    );
                } else {
                    setError(
                        "Unable to load your orders. Please try again."
                    );
                }
            } finally {
                setLoading(false);
            }
        };

        void fetchOrders();
    }, [navigate]);

    const formatDate = (date: string) => {
        return new Date(date).toLocaleString("en-IN", {
            dateStyle: "medium",
            timeStyle: "short",
        });
    };

    const formatPrice = (amount: number) => {
        return `₹${Number(amount).toFixed(2)}`;
    };

    if (loading) {
        return (
            <main className="container orders-page">
                <h1>My Orders</h1>
                <p>Loading your orders...</p>
            </main>
        );
    }

    if (error) {
        return (
            <main className="container orders-page">
                <h1>My Orders</h1>
                <p className="error-message">{error}</p>
                <button
                    type="button"
                    onClick={() => window.location.reload()}
                >
                    Try Again
                </button>
            </main>
        );
    }

    return (
        <main className="container orders-page">
            <div className="orders-heading">
                <div>
                    <h1>My Orders</h1>
                    <p>View your order history and current orders.</p>
                </div>
                <span>{orders.length} orders</span>
            </div>

            {orders.length === 0 ? (
                <div className="orders-empty">
                    <h2>No orders yet</h2>
                    <p>
                        Your orders will appear here after checkout.
                    </p>
                    <Link to="/">Explore Restaurants</Link>
                </div>
            ) : (
                <div className="orders-list">
                    {orders.map((order) => (
                        <article className="order-card" key={order.id}>
                            <div className="order-card-header">
                                <div>
                                    <h2>Order #{order.id}</h2>
                                    <p>{formatDate(order.createdAt)}</p>
                                    <p>Restaurant #{order.restaurantId}</p>
                                </div>

                                <span className="order-status">
                  {order.status.replaceAll("_", " ")}
                </span>
                            </div>

                            <div className="order-items">
                                {order.items.map((item) => (
                                    <div
                                        className="order-item-row"
                                        key={item.menuItemId}
                                    >
                    <span>
                      {item.name} × {item.quantity}
                    </span>
                                        <strong>
                                            {formatPrice(
                                                Number(item.price) * item.quantity
                                            )}
                                        </strong>
                                    </div>
                                ))}
                            </div>

                            <div className="order-card-footer">
                                <strong>
                                    Total: {formatPrice(order.totalAmount)}
                                </strong>
                                <span>
                  {order.deliveryPartnerId
                      ? `Delivery Partner #${order.deliveryPartnerId}`
                      : "Delivery partner not assigned"}
                </span>
                            </div>
                        </article>
                    ))}
                </div>
            )}
        </main>
    );
}
