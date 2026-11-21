
CREATE TABLE carts (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       user_id BIGINT NOT NULL,
                       restaurant_id BIGINT NULL,

                       CONSTRAINT pk_carts PRIMARY KEY (id),
                       CONSTRAINT uk_cart_user_id UNIQUE (user_id)
);

CREATE TABLE cart_items (
                            cart_id BIGINT NOT NULL,
                            item_position INT NOT NULL,
                            menu_item_id BIGINT NOT NULL,
                            name VARCHAR(255) NOT NULL,
                            price DECIMAL(10, 2) NOT NULL,
                            quantity INT NOT NULL,

                            CONSTRAINT pk_cart_items
                                PRIMARY KEY (cart_id, item_position),

                            CONSTRAINT uk_cart_item
                                UNIQUE (cart_id, menu_item_id),

                            CONSTRAINT fk_cart_items_cart
                                FOREIGN KEY (cart_id)
                                    REFERENCES carts(id)
                                    ON DELETE CASCADE
);
