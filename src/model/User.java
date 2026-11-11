package model;

import constants.AppConstants;

public class User {
    private String username;
    private String passwordHash;
    private String question;
    private String answerHash;

    public User(String username, String passwordHash, String question, String answerHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.question = question;
        this.answerHash = answerHash;
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getQuestion() { return question; }
    public String getAnswerHash() { return answerHash; }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String toFileString() {
        return username + AppConstants.DELIMITER + passwordHash + AppConstants.DELIMITER
                + question + AppConstants.DELIMITER + answerHash;
    }
}