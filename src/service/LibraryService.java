package service;

import constants.AppConstants;
import dao.BookDAO;
import model.Book;

import java.util.List;

/** The library rules. The data itself is stored in MySQL (see BookDAO). */
public class LibraryService {

    private static final int MAX_LENGTH = 255; // size of the title / author columns

    private final BookDAO bookDAO = new BookDAO();

    // Returns false if the title or author is empty or too long
    public boolean addBook(String title, String author) {
        title = title.trim();
        author = author.trim();
        if (title.isEmpty() || author.isEmpty()
                || title.length() > MAX_LENGTH || author.length() > MAX_LENGTH) {
            return false;
        }

        bookDAO.insert(title, author);
        return true;
    }

    public List<Book> getAllBooks() {
        return bookDAO.findAll();
    }

    // Matches the keyword against title or author (ignores uppercase/lowercase)
    public List<Book> searchBooks(String keyword) {
        return searchBooks(keyword, false);
    }

    // Same, and the admin can also search by the name of the borrower
    public List<Book> searchBooks(String keyword, boolean includeBorrower) {
        return bookDAO.search(keyword, includeBorrower);
    }

    // How many books this student currently has
    public int countBorrowedBy(String username) {
        return bookDAO.countBorrowedBy(username);
    }

    // Returns a message the UI can show.
    // Messages that start with "You " or "Deleted" mean it worked; anything else means it did not.
    public String borrowBook(int id, String username) {
        Book book = bookDAO.findById(id);

        if (book == null) {
            return "Book not found.";
        }
        if (book.getStatus().equals(AppConstants.STATUS_BORROWED)) {
            return "Sorry, this book is already borrowed.";
        }
        // the database only changes the row if it is still Available (two people can't take it at once)
        if (!bookDAO.markBorrowed(id, username)) {
            return "Sorry, this book is already borrowed.";
        }
        return "You borrowed \"" + book.getTitle() + "\".";
    }

    public String returnBook(int id, String username) {
        Book book = bookDAO.findById(id);

        if (book == null) {
            return "Book not found.";
        }
        if (book.getStatus().equals(AppConstants.STATUS_AVAILABLE)) {
            return "This book was not borrowed.";
        }
        // Only the student who borrowed it can return it
        // (a borrowed book with no borrower name can be returned by anyone)
        if (!book.getBorrowedBy().isEmpty() && !book.getBorrowedBy().equalsIgnoreCase(username)) {
            return "This book was borrowed by someone else.";
        }
        if (!bookDAO.markReturned(id)) {
            return "This book was not borrowed.";
        }
        return "You returned \"" + book.getTitle() + "\".";
    }

    // Admin only (the UI only shows this button to the admin)
    public String deleteBook(int id) {
        Book book = bookDAO.findById(id);

        if (book == null) {
            return "Book not found.";
        }
        if (book.getStatus().equals(AppConstants.STATUS_BORROWED)) {
            String who = book.getBorrowedBy().isEmpty() ? "a student" : book.getBorrowedBy();
            return "Can't delete: this book is borrowed by " + who + ".";
        }
        if (!bookDAO.deleteIfAvailable(id)) {
            return "Can't delete: this book was just borrowed.";
        }
        return "Deleted \"" + book.getTitle() + "\".";
    }
}
