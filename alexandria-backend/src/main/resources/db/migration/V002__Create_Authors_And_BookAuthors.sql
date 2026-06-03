CREATE TABLE authors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    birth_year INT NULL,
    death_year INT NULL
);

CREATE TABLE book_authors (
    book_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    CONSTRAINT fk_book_author_book FOREIGN KEY (book_id) REFERENCES books(id),
    CONSTRAINT fk_book_author_author FOREIGN KEY (author_id) REFERENCES authors(id)
);

INSERT INTO authors (name)
SELECT DISTINCT TRIM(author) FROM books
WHERE author IS NOT NULL AND TRIM(author) != '';

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id FROM books b
JOIN authors a ON TRIM(b.author) = a.name;

ALTER TABLE books DROP COLUMN author;

CREATE INDEX idx_author_name ON authors(name);
CREATE INDEX idx_book_authors_book ON book_authors(book_id);
CREATE INDEX idx_book_authors_author ON book_authors(author_id);
