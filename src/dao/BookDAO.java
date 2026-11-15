package dao;

import constants.AppConstants;
import model.Book;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** All the SQL for the books table. The rules (who may borrow, delete...) stay in LibraryService. */
public class BookDAO {

    private static final String COLUMNS = "id, title, author, status, borrowed_by";

    private Book map(ResultSet rs) throws SQLException {
        return new Book(rs.getInt("id"), rs.getString("title"), rs.getString("author"),
                rs.getString("status"), rs.getString("borrowed_by")); // borrowed_by can be NULL
    }

    // Sets the ? placeholders (NULL-safe)
    private void fill(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) ps.setNull(i + 1, Types.VARCHAR);
            else ps.setObject(i + 1, params[i]);
        }
    }

    private List<Book> query(String sql, Object... params) {
        try (Connection con = DBConnection.get(); PreparedStatement ps = con.prepareStatement(sql)) {
            fill(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<Book> books = new ArrayList<>();
                while (rs.next()) books.add(map(rs));
                return books;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read the books.", e);
        }
    }

    // Runs INSERT / UPDATE / DELETE and returns how many rows changed
    private int update(String sql, Object... params) {
        try (Connection con = DBConnection.get(); PreparedStatement ps = con.prepareStatement(sql)) {
            fill(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not save the book.", e);
        }
    }

    // ---------- Reading ----------

    public List<Book> findAll() {
        return query("SELECT " + COLUMNS + " FROM books ORDER BY id");
    }

    /** Returns the book, or null if there is no book with that ID. */
    public Book findById(int id) {
        List<Book> books = query("SELECT " + COLUMNS + " FROM books WHERE id = ?", id);
        return books.isEmpty() ? null : books.get(0);
    }

    /** Matches title or author (and the borrower's name, for the admin). Not case-sensitive. */
    public List<Book> search(String keyword, boolean includeBorrower) {
        String like = "%" + escapeLike(keyword) + "%";
        if (includeBorrower) {
            return query("SELECT " + COLUMNS + " FROM books "
                    + "WHERE title LIKE ? OR author LIKE ? OR borrowed_by LIKE ? ORDER BY id", like, like, like);
        }
        return query("SELECT " + COLUMNS + " FROM books WHERE title LIKE ? OR author LIKE ? ORDER BY id", like, like);
    }

    // So that typing % or _ in the search box searches for those characters
    private String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /** How many books this student currently has. */
    public int countBorrowedBy(String username) {
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT COUNT(*) FROM books WHERE status = ? AND borrowed_by = ?")) {
            ps.setString(1, AppConstants.STATUS_BORROWED);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not count the borrowed books.", e);
        }
    }

    // ---------- Writing ----------

    /** Adds a new Available book. MySQL picks the ID. Returns the new ID. */
    public int insert(String title, String author) {
        String sql = "INSERT INTO books (title, author, status) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, AppConstants.STATUS_AVAILABLE);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not add the book.", e);
        }
    }

    /** Adds a book with a fixed ID (used when importing the old text file). */
    public void insertWithId(Book book) {
        String borrower = book.getBorrowedBy().isEmpty() ? null : book.getBorrowedBy();
        update("INSERT INTO books (id, title, author, status, borrowed_by) VALUES (?, ?, ?, ?, ?)",
                book.getId(), book.getTitle(), book.getAuthor(), book.getStatus(), borrower);
    }

    /** Marks an Available book as borrowed. Returns false if somebody else got it first. */
    public boolean markBorrowed(int id, String username) {
        return update("UPDATE books SET status = ?, borrowed_by = ? WHERE id = ? AND status = ?",
                AppConstants.STATUS_BORROWED, username, id, AppConstants.STATUS_AVAILABLE) == 1;
    }

    /** Marks a Borrowed book as available again and clears the borrower. */
    public boolean markReturned(int id) {
        return update("UPDATE books SET status = ?, borrowed_by = NULL WHERE id = ? AND status = ?",
                AppConstants.STATUS_AVAILABLE, id, AppConstants.STATUS_BORROWED) == 1;
    }

    /** Deletes the book only if it is not borrowed. */
    public boolean deleteIfAvailable(int id) {
        return update("DELETE FROM books WHERE id = ? AND status = ?",
                id, AppConstants.STATUS_AVAILABLE) == 1;
    }
}
