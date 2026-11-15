package util;

import dao.BookDAO;
import dao.UserDAO;
import model.Book;
import model.User;

/**
 * One-time tool: copies the old data/Users.txt and data/Book.txt into MySQL.
 * It is safe to run twice: users and books that are already in MySQL are skipped.
 *
 * Run it from the project root:
 *     java -cp "bin:lib/*" util.ImportFromFiles
 */
public class ImportFromFiles {

    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        BookDAO bookDAO = new BookDAO();

        int usersAdded = 0, usersSkipped = 0, booksAdded = 0, booksSkipped = 0;

        try {
            // users first, because a borrowed book points to a user
            for (User user : FileHandler.loadUsers()) {
                if (userDAO.findByUsername(user.getUsername()) != null) {
                    usersSkipped++;
                } else if (userDAO.insert(user)) {
                    usersAdded++;
                } else {
                    usersSkipped++;
                }
            }

            for (Book book : FileHandler.loadBooks()) {
                if (bookDAO.findById(book.getId()) != null) {
                    booksSkipped++;
                    continue;
                }

                String borrower = book.getBorrowedBy();
                if (!borrower.isEmpty() && userDAO.findByUsername(borrower) == null) {
                    System.out.println("Note: \"" + book.getTitle() + "\" was borrowed by \"" + borrower
                            + "\", who has no account. The borrower name is left empty.");
                    borrower = "";
                }

                bookDAO.insertWithId(new Book(book.getId(), book.getTitle(), book.getAuthor(),
                        book.getStatus(), borrower));
                booksAdded++;
            }
        } catch (RuntimeException e) {
            System.out.println("\nImport stopped: " + DBConnection.explain(e));
            return;
        }

        System.out.println("Users: " + usersAdded + " imported, " + usersSkipped + " skipped (already in MySQL).");
        System.out.println("Books: " + booksAdded + " imported, " + booksSkipped + " skipped (already in MySQL).");
        System.out.println("Done. You can keep data/*.txt as a backup; the program no longer uses them.");
    }
}
