package dao;

import model.User;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** All the SQL for the users table. */
public class UserDAO {

    private static final int DUPLICATE_ENTRY = 1062; // MySQL error code for "already exists"

    /** Returns the user (not case-sensitive), or null if there is no such username. */
    public User findByUsername(String username) {
        String sql = "SELECT username, password_hash, security_question, answer_hash FROM users WHERE username = ?";
        try (Connection con = DBConnection.get(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new User(rs.getString("username"), rs.getString("password_hash"),
                        rs.getString("security_question"), rs.getString("answer_hash"));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read the user.", e);
        }
    }

    /** Saves a new user. Returns false if the username is already taken. */
    public boolean insert(User user) {
        String sql = "INSERT INTO users (username, password_hash, security_question, answer_hash) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.get(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getQuestion());
            ps.setString(4, user.getAnswerHash());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() == DUPLICATE_ENTRY) return false;
            throw new DataAccessException("Could not save the user.", e);
        }
    }

    public void updatePassword(String username, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE username = ?";
        try (Connection con = DBConnection.get(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not change the password.", e);
        }
    }
}
