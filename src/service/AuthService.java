package service;

import constants.AppConstants;
import model.User;
import util.FileHandler;
import util.PasswordUtil;

import java.util.List;

public class AuthService {

    private List<User> users;

    public AuthService() {
        users = FileHandler.loadUsers();
    }

    private User findUser(String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    // Security answers are not case-sensitive
    private String hashAnswer(String answer) {
        return PasswordUtil.hash(answer.trim().toLowerCase());
    }

    // Returns null if OK, otherwise an error message
    public String signUp(String username, String password, String confirm,
                         String question, String answer) {
        if (username.length() < 3) return "Username must be at least 3 characters.";
        if (username.contains(AppConstants.DELIMITER) || username.contains(" "))
            return "Username can't contain spaces or commas.";
        if (username.equalsIgnoreCase(AppConstants.ADMIN_USERNAME)) return "That username is reserved.";
        if (findUser(username) != null) return "That username is already taken.";
        if (password.length() < 6) return "Password must be at least 6 characters.";
        if (!password.equals(confirm)) return "Passwords do not match.";
        if (answer.trim().isEmpty()) return "Please answer the security question.";

        users.add(new User(username, PasswordUtil.hash(password), question, hashAnswer(answer)));
        FileHandler.saveUsers(users);
        return null;
    }

    // The username exactly as it was saved at sign-up (so "MARIA" and "maria" count as the same student)
    public String getStoredUsername(String username) {
        User user = findUser(username);
        return user == null ? username : user.getUsername();
    }

    // The admin has one fixed account; only the password is typed
    public boolean adminLogin(String password) {
        return PasswordUtil.hash(password).equals(AppConstants.ADMIN_PASSWORD_HASH);
    }

    public boolean login(String username, String password) {
        User user = findUser(username);
        return user != null && user.getPasswordHash().equals(PasswordUtil.hash(password));
    }

    // Returns the user's security question, or null if the account doesn't exist
    public String getSecurityQuestion(String username) {
        User user = findUser(username);
        return user == null ? null : user.getQuestion();
    }

    // Returns null if OK, otherwise an error message
    public String resetPassword(String username, String answer, String newPassword, String confirm) {
        User user = findUser(username);

        if (user == null) return "Account not found.";
        if (!user.getAnswerHash().equals(hashAnswer(answer)))
            return "Wrong answer to the security question.";
        if (newPassword.length() < 6) return "Password must be at least 6 characters.";
        if (!newPassword.equals(confirm)) return "Passwords do not match.";

        user.setPasswordHash(PasswordUtil.hash(newPassword));
        FileHandler.saveUsers(users);
        return null;
    }
}