CREATE TABLE IF NOT EXISTS invoice (
    id BIGSERIAL PRIMARY KEY,
    public_token VARCHAR(36) NOT NULL UNIQUE,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    total_net NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_vat NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_gross NUMERIC(12,2) NOT NULL DEFAULT 0,
    client_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT now(),
    CONSTRAINT fk_invoice_client FOREIGN KEY (client_id) REFERENCES client(id)
);