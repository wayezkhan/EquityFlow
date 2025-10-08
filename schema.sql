-- This script provides the schema for the demat_transactions table.
-- To reset your database, you can uncomment the following line:
-- DROP TABLE IF EXISTS demat_transactions;

CREATE TABLE demat_transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    txn_date DATE NOT NULL,
    txn_type VARCHAR(50) NOT NULL,
    stock_name VARCHAR(255) NOT NULL,
    qty DECIMAL(10, 2) NOT NULL,
    rate DECIMAL(10, 2) NOT NULL,
    credit DECIMAL(10, 2) NOT NULL,
    debit DECIMAL(10, 2) NOT NULL
);
