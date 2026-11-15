package ui;

import dao.DataAccessException;
import util.DBConnection;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        // smoother text rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // If MySQL stops while the program is open, show a message instead of failing silently
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            if (error instanceof DataAccessException) {
                JOptionPane.showMessageDialog(null, DBConnection.explain(error),
                        "Database problem", JOptionPane.ERROR_MESSAGE);
            } else {
                error.printStackTrace();
            }
        });

        SwingUtilities.invokeLater(() -> {
            // Check the database before showing the login screen
            try (Connection ignored = DBConnection.get()) {
                new LoginFrame().setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, DBConnection.explain(e),
                        "Can't connect to MySQL", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
