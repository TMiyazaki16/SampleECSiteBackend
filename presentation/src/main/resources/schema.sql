-- ユーザーテーブル
CREATE TABLE IF NOT EXISTS users
(
    id           VARCHAR(36)  NOT NULL PRIMARY KEY,
    email        VARCHAR(255) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    name         VARCHAR(255) NOT NULL,
    company_name VARCHAR(255) NOT NULL DEFAULT '',
    role         VARCHAR(20)  NOT NULL DEFAULT 'BUYER',
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL
);

-- 商品テーブル
CREATE TABLE IF NOT EXISTS products
(
    id          VARCHAR(36)    NOT NULL PRIMARY KEY,
    name        VARCHAR(255)   NOT NULL,
    description TEXT,
    price       DECIMAL(12, 2) NOT NULL,
    currency    VARCHAR(3)     NOT NULL DEFAULT 'JPY',
    stock       INT            NOT NULL DEFAULT 0,
    category    VARCHAR(100)   NOT NULL DEFAULT '',
    image_url   VARCHAR(500),
    version     INT            NOT NULL DEFAULT 0,
    created_at  TIMESTAMP      NOT NULL,
    updated_at  TIMESTAMP      NOT NULL
);

-- 注文テーブル
CREATE TABLE IF NOT EXISTS orders
(
    id          VARCHAR(36)    NOT NULL PRIMARY KEY,
    buyer_id    VARCHAR(36)    NOT NULL,
    total_price DECIMAL(12, 2) NOT NULL,
    status      VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP      NOT NULL,
    updated_at  TIMESTAMP      NOT NULL,
    FOREIGN KEY (buyer_id) REFERENCES users (id)
);

-- 注文明細テーブル
CREATE TABLE IF NOT EXISTS order_items
(
    id         VARCHAR(36)    NOT NULL PRIMARY KEY,
    order_id   VARCHAR(36)    NOT NULL,
    product_id VARCHAR(36)    NOT NULL,
    quantity   INT            NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders (id),
    FOREIGN KEY (product_id) REFERENCES products (id)
);
