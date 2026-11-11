package service;

import constants.AppConstants;
import model.Book;
import util.FileHandler;

import java.util.ArrayList;
import java.util.List;

public class LibraryService {

    private List<Book> books;

    public LibraryService() {
        books = FileHandler.loadBooks();
    }

    // Next ID = highest existing ID + 1
    private int generateId() {
        int maxId = 0;
        for (Book book : books) {
            if (book.getId() > maxId) {
                maxId = book.getId();
            }
        }
        return maxId + 1;
    }

    // Finds a book by its ID, or returns null if not found
    private Book findById(int id) {
        for (Book book : books) {
            if (book.getId() == id) {
                return book;
            }
        }
        return null;
    }

    public boolean addBook(String title, String author) {
        // A comma would break the Book.txt format
        if (title.isEmpty() || author.isEmpty()
                || title.contains(AppConstants.DELIMITER)
                || author.contains(AppConstants.DELIMITER)) {
            return false;
        }

        books.add(new Book(generateId(), title, author));
        FileHandler.saveBooks(books);
        return true;
    }

    public List<Book> getAllBooks() {
        return books;
    }

    // Matches the keyword against title or author (ignores uppercase/lowercase)
    public List<Book> searchBooks(String keyword) {
        List<Book> results = new ArrayList<>();
        String key = keyword.toLowerCase();

        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(key)
                    || book.getAuthor().toLowerCase().contains(key)) {
                results.add(book);
            }
        }
        return results;
    }

    // Returns a message the UI can show
    public String borrowBook(int id) {
        Book book = findById(id);

        if (book == null) {
            return "Book not found.";
        }
        if (book.getStatus().equals(AppConstants.STATUS_BORROWED)) {
            return "Sorry, this book is already borrowed.";
        }

        book.setStatus(AppConstants.STATUS_BORROWED);
        FileHandler.saveBooks(books);
        return "You borrowed \"" + book.getTitle() + "\".";
    }

    public String returnBook(int id) {
        Book book = findById(id);

        if (book == null) {
            return "Book not found.";
        }
        if (book.getStatus().equals(AppConstants.STATUS_AVAILABLE)) {
            return "This book was not borrowed.";
        }

        book.setStatus(AppConstants.STATUS_AVAILABLE);
        FileHandler.saveBooks(books);
        return "You returned \"" + book.getTitle() + "\".";
    }
}