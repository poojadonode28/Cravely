
CREATE TABLE orders (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        user_id BIGINT NOT NULL,
                        restaurant_id BIGINT NOT NULL,
                        delivery_partner_id BIGINT NULL,
                        total_amount DECIMAL(10, 2) NOT NULL,
                        status VARCHAR(50) NOT NULL,
                        created_at DATETIME(6) NOT NULL,

                        CONSTRAINT pk_orders PRIMARY KEY (id),

                        CONSTRAINT fk_orders_restaurant
                            FOREIGN KEY (restaurant_id)
                                REFERENCES restaurants(id)
);

CREATE INDEX idx_orders_user_id
    ON orders(user_id);

CREATE INDEX idx_orders_restaurant_id
    ON orders(restaurant_id);


CREATE TABLE order_items (
                             order_id BIGINT NOT NULL,
                             item_position INT NOT NULL,
                             menu_item_id BIGINT NOT NULL,
                             name VARCHAR(150) NOT NULL,
                             price DECIMAL(10, 2) NOT NULL,
                             quantity INT NOT NULL,

                             CONSTRAINT pk_order_items
                                 PRIMARY KEY (order_id, item_position),

                             CONSTRAINT fk_order_items_order
                                 FOREIGN KEY (order_id)
                                     REFERENCES orders(id)
                                     ON DELETE CASCADE
);
