package constants;

public class AppConstants {

    // User accounts
    public static final String USER_FILE = "data/Users.txt";
    public static final String[] SECURITY_QUESTIONS = {
        "What is your favorite color?",
        "What is the name of your first pet?",
        "What city were you born in?"
    };

    // Text file storage
    public static final String BOOK_FILE = "data/Book.txt";
    public static final String DELIMITER = ",";

    // Book status
    public static final String STATUS_AVAILABLE = "Available";
    public static final String STATUS_BORROWED = "Borrowed";

    // Menu options
    public static final int MENU_ADD_BOOK = 1;
    public static final int MENU_VIEW_BOOKS = 2;
    public static final int MENU_SEARCH_BOOK = 3;
    public static final int MENU_BORROW_BOOK = 4;
    public static final int MENU_RETURN_BOOK = 5;
    public static final int MENU_EXIT = 0;

    private AppConstants() {} // prevents creating an object of this class
}