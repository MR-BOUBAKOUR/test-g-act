-- Créer la base de données de test
DROP DATABASE IF EXISTS pay_my_buddy_test;
CREATE DATABASE pay_my_buddy_test;
USE pay_my_buddy_test;

SET FOREIGN_KEY_CHECKS = 0;

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
);

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
    ('David', 'david@example.com', '$2a$12$iZnlODlu9HDgV8GLfh.sQ.yTDP/1ObLUVOU8lVo4LrsUEu5gY6A0y');

INSERT INTO accounts (user_id, balance, name)
VALUES
    (1, 100.00, 'Account 1 for Alice'),
    (1, 200.00, 'Account 2 for Alice'),
    (2, 200.00, 'Account 1 for Bob'),
    (3, 300.00, 'Account 1 for Claire'),
    (4, 400.00, 'Account 1 for David');


INSERT INTO user_contacts (user_id, contact_id)
VALUES
    (1, 2),
    (2, 1),
    (1, 3),
    (3, 1),
    (3, 5),
    (5, 3);

INSERT INTO transactions (sender_account_id, receiver_account_id, amount, description, type)
VALUES
    (1, 2, 50.00, 'Payment for service', 'SELF_TRANSFER'),
    (2, 3, 30.00, 'Payment for product', 'BENEFICIARY_TRANSFER'),
    (3, 4, 100.00, 'Payment for service', 'BENEFICIARY_TRANSFER'),
    (4, 5, 150.00, 'Payment for goods', 'BENEFICIARY_TRANSFER');

SET FOREIGN_KEY_CHECKS = 1;