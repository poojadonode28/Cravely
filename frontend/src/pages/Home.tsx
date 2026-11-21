
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/axios";

interface Restaurant {
    id: number;
    name: string;
    description?: string;
    address?: string;
    active?: boolean;
    imageKey?: string | null;
    imageUrl?: string | null;
}

export default function Home() {
    const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const fetchRestaurants = async () => {
            try {
                const response = await api.get<Restaurant[]>("/restaurants");
                setRestaurants(response.data);
            } catch (err) {
                console.error(err);
                setError("Unable to load restaurants. Please try again.");
            } finally {
                setLoading(false);
            }
        };

        fetchRestaurants();
    }, []);

    if (loading) {
        return <main className="container">Loading restaurants...</main>;
    }

    if (error) {
        return <main className="container">{error}</main>;
    }

    return (
        <main className="container">
            <h1>Discover great food 🍕</h1>
            <p>Order your favourite meals with Cravely.</p>

            <div className="restaurant-grid">
                {restaurants.map((restaurant) => (
                    <Link
                        to={`/restaurant/${restaurant.id}`}
                        className="restaurant-card"
                        key={restaurant.id}
                    >
                        <div className="restaurant-image">
                            {restaurant.imageUrl ? (
                                <img
                                    src={restaurant.imageUrl}
                                    alt={restaurant.name}
                                    className="restaurant-cover-image"
                                    loading="lazy"
                                />
                            ) : (
                                <span>🍽️</span>
                            )}
                        </div>

                        <h3>{restaurant.name}</h3>

                        <p>
                            {restaurant.description || restaurant.address}
                        </p>
                    </Link>
                ))}
            </div>

            {restaurants.length === 0 && (
                <p>No restaurants available right now.</p>
            )}
        </main>
    );
}
