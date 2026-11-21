
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from "react";
import api from "../api/axios";

export interface CartItem {
  menuItemId: number;
  name: string;
  price: number;
  quantity: number;
}

export interface Cart {
  id: number | null;
  userId: number;
  restaurantId: number | null;
  items: CartItem[];
}

interface CartContextType {
  cart: Cart | null;
  loading: boolean;
  itemCount: number;
  subtotal: number;
  refreshCart: () => Promise<void>;
  addItem: (menuItemId: number) => Promise<void>;
  updateQuantity: (menuItemId: number, quantity: number) => Promise<void>;
  removeItem: (menuItemId: number) => Promise<void>;
  clearCart: () => Promise<void>;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export function CartProvider({ children }: { children: ReactNode }) {
  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState(false);

  const refreshCart = useCallback(async () => {
    if (!localStorage.getItem("token")) {
      setCart(null);
      return;
    }

    setLoading(true);

    try {
      const response = await api.get("/cart");
      setCart(response.data);
    } catch (error) {
      console.error("Unable to fetch cart:", error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void refreshCart();
  }, [refreshCart]);

  const addItem = async (menuItemId: number) => {
    const response = await api.post("/cart/items", null, {
      params: {
        menuItemId,
        quantity: 1,
      },
    });

    setCart(response.data);
  };

  const updateQuantity = async (menuItemId: number, quantity: number) => {
    if (quantity <= 0) {
      await removeItem(menuItemId);
      return;
    }

    const response = await api.put(
      `/cart/items/${menuItemId}`,
      null,
      { params: { quantity } }
    );

    setCart(response.data);
  };

  const removeItem = async (menuItemId: number) => {
    const response = await api.delete(`/cart/items/${menuItemId}`);
    setCart(response.data);
  };

  const clearCart = async () => {
    await api.delete("/cart");
    await refreshCart();
  };

  const itemCount =
    cart?.items.reduce((total, item) => total + item.quantity, 0) ?? 0;

  const subtotal =
    cart?.items.reduce(
      (total, item) => total + Number(item.price) * item.quantity,
      0
    ) ?? 0;

  return (
    <CartContext.Provider
      value={{
        cart,
        loading,
        itemCount,
        subtotal,
        refreshCart,
        addItem,
        updateQuantity,
        removeItem,
        clearCart,
      }}
    >
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const context = useContext(CartContext);

  if (!context) {
    throw new Error("useCart must be used inside CartProvider");
  }

  return context;
}
