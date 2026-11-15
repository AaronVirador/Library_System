package util;

import dao.BookDAO;
import dao.UserDAO;
import model.Book;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Self-test for the MySQL connection. It adds a temporary student and book, uses them,
 * and deletes them again, so your real data is not touched.
 *
 * Run it from the project root:
 *     java -cp "bin:lib/*" util.DbCheck
 */
public class DbCheck {

    private static int failed = 0;

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "  OK    " : "  FAIL  ") + name);
        if (!ok) failed++;
    }

    public static void main(String[] args) {
        System.out.println("Library System - database check\n");

        // 1. Can we connect, and what are we connected to?
        try (Connection con = DBConnection.get()) {
            System.out.println("Connected to " + con.getMetaData().getDatabaseProductName() + " "
                    + con.getMetaData().getDatabaseProductVersion());
            System.out.println("Database: " + con.getCatalog() + "\n");
        } catch (SQLException e) {
            System.out.println("Could not connect.\n\n" + DBConnection.explain(e));
            return;
        }

        UserDAO userDAO = new UserDAO();
        BookDAO bookDAO = new BookDAO();
        String testUser = "dbcheck_tmp";
        String hash = PasswordUtil.hash("test");
        int bookId = -1;

        try {
            // 2. Users
            userDAO.insert(new User(testUser, hash, "question?", hash)); // fine if left over from a failed run
            check("add a student", userDAO.findByUsername(testUser) != null);
            check("student lookup is not case-sensitive", userDAO.findByUsername("DBCHECK_TMP") != null);
            check("same username is refused", !userDAO.insert(new User("DBCHECK_TMP", hash, "q", hash)));

            // 3. Books
            bookId = bookDAO.insert("__dbcheck__ Ñandú's Book", "Author, With Comma");
            Book book = bookDAO.findById(bookId);
            check("add a book (commas and accents are fine)",
                    book != null && book.getTitle().equals("__dbcheck__ Ñandú's Book")
                            && book.getAuthor().equals("Author, With Comma"));
            check("new book is Available with no borrower",
                    book != null && book.getStatus().equals("Available") && book.getBorrowedBy().isEmpty());

            check("borrow the book", bookDAO.markBorrowed(bookId, testUser));
            check("second borrow is refused", !bookDAO.markBorrowed(bookId, testUser));
            Book borrowed = bookDAO.findById(bookId);
            check("borrower is saved", borrowed.getStatus().equals("Borrowed") && borrowed.getBorrowedBy().equalsIgnoreCase(testUser));
            check("admin search finds it by borrower", containsId(bookDAO.search("dbcheck_t", true), bookId));
            check("student search does not search borrowers", !containsId(bookDAO.search("dbcheck_t", false), bookId));
            check("delete is refused while borrowed", !bookDAO.deleteIfAvailable(bookId));

            check("return the book", bookDAO.markReturned(bookId));
            check("returned book has no borrower", bookDAO.findById(bookId).getBorrowedBy().isEmpty());
            check("search works with % and _ typed in", bookDAO.search("100%", false).isEmpty());

            check("delete the book", bookDAO.deleteIfAvailable(bookId));
            check("deleted book is gone", bookDAO.findById(bookId) == null);
            bookId = -1;
        } catch (RuntimeException e) {
            failed++;
            System.out.println("\n  FAIL  " + DBConnection.explain(e));
        } finally {
            // clean up the temporary data, whatever happened
            try (Connection con = DBConnection.get()) {
                if (bookId != -1) run(con, "DELETE FROM books WHERE id = ?", String.valueOf(bookId));
                run(con, "DELETE FROM users WHERE username = ?", testUser);
            } catch (SQLException ignored) {
                System.out.println("Could not clean up the temporary data.");
            }
        }

        System.out.println(failed == 0 ? "\nAll checks passed. MySQL is ready." : "\n" + failed + " check(s) failed.");
    }

    private static boolean containsId(List<Book> books, int id) {
        for (Book b : books) if (b.getId() == id) return true;
        return false;
    }

    private static void run(Connection con, String sql, String value) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.executeUpdate();
        }
    }
}
