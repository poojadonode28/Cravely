
import { Link, useNavigate } from "react-router-dom";
import { useCart } from "../context/CartContext";

export default function CartPage() {

    const navigate = useNavigate();
    const {
        cart,
        loading,
        itemCount,
        subtotal,
        updateQuantity,
        removeItem,
        clearCart,
    } = useCart();

    if (!localStorage.getItem("token")) {
        return (
            <main className="container">
                <h2>Please log in to view your cart</h2>
                <Link to="/login" className="primary-link">
                    Login
                </Link>
            </main>
        );
    }

    if (loading && !cart) {
        return <main className="container">Loading cart...</main>;
    }

    if (!cart || cart.items.length === 0) {
        return (
            <main className="container empty-cart">
                <div className="empty-cart-icon">🛒</div>
                <h2>Your cart is empty</h2>
                <p>Add some delicious food to get started.</p>
                <Link to="/" className="primary-link">
                    Browse restaurants
                </Link>
            </main>
        );
    }

    return (
        <main className="container cart-page">
            <div className="cart-heading">
                <div>
                    <h1>Your Cart</h1>
                    <p>{itemCount} items in your cart</p>
                </div>

                <button
                    className="clear-cart-button"
                    onClick={async () => {
                        if (window.confirm("Are you sure you want to clear your cart?")) {
                            try {
                                await clearCart();
                            } catch (error) {
                                console.error(error);
                                alert("Unable to clear cart.");
                            }
                        }
                    }}
                >
                    Clear Cart
                </button>
            </div>

            <div className="cart-layout">
                <section className="cart-items">
                    {cart.items.map((item) => (
                        <article className="cart-item" key={item.menuItemId}>
                            <div className="cart-item-image">🍽️</div>

                            <div className="cart-item-details">
                                <h3>{item.name}</h3>
                                <p>₹{Number(item.price).toFixed(2)} each</p>

                                <div className="quantity-control">
                                    <button
                                        disabled={item.quantity <= 1}
                                        onClick={() =>
                                            void updateQuantity(
                                                item.menuItemId,
                                                item.quantity - 1
                                            ).catch(() => alert("Unable to update quantity."))
                                        }
                                    >
                                        −
                                    </button>

                                    <span>{item.quantity}</span>

                                    <button
                                        onClick={() =>
                                            void updateQuantity(
                                                item.menuItemId,
                                                item.quantity + 1
                                            ).catch(() => alert("Unable to update quantity."))
                                        }
                                    >
                                        +
                                    </button>
                                </div>
                            </div>

                            <div className="cart-item-right">
                                <strong>
                                    ₹{(Number(item.price) * item.quantity).toFixed(2)}
                                </strong>

                                <button
                                    className="remove-item-button"
                                    onClick={() =>
                                        void removeItem(item.menuItemId).catch(() =>
                                            alert("Unable to remove item.")
                                        )
                                    }
                                >
                                    Remove
                                </button>
                            </div>
                        </article>
                    ))}
                </section>

                <aside className="cart-summary">
                    <h2>Bill Details</h2>

                    <div className="summary-row">
                        <span>Item total</span>
                        <span>₹{subtotal.toFixed(2)}</span>
                    </div>

                    <div className="summary-row">
                        <span>Delivery fee</span>
                        <span>₹0.00</span>
                    </div>

                    <div className="summary-row">
                        <span>Taxes</span>
                        <span>₹0.00</span>
                    </div>

                    <div className="summary-divider" />

                    <div className="summary-row summary-total">
                        <strong>Total</strong>
                        <strong>₹{subtotal.toFixed(2)}</strong>
                    </div>

                    <button
                        className="checkout-button"
                        disabled={loading || !cart || cart.items.length === 0}
                        onClick={() => navigate("/checkout")}
                    >
                        Proceed to Checkout
                    </button>

                    <p className="checkout-note">
                        Delivery fee and taxes will be calculated during checkout.
                    </p>
                </aside>
            </div>
        </main>
    );
}
