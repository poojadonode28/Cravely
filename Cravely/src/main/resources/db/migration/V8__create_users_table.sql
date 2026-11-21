
CREATE TABLE app_users (
                           id BIGINT NOT NULL AUTO_INCREMENT,
                           name VARCHAR(100) NOT NULL,
                           email VARCHAR(150) NOT NULL,
                           password_hash VARCHAR(255) NOT NULL,
                           phone VARCHAR(20),
                           role VARCHAR(30) NOT NULL,
                           enabled BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at DATETIME(6) NOT NULL,
                           updated_at DATETIME(6) NOT NULL,
                           version BIGINT NOT NULL DEFAULT 0,

                           CONSTRAINT pk_app_users PRIMARY KEY (id),
                           CONSTRAINT uk_app_users_email UNIQUE (email),
                           CONSTRAINT uk_app_users_phone UNIQUE (phone)
);

CREATE INDEX idx_app_users_role ON app_users(role);
