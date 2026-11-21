CREATE TABLE restaurants (
                             id BIGINT NOT NULL AUTO_INCREMENT,
                             name VARCHAR(255) NOT NULL,
                             description TEXT,
                             address VARCHAR(500),
                             latitude DOUBLE NOT NULL,
                             longitude DOUBLE NOT NULL,
                             active BOOLEAN NOT NULL DEFAULT TRUE,
                             PRIMARY KEY (id)
);