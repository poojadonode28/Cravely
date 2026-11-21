
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../api/axios";
import { useCart } from "../context/CartContext";

interface OrderResponse {
    id: number;
    totalAmount: number;
    status: string;
}

interface PaymentResponse {
    paymentId: number;
    orderId: number;
    amount: number;
    currency: string;
    status: string;
    gatewayOrderId: string;
    razorpayKeyId: string;
}

interface VerificationResponse {
    paymentId: number;
    orderId: number;
    status: string;
}

interface RazorpayResult {
    razorpay_order_id: string;
    razorpay_payment_id: string;
    razorpay_signature: string;
}

interface RazorpayOptions {
    key: string;
    amount: number;
    currency: string;
    order_id: string;
    name: string;
    description: string;
    handler: (result: RazorpayResult) => void;
    modal: {
        ondismiss: () => void;
    };
}

interface RazorpayInstance {
    open: () => void;
    on: (
        event: string,
        callback: (response: unknown) => void
    ) => void;
}

declare global {
    interface Window {
        Razorpay: new (options: RazorpayOptions) => RazorpayInstance;
    }
}

function loadRazorpay(): Promise<void> {
    return new Promise((resolve, reject) => {
        if (window.Razorpay) {
            resolve();
            return;
        }

        const script = document.createElement("script");
        script.src = "https://checkout.razorpay.com/v1/checkout.js";
        script.onload = () => resolve();
        script.onerror = () =>
            reject(new Error("Unable to load Razorpay Checkout."));
        document.body.appendChild(script);
    });
}

function errorMessage(error: unknown): string {
    const err = error as {
        response?: { data?: { message?: string } };
        message?: string;
    };

    return (
        err.response?.data?.message ||
        err.message ||
        "Something went wrong. Please try again."
    );
}

export default function Checkout() {
    const navigate = useNavigate();
    const { cart, subtotal, refreshCart } = useCart();

    const [processing, setProcessing] = useState(false);
    const [error, setError] = useState("");
    const [pendingOrderId, setPendingOrderId] = useState<number | null>(
        () => {
            const saved = sessionStorage.getItem("cravelyPendingOrderId");
            return saved ? Number(saved) : null;
        }
    );

    const handlePayment = async () => {
        if (processing) return;

        setProcessing(true);
        setError("");

        try {
            await loadRazorpay();

            let orderId = pendingOrderId;

            // Create an order only once. Reuse it on payment retries.
            if (!orderId) {
                if (!cart || cart.items.length === 0) {
                    throw new Error("Your cart is empty.");
                }

                const orderResponse = await api.post<OrderResponse>("/orders");
                orderId = orderResponse.data.id;

                sessionStorage.setItem(
                    "cravelyPendingOrderId",
                    String(orderId)
                );
                setPendingOrderId(orderId);
            }

            const idempotencyKey = crypto.randomUUID();

            const paymentResponse = await api.post<PaymentResponse>(
                `/payments/orders/${orderId}`,
                null,
                {
                    headers: {
                        "Idempotency-Key": idempotencyKey,
                    },
                }
            );

            const payment = paymentResponse.data;

            if (!payment.gatewayOrderId || !payment.razorpayKeyId) {
                throw new Error("Invalid payment response from backend.");
            }

            const options: RazorpayOptions = {
                key: payment.razorpayKeyId,
                amount: Math.round(Number(payment.amount) * 100),
                currency: payment.currency,
                order_id: payment.gatewayOrderId,
                name: "Cravely",
                description: `Payment for order #${orderId}`,

                handler: async (result) => {
                    try {
                        const verification = await api.post<VerificationResponse>(
                            "/payments/verify",
                            {
                                razorpayOrderId: result.razorpay_order_id,
                                razorpayPaymentId: result.razorpay_payment_id,
                                razorpaySignature: result.razorpay_signature,
                            }
                        );

                        if (verification.data.status !== "SUCCESS") {
                            throw new Error(
                                `Payment verification returned: ${verification.data.status}`
                            );
                        }

                        sessionStorage.removeItem("cravelyPendingOrderId");
                        setPendingOrderId(null);

                        await refreshCart().catch(console.error);
                        navigate("/orders", { replace: true });
                    } catch (err) {
                        setError(
                            "Payment verification could not be confirmed. " +
                            errorMessage(err) +
                            " Do not pay again until you check your order."
                        );
                    } finally {
                        setProcessing(false);
                    }
                },

                modal: {
                    ondismiss: () => {
                        setProcessing(false);
                        setError(
                            "Payment window closed. Your order is saved; payment is not confirmed."
                        );
                    },
                },
            };

            const razorpay = new window.Razorpay(options);

            razorpay.on("payment.failed", () => {
                setError("Payment failed. Please check your order before retrying.");
                setProcessing(false);
            });

            razorpay.open();
        } catch (err) {
            console.error("Checkout failed:", err);
            setError(errorMessage(err));
            setProcessing(false);
        }
    };

    if (!localStorage.getItem("token")) {
        return (
            <main className="container">
                <h2>Please log in to continue checkout</h2>
                <Link to="/login">Login</Link>
            </main>
        );
    }

    if ((!cart || cart.items.length === 0) && !pendingOrderId) {
        return (
            <main className="container">
                <h2>Your cart is empty</h2>
                <Link to="/">Browse restaurants</Link>
            </main>
        );
    }

    return (
        <main className="container cart-page">
            <h1>Checkout</h1>

            <section className="cart-summary">
                <h2>Order Summary</h2>

                {cart?.items.map((item) => (
                    <div className="summary-row" key={item.menuItemId}>
            <span>
              {item.name} × {item.quantity}
            </span>
                        <span>
              ₹{(Number(item.price) * item.quantity).toFixed(2)}
            </span>
                    </div>
                ))}

                <div className="summary-divider" />

                <div className="summary-row summary-total">
                    <strong>Cart subtotal</strong>
                    <strong>₹{subtotal.toFixed(2)}</strong>
                </div>

                <p>
                    Final payable amount will be confirmed by the payment gateway.
                </p>

                {pendingOrderId && (
                    <p>Order #{pendingOrderId} created. Payment pending.</p>
                )}

                {error && (
                    <p role="alert" className="auth-error">
                        {error}
                    </p>
                )}

                <button
                    className="checkout-button"
                    disabled={processing}
                    onClick={handlePayment}
                >
                    {processing ? "Processing..." : "Pay with Razorpay"}
                </button>

                <p>
                    <Link to="/orders">View My Orders</Link>
                </p>
            </section>
        </main>
    );
}
