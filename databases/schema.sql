-- =====================================================================
--  Library System - database schema (MySQL 8.x)
--
--  Run it once from the project root:
--      sudo mysql < databases/schema.sql
--
--  It is safe to run again: nothing is deleted if the tables already exist.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS library_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE library_db;

-- ---------------------------------------------------------------------
--  Students (the admin account is built into the program, not stored here)
--  Passwords and security answers are stored as SHA-256 hashes (64 characters).
--  The collation is case-insensitive, so "Maria" and "maria" are the same user.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id                INT          NOT NULL AUTO_INCREMENT,
    username          VARCHAR(50)  NOT NULL,
    password_hash     CHAR(64)     NOT NULL,
    security_question VARCHAR(255) NOT NULL,
    answer_hash       CHAR(64)     NOT NULL,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
--  Books
--  borrowed_by is NULL when nobody has the book.
--  If a student account is ever deleted, their books become "borrower unknown"
--  instead of blocking the delete.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS books (
    id          INT          NOT NULL AUTO_INCREMENT,
    title       VARCHAR(255) NOT NULL,
    author      VARCHAR(255) NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'Available',
    borrowed_by VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_books_status CHECK (status IN ('Available', 'Borrowed')),
    CONSTRAINT fk_books_borrower FOREIGN KEY (borrowed_by)
        REFERENCES users (username)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
