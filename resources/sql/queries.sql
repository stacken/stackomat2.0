-- name: create-product-table!
CREATE TABLE products (
	id varchar(30) PRIMARY KEY,
	name varchar(255),
	price money
);

-- name: create-users-table!
CREATE TABLE users (
	id varchar(64) PRIMARY KEY,
	balance money CHECK (balance >= 0::money)
);

-- name: drop-products-table!
DROP TABLE products;

-- name: drop-users-table!
DROP TABLE users;

-- name: list-products
SELECT id, name, price::numeric FROM products;

-- name: add-user!
INSERT INTO users (id, balance) VALUES (:id, :balance);

-- name: pay!
UPDATE users
SET balance = balance - :amount ::numeric::money
WHERE id = :id;

-- name: add-money!
UPDATE users
SET balance = balance + :amount :: numeric::money
WHERE id = :id;

-- name: add-product!
INSERT INTO products (id, name, price) VALUES (:id, :name, :price);

-- name: change-product!
UPDATE products
SET name = :name, price = :price 
WHERE id = :id;

-- name: delete-product!
DELETE FROM products WHERE id = :id;

-- name: select-balance
SELECT balance::numeric FROM users WHERE id = :id;

-- name: select-user
SELECT id, balance::numeric FROM users WHERE id = :id;
