package model;

import constants.AppConstants;

public class Book {
    private int id;
    private String title;
    private String author;
    private String status;
    private String borrowedBy; // username of the student who has the book ("" if nobody)

    public Book(int id, String title, String author, String status, String borrowedBy) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.status = status;
        this.borrowedBy = borrowedBy == null ? "" : borrowedBy;
    }

    // Used when loading old Book.txt lines that have no "borrowed by" part
    public Book(int id, String title, String author, String status) {
        this(id, title, author, status, "");
    }

    // New books start as Available
    public Book(int id, String title, String author) {
        this(id, title, author, AppConstants.STATUS_AVAILABLE, "");
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getStatus() { return status; }
    public String getBorrowedBy() { return borrowedBy; }

    public void setStatus(String status) { this.status = status; }
    public void setBorrowedBy(String borrowedBy) { this.borrowedBy = borrowedBy == null ? "" : borrowedBy; }

    // How the book is saved as one line in Book.txt
    public String toFileString() {
        return id + AppConstants.DELIMITER + title + AppConstants.DELIMITER
                + author + AppConstants.DELIMITER + status + AppConstants.DELIMITER + borrowedBy;
    }

    @Override
    public String toString() {
        return id + " | " + title + " | " + author + " | " + status
                + (borrowedBy.isEmpty() ? "" : " | " + borrowedBy);
    }
}