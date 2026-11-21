
import { useState, useEffect } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useCart } from "../context/CartContext";

interface User {
    id: number;
    name: string;
    email: string;
    role: string;
}

export default function Navbar() {
    const { itemCount } = useCart();
    const navigate = useNavigate();
    const location = useLocation();

    const [user, setUser] = useState<User | null>(null);

    useEffect(() => {
        const storedUser = localStorage.getItem("user");
        const token = localStorage.getItem("token");

        if (storedUser && token) {
            try {
                setUser(JSON.parse(storedUser));
            } catch {
                setUser(null);
            }
        } else {
            setUser(null);
        }
    }, [location.pathname]);

    const handleLogout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        setUser(null);
        navigate("/login", { replace: true });
    };

    return (
        <nav className="navbar">
            <Link to="/" className="navbar-brand">
                Cravely
            </Link>

            <div className="navbar-links">
                <Link to="/">Restaurants</Link>

                {user ? (
                    <>
                        <Link to="/orders">My Orders</Link>

                        <Link to="/cart" className="cart-nav-link">
                            Cart
                            {itemCount > 0 && (
                                <span className="cart-badge">
                                    {itemCount}
                                </span>
                            )}
                        </Link>

                        <span>Hi, {user.name}</span>

                        <button
                            type="button"
                            onClick={handleLogout}
                            className="navbar-logout"
                        >
                            Logout
                        </button>
                    </>
                ) : (
                    <>
                        <Link to="/login">Login</Link>
                        <Link to="/register">Register</Link>
                    </>
                )}
            </div>
        </nav>
    );
}
