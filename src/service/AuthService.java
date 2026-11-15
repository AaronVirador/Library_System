package service;

import constants.AppConstants;
import dao.UserDAO;
import model.User;
import util.PasswordUtil;

/** Sign up, log in and password reset. Students are stored in MySQL (see UserDAO). */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    // Security answers are not case-sensitive
    private String hashAnswer(String answer) {
        return PasswordUtil.hash(answer.trim().toLowerCase());
    }

    // Returns null if OK, otherwise an error message
    public String signUp(String username, String password, String confirm,
                         String question, String answer) {
        if (username.length() < 3) return "Username must be at least 3 characters.";
        if (username.length() > 50) return "Username must be 50 characters or less.";
        if (username.contains(AppConstants.DELIMITER) || username.contains(" "))
            return "Username can't contain spaces or commas.";
        if (username.equalsIgnoreCase(AppConstants.ADMIN_USERNAME)) return "That username is reserved.";
        if (userDAO.findByUsername(username) != null) return "That username is already taken.";
        if (password.length() < 6) return "Password must be at least 6 characters.";
        if (!password.equals(confirm)) return "Passwords do not match.";
        if (answer.trim().isEmpty()) return "Please answer the security question.";

        User user = new User(username, PasswordUtil.hash(password), question, hashAnswer(answer));
        if (!userDAO.insert(user)) return "That username is already taken."; // someone else just took it
        return null;
    }

    // The username exactly as it was saved at sign-up (so "MARIA" and "maria" count as the same student)
    public String getStoredUsername(String username) {
        User user = userDAO.findByUsername(username);
        return user == null ? username : user.getUsername();
    }

    // The admin has one fixed account; only the password is typed
    public boolean adminLogin(String password) {
        return PasswordUtil.hash(password).equals(AppConstants.ADMIN_PASSWORD_HASH);
    }

    public boolean login(String username, String password) {
        User user = userDAO.findByUsername(username);
        return user != null && user.getPasswordHash().equals(PasswordUtil.hash(password));
    }

    // Returns the user's security question, or null if the account doesn't exist
    public String getSecurityQuestion(String username) {
        User user = userDAO.findByUsername(username);
        return user == null ? null : user.getQuestion();
    }

    // Returns null if OK, otherwise an error message
    public String resetPassword(String username, String answer, String newPassword, String confirm) {
        User user = userDAO.findByUsername(username);

        if (user == null) return "Account not found.";
        if (!user.getAnswerHash().equals(hashAnswer(answer)))
            return "Wrong answer to the security question.";
        if (newPassword.length() < 6) return "Password must be at least 6 characters.";
        if (!newPassword.equals(confirm)) return "Passwords do not match.";

        userDAO.updatePassword(user.getUsername(), PasswordUtil.hash(newPassword));
        return null;
    }
}
