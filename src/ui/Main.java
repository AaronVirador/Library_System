package ui;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // smoother text rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}