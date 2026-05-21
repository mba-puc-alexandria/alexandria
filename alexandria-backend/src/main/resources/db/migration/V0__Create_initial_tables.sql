-- V0: Criacao das tabelas iniciais
-- Esta migration cria o schema inicial para que as migrations V001 e V002 possam executar
-- Motivacao: Flyway precisa que as tabelas existam antes de fazer ALTER TABLE

CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255),
    genre VARCHAR(255),
    publisher_id BIGINT,
    source VARCHAR(255) NOT NULL
);
CREATE UNIQUE INDEX idx_title ON books(title);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME
);

CREATE TABLE user_books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'toread',
    progress INT,
    rating INT,
    created_at DATETIME,
    CONSTRAINT fk_user_books_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_books_book FOREIGN KEY (book_id) REFERENCES books(id),
    UNIQUE (user_id, book_id)
);
