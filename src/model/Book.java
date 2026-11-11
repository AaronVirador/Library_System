package model;

import constants.AppConstants;

public class Book {
    private int id;
    private String title;
    private String author;
    private String status;

    public Book(int id, String title, String author, String status) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.status = status;
    }

    // New books start as Available
    public Book(int id, String title, String author) {
        this(id, title, author, AppConstants.STATUS_AVAILABLE);
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    // How the book is saved as one line in Book.txt
    public String toFileString() {
        return id + AppConstants.DELIMITER + title + AppConstants.DELIMITER
                + author + AppConstants.DELIMITER + status;
    }

    @Override
    public String toString() {
        return id + " | " + title + " | " + author + " | " + status;
    }
}