package util;

import constants.AppConstants;
import model.Book;
import model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {

    // Makes sure the file (and its folder) exists
    private static void ensureFileExists(String path) throws IOException {
        File file = new File(path);
        File folder = file.getParentFile();

        if (folder != null && !folder.exists()) {
            folder.mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    // ---------- BOOKS ----------

    public static List<Book> loadBooks() {
        List<Book> books = new ArrayList<>();

        try {
            ensureFileExists(AppConstants.BOOK_FILE);

            try (BufferedReader reader = new BufferedReader(new FileReader(AppConstants.BOOK_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    String[] parts = line.split(AppConstants.DELIMITER);
                    if (parts.length == 4) {
                        int id = Integer.parseInt(parts[0].trim());
                        books.add(new Book(id, parts[1].trim(), parts[2].trim(), parts[3].trim()));
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading books: " + e.getMessage());
        }

        return books;
    }

    public static void saveBooks(List<Book> books) {
        try {
            ensureFileExists(AppConstants.BOOK_FILE);

            try (PrintWriter writer = new PrintWriter(new FileWriter(AppConstants.BOOK_FILE))) {
                for (Book book : books) {
                    writer.println(book.toFileString());
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving books: " + e.getMessage());
        }
    }

    // ---------- USERS ----------

    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();

        try {
            ensureFileExists(AppConstants.USER_FILE);

            try (BufferedReader reader = new BufferedReader(new FileReader(AppConstants.USER_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    String[] parts = line.split(AppConstants.DELIMITER);
                    if (parts.length == 4) {
                        users.add(new User(parts[0], parts[1], parts[2], parts[3]));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }

        return users;
    }

    public static void saveUsers(List<User> users) {
        try {
            ensureFileExists(AppConstants.USER_FILE);

            try (PrintWriter writer = new PrintWriter(new FileWriter(AppConstants.USER_FILE))) {
                for (User user : users) {
                    writer.println(user.toFileString());
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }
}