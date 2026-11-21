
import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import api from "../api/axios";
import { useCart } from "../context/CartContext";

interface LoginResponse {
    accessToken: string;
    tokenType: string;
    expiresIn: number;
    user: {
        id: number;
        name: string;
        email: string;
        role: string;
    };
}

export default function Login() {
    const navigate = useNavigate();
    const location = useLocation();
    const { refreshCart } = useCart();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setError("");
        setLoading(true);

        try {
            const response = await api.post<LoginResponse>("/auth/login", {
                email,
                password,
            });

            const data = response.data;

            if (data.user.role !== "CUSTOMER") {
                setError("Please log in using a customer account to order food.");
                return;
            }

            localStorage.setItem("token", data.accessToken);
            localStorage.setItem("user", JSON.stringify(data.user));

            await refreshCart();

            const destination =
                (location.state as { from?: string } | null)?.from || "/";

            navigate(destination, { replace: true });
        } catch (err: any) {
            console.error("Login failed:", err);

            setError(
                err.response?.data?.message ||
                "Invalid email or password. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <main className="auth-container">
            <form className="auth-card" onSubmit={handleLogin}>
                <h1>Welcome back!</h1>
                <p>Login to order your favourite food.</p>

                {error && <div className="auth-error">{error}</div>}

                <label htmlFor="email">Email address</label>
                <input
                    id="email"
                    type="email"
                    placeholder="Enter your email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                />

                <label htmlFor="password">Password</label>
                <input
                    id="password"
                    type="password"
                    placeholder="Enter your password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />

                <button type="submit" disabled={loading}>
                    {loading ? "Logging in..." : "Login"}
                </button>

                <div className="auth-footer">
                    Don't have an account? <Link to="/register">Register</Link>
                </div>
            </form>
        </main>
    );
}
