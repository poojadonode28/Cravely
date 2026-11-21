
CREATE TABLE payments (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          order_id BIGINT NOT NULL,
                          amount DECIMAL(10, 2) NOT NULL,
                          currency VARCHAR(3) NOT NULL DEFAULT 'INR',
                          status VARCHAR(30) NOT NULL,
                          payment_method VARCHAR(30),
                          gateway VARCHAR(50) NOT NULL DEFAULT 'RAZORPAY',
                          gateway_order_id VARCHAR(100),
                          gateway_payment_id VARCHAR(100),
                          idempotency_key VARCHAR(100) NOT NULL,
                          created_at DATETIME(6) NOT NULL,
                          updated_at DATETIME(6) NOT NULL,
                          version BIGINT NOT NULL DEFAULT 0,

                          CONSTRAINT pk_payments PRIMARY KEY (id),

                          CONSTRAINT fk_payments_order
                              FOREIGN KEY (order_id) REFERENCES orders(id),

                          CONSTRAINT uk_payments_idempotency_key
                              UNIQUE (idempotency_key),

                          CONSTRAINT uk_payments_gateway_order
                              UNIQUE (gateway_order_id),

                          CONSTRAINT uk_payments_gateway_payment
                              UNIQUE (gateway_payment_id),

                          CONSTRAINT chk_payments_amount
                              CHECK (amount > 0)
);

CREATE INDEX idx_payments_order_id
    ON payments(order_id);

CREATE INDEX idx_payments_status
    ON payments(status);
