DROP DATABASE IF EXISTS pay_my_buddy;
CREATE DATABASE pay_my_buddy;
USE pay_my_buddy;

DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    CONSTRAINT unique_email UNIQUE (email)
);

DROP TABLE IF EXISTS user_contacts;
CREATE TABLE user_contacts (
    user_id INT NOT NULL,
    contact_id INT NOT NULL,
    PRIMARY KEY (user_id, contact_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_contact FOREIGN KEY (contact_id) REFERENCES users (id)
);

DROP TABLE IF EXISTS accounts;
CREATE TABLE accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_account FOREIGN KEY (user_id) REFERENCES users (id)
) AUTO_INCREMENT = 30000;

DROP TABLE IF EXISTS transactions;
CREATE TABLE transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_account_id INT NOT NULL,
    receiver_account_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    description VARCHAR(255),
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sender_account FOREIGN KEY (sender_account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_receiver_account FOREIGN KEY (receiver_account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

INSERT INTO users (username, email, password)
VALUES
    ('Alice', 'alice@example.com', '$2a$12$IHcw/w11QtHyvSsa/PkTcOxfU6y7ylauBe07d1ZIDaFWKeghOpHF6'),
    ('Bob', 'bob@example.com', '$2a$12$x3INJ0gPAbgqn3Xz4gQtQOxbmwngp/uPE.yFg3bLHmJEYibCvHcre'),
    ('Charlie', 'charlie@example.com', '$2a$12$WfggkW8z2STUMs1fsm0SouOFAgdtKwR5nZiQNe246ybMh/tGgNlma'),
    ('David', 'david@example.com', '$2a$12$iZnlODlu9HDgV8GLfh.sQ.yTDP/1ObLUVOU8lVo4LrsUEu5gY6A0y'),
    ('Redha', 'redha.boubakour@gmail.com', '$2a$10$LIpKVUajUvQvtyX9xWslYer7phf/6N/nswXS6jxRvvdIC1vg79qN.');

INSERT INTO accounts (user_id, balance, name)
VALUES
    (1, 100.00, 'Pay My Buddy'),
    (2, 500.00, 'Pay My Buddy'),
    (3, 50.00, 'Pay My Buddy'),
    (4, 25.00, 'Pay My Buddy'),
    (5, 10.00, 'Pay My Buddy');
