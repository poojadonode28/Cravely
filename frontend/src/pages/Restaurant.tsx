
import { useEffect, useState } from "react";
import {
    Link,
    useLocation,
    useNavigate,
    useParams,
} from "react-router-dom";

import api from "../api/axios";
import { useCart } from "../context/CartContext";

interface Restaurant {
    id: number;
    name: string;
    description?: string;
    address?: string;
}

interface MenuItem {
    id: number;
    restaurantId: number;
    name: string;
    description?: string;
    price: number;
    imageUrl?: string | null;
    category?: string | null;
    preparationTimeMinutes?: number | null;
    vegetarian: boolean;
    available: boolean;
}

export default function RestaurantPage() {
    const { id } = useParams<{ id: string }>();

    const navigate = useNavigate();
    const location = useLocation();

    const {
        addItem,
        updateQuantity,
        removeItem,
        cart,
    } = useCart();

    const [restaurant, setRestaurant] =
        useState<Restaurant | null>(null);

    const [menuItems, setMenuItems] =
        useState<MenuItem[]>([]);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [selectedCategory, setSelectedCategory] =
        useState("All");

    const [addingItemId, setAddingItemId] =
        useState<number | null>(null);

    const [message, setMessage] = useState("");

    // Fetch restaurant data only when restaurant ID changes.
    useEffect(() => {
        if (!id) return;

        let cancelled = false;

        const fetchRestaurantData = async () => {
            setLoading(true);
            setError("");

            try {
                const [restaurantResponse, menuResponse] =
                    await Promise.all([
                        api.get(`/restaurants/${id}`),
                        api.get(`/restaurants/${id}/menu/available`),
                    ]);

                if (cancelled) return;

                setRestaurant(restaurantResponse.data);
                setMenuItems(menuResponse.data);
            } catch (err) {
                if (cancelled) return;

                console.error("Restaurant details error:", err);
                setError(
                    "Unable to load restaurant menu. Please try again."
                );
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        };

        void fetchRestaurantData();

        return () => {
            cancelled = true;
        };
    }, [id]);

    // Automatically hide notifications after 3 seconds.
    useEffect(() => {
        if (!message) return;

        const timeout = window.setTimeout(() => {
            setMessage("");
        }, 3000);

        return () => window.clearTimeout(timeout);
    }, [message]);

    const categories = [
        "All",
        ...new Set(
            menuItems
                .map((item) => item.category)
                .filter(
                    (category): category is string =>
                        Boolean(category)
                )
        ),
    ];

    const filteredItems =
        selectedCategory === "All"
            ? menuItems
            : menuItems.filter(
                (item) => item.category === selectedCategory
            );

    const getQuantity = (menuItemId: number): number => {
        return (
            cart?.items.find(
                (cartItem) =>
                    cartItem.menuItemId === menuItemId
            )?.quantity ?? 0
        );
    };

    const validateCart = (item: MenuItem): boolean => {
        if (!localStorage.getItem("token")) {
            navigate("/login", {
                state: { from: location.pathname },
            });
            return false;
        }

        if (
            cart?.restaurantId &&
            cart.restaurantId !== item.restaurantId
        ) {
            setMessage(
                "Your cart contains items from another restaurant. Clear your cart before adding this item."
            );
            return false;
        }

        return true;
    };

    const handleAddToCart = async (item: MenuItem) => {
        if (addingItemId !== null) return;
        if (!item.available) return;
        if (!validateCart(item)) return;

        setAddingItemId(item.id);

        try {
            await addItem(item.id);
            setMessage(`${item.name} added to your cart!`);
        } catch (err: any) {
            console.error("Add to cart failed:", err);

            if (
                err.response?.status === 401 ||
                err.response?.status === 403
            ) {
                setMessage(
                    "Please log in with a customer account to add items."
                );
            } else {
                setMessage(
                    err.response?.data?.message ||
                    "Unable to add item to cart."
                );
            }
        } finally {
            setAddingItemId(null);
        }
    };

    const handleQuantityChange = async (
        item: MenuItem,
        change: number
    ) => {
        if (addingItemId !== null) return;
        if (!validateCart(item)) return;

        const currentQuantity = getQuantity(item.id);
        const newQuantity = currentQuantity + change;

        if (newQuantity < 0) return;

        setAddingItemId(item.id);

        try {
            if (newQuantity === 0) {
                await removeItem(item.id);
            } else {
                await updateQuantity(item.id, newQuantity);
            }
        } catch (err: any) {
            console.error("Quantity update failed:", err);

            setMessage(
                err.response?.data?.message ||
                "Unable to update quantity."
            );
        } finally {
            setAddingItemId(null);
        }
    };

    if (loading) {
        return (
            <main className="container">
                <p className="loading-message">
                    Loading restaurant menu...
                </p>
            </main>
        );
    }

    if (error || !restaurant) {
        return (
            <main className="container">
                <p className="error-message">
                    {error || "Restaurant not found."}
                </p>

                <Link to="/" className="back-link">
                    ← Back to restaurants
                </Link>
            </main>
        );
    }

    return (
        <main className="container restaurant-page">
            <Link to="/" className="back-link">
                ← Back to restaurants
            </Link>

            <section className="restaurant-header">
                <h1>{restaurant.name}</h1>

                <p>{restaurant.description}</p>

                <p className="restaurant-address">
                    📍 {restaurant.address}
                </p>
            </section>

            <section className="menu-section">
                <div className="menu-heading">
                    <div>
                        <h2>Explore our menu</h2>
                        <p>Choose your favourite dishes</p>
                    </div>

                    <span className="menu-count">
            {menuItems.length} items
          </span>
                </div>

                {categories.length > 1 && (
                    <div className="category-list">
                        {categories.map((category) => (
                            <button
                                key={category}
                                type="button"
                                className={
                                    selectedCategory === category
                                        ? "category-button active"
                                        : "category-button"
                                }
                                onClick={() =>
                                    setSelectedCategory(category)
                                }
                            >
                                {category}
                            </button>
                        ))}
                    </div>
                )}

                {/* Fixed notification avoids menu layout shifts. */}
                {message && (
                    <div
                        className="cart-message cart-toast"
                        role="status"
                    >
                        {message}

                        {message.includes("added") && (
                            <Link to="/cart"> View cart</Link>
                        )}
                    </div>
                )}

                <div className="menu-grid">
                    {filteredItems.map((item) => {
                        const quantity = getQuantity(item.id);
                        const updating = addingItemId === item.id;

                        return (
                            <article
                                className="menu-card"
                                key={item.id}
                            >
                                <div className="menu-image-wrapper">
                                    {item.imageUrl ? (
                                        <img
                                            src={item.imageUrl}
                                            alt={item.name}
                                            className="menu-item-image"
                                            loading="lazy"
                                        />
                                    ) : (
                                        <div className="menu-image-placeholder">
                                            🍽️
                                        </div>
                                    )}

                                    <span
                                        className={
                                            item.vegetarian
                                                ? "food-type veg"
                                                : "food-type non-veg"
                                        }
                                        title={
                                            item.vegetarian
                                                ? "Vegetarian"
                                                : "Non-vegetarian"
                                        }
                                    >
                    <span />
                  </span>
                                </div>

                                <div className="menu-item-details">
                                    {item.category && (
                                        <span className="menu-category">
                      {item.category}
                    </span>
                                    )}

                                    <h3>{item.name}</h3>

                                    <p className="menu-description">
                                        {item.description ||
                                            "Delicious food made fresh for you."}
                                    </p>

                                    <div className="menu-item-footer">
                                        <strong className="menu-price">
                                            ₹{Number(item.price).toFixed(2)}
                                        </strong>

                                        <div className="menu-cart-control">
                                            {!item.available ? (
                                                <button
                                                    type="button"
                                                    className="add-button"
                                                    disabled
                                                >
                                                    Unavailable
                                                </button>
                                            ) : quantity === 0 ? (
                                                <button
                                                    type="button"
                                                    className="add-button"
                                                    disabled={
                                                        addingItemId !== null
                                                    }
                                                    onClick={() =>
                                                        void handleAddToCart(item)
                                                    }
                                                >
                                                    ADD +
                                                </button>
                                            ) : (
                                                <div className="menu-quantity-control">
                                                    <button
                                                        type="button"
                                                        aria-label={`Decrease ${item.name}`}
                                                        disabled={
                                                            addingItemId !== null
                                                        }
                                                        onClick={() =>
                                                            void handleQuantityChange(
                                                                item,
                                                                -1
                                                            )
                                                        }
                                                    >
                                                        −
                                                    </button>

                                                    <span aria-live="polite">
                            {quantity}
                          </span>

                                                    <button
                                                        type="button"
                                                        aria-label={`Increase ${item.name}`}
                                                        disabled={
                                                            addingItemId !== null
                                                        }
                                                        onClick={() =>
                                                            void handleQuantityChange(
                                                                item,
                                                                1
                                                            )
                                                        }
                                                    >
                                                        +
                                                    </button>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            </article>
                        );
                    })}
                </div>

                {filteredItems.length === 0 && (
                    <div className="empty-menu">
                        <span>🍽️</span>
                        <h3>No menu items found</h3>
                        <p>Try selecting another category.</p>
                    </div>
                )}
            </section>
        </main>
    );
}
