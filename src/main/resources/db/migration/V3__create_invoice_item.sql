CREATE TABLE IF NOT EXISTS invoice_item (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    quantity BIGINT NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    vat_rate NUMERIC(5,2) NOT NULL DEFAULT 23.00,
    net_value NUMERIC(12,2) NOT NULL,
    vat_value NUMERIC(12,2) NOT NULL,
    gross_value NUMERIC(12,2) NOT NULL,
    invoice_id BIGINT NOT NULL,
    CONSTRAINT fk_item_invoice FOREIGN KEY (invoice_id) REFERENCES invoice(id)
);