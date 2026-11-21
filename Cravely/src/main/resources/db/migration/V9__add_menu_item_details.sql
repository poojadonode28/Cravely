ALTER TABLE menu_items
    ADD COLUMN image_key VARCHAR(500),
    ADD COLUMN category VARCHAR(100),
    ADD COLUMN preparation_time_minutes INT,
    ADD COLUMN vegetarian BOOLEAN NOT NULL DEFAULT FALSE;