CREATE TABLE delivery_partners (
                                   id BIGINT NOT NULL AUTO_INCREMENT,
                                   name VARCHAR(150) NOT NULL,
                                   phone VARCHAR(20) NOT NULL,
                                   status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
                                   created_at DATETIME(6) NOT NULL,
                                   version BIGINT NOT NULL DEFAULT 0,

                                   CONSTRAINT pk_delivery_partners PRIMARY KEY (id),
                                   CONSTRAINT uk_delivery_partners_phone UNIQUE (phone)
);

CREATE INDEX idx_delivery_partners_status
    ON delivery_partners(status);
