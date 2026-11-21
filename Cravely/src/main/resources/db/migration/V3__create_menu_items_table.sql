
CREATE TABLE menu_items (
                            id BIGINT NOT NULL AUTO_INCREMENT,
                            restaurant_id BIGINT NOT NULL,
                            name VARCHAR(150) NOT NULL,
                            description VARCHAR(1000),
                            price DECIMAL(10, 2) NOT NULL,
                            available BOOLEAN NOT NULL DEFAULT TRUE,

                            CONSTRAINT pk_menu_items PRIMARY KEY (id),

                            CONSTRAINT fk_menu_items_restaurant
                                FOREIGN KEY (restaurant_id)
                                    REFERENCES restaurants(id)
);

CREATE INDEX idx_menu_items_restaurant_id
    ON menu_items(restaurant_id);
