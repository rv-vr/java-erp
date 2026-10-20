-- BB Bricks ERP - Sales Order module
-- Extends dbemployee (import Employee/dbemployee.sql first) with the spec.md
-- fields: credit limit / AR balance, PO + SQ references, delivery details, and
-- per-line inventory allocation + backorder, and a product unit of measure.
--
-- Run once:  mariadb -u root dbemployee < db-bberp.sql
-- Requires MariaDB 10.0.2+ / MySQL 8.0.29+ for ADD COLUMN IF NOT EXISTS.

ALTER TABLE tblcustomer
    ADD COLUMN IF NOT EXISTS CreditLimit    DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS CurrentARBalance DECIMAL(14,2) NOT NULL DEFAULT 0.00;

ALTER TABLE tblproduct
    ADD COLUMN IF NOT EXISTS UOM VARCHAR(20) NOT NULL DEFAULT 'Pieces';

ALTER TABLE tblsalesorder
    ADD COLUMN IF NOT EXISTS CustomerPONo         VARCHAR(60),
    ADD COLUMN IF NOT EXISTS SQRef                VARCHAR(60),
    ADD COLUMN IF NOT EXISTS PaymentTerms         VARCHAR(40)  NOT NULL DEFAULT 'Net 30',
    ADD COLUMN IF NOT EXISTS ShipToAddress        VARCHAR(255),
    ADD COLUMN IF NOT EXISTS RequestedDeliveryDate DATE,
    ADD COLUMN IF NOT EXISTS DeliveryInstructions VARCHAR(255),
    ADD COLUMN IF NOT EXISTS HoldReason           VARCHAR(255);

ALTER TABLE tblsalesorderline
    ADD COLUMN IF NOT EXISTS AllocatedQty  INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS BackorderQty  INT NOT NULL DEFAULT 0;

-- Seed data. Captures the current max IDs so inserts below never collide with
-- the rows from Employee/dbemployee.sql.
SET @max_customer = (SELECT COALESCE(MAX(ID), 0) FROM tblcustomer);
SET @max_product  = (SELECT COALESCE(MAX(ID), 0) FROM tblproduct);

INSERT INTO tblcustomer (CustomerName, Address, Phone, Email, CreditLimit, CurrentARBalance)
SELECT 'ABC Construction Corp.', 'Project Site A, Quezon City', '02-8123-4567', 'ar@abc-construction.test', 1000000.00, 800000.00
WHERE NOT EXISTS (SELECT 1 FROM tblcustomer WHERE CustomerName = 'ABC Construction Corp.');

INSERT INTO tblcustomer (CustomerName, Address, Phone, Email, CreditLimit, CurrentARBalance)
SELECT 'XYZ Builders Inc.', '12 Ortigas Ave, Pasig', '02-8555-0102', 'ap@xyzbuilders.test', 500000.00, 120000.00
WHERE NOT EXISTS (SELECT 1 FROM tblcustomer WHERE CustomerName = 'XYZ Builders Inc.');

INSERT INTO tblcustomer (CustomerName, Address, Phone, Email, CreditLimit, CurrentARBalance)
SELECT 'Lotus Homes Co.', '88 Marcos Hwy, Antipolo', '02-8777-0033', 'po@lotushomes.test', 250000.00, 0.00
WHERE NOT EXISTS (SELECT 1 FROM tblcustomer WHERE CustomerName = 'Lotus Homes Co.');

INSERT INTO tblproduct (ProductCode, ProductName, UnitPrice, StockQty, UOM)
SELECT 'CEM-OPC-50KG', 'Ordinary Portland Cement 50kg', 100.00, 300, 'Tons'
WHERE NOT EXISTS (SELECT 1 FROM tblproduct WHERE ProductCode = 'CEM-OPC-50KG');

INSERT INTO tblproduct (ProductCode, ProductName, UnitPrice, StockQty, UOM)
SELECT 'CHB-4IN', 'Hollow Blocks 4 inch', 15.00, 10000, 'Pieces'
WHERE NOT EXISTS (SELECT 1 FROM tblproduct WHERE ProductCode = 'CHB-4IN');

INSERT INTO tblproduct (ProductCode, ProductName, UnitPrice, StockQty, UOM)
SELECT 'CLAY-BRK', 'Clay Bricks (per ton)', 250.00, 120, 'Tons'
WHERE NOT EXISTS (SELECT 1 FROM tblproduct WHERE ProductCode = 'CLAY-BRK');

INSERT INTO tblemployee (EmployeeName, Username, Password)
SELECT 'Sales Admin', 'admin', 'admin'
WHERE NOT EXISTS (SELECT 1 FROM tblemployee WHERE Username = 'admin');
