package ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Theme {

    public static final Color PRIMARY = new Color(79, 70, 229);
    public static final Color PRIMARY_HOVER = new Color(67, 56, 202);
    public static final Color ACCENT = new Color(147, 51, 234);
    public static final Color TEXT = new Color(17, 24, 39);
    public static final Color MUTED = new Color(107, 114, 128);
    public static final Color BORDER = new Color(209, 213, 219);
    public static final Color ERROR = new Color(220, 38, 38);
    public static final Color SUCCESS = new Color(22, 163, 74);

    public static final String FONT = "Segoe UI";

    // ---------- Labels ----------

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(FONT, Font.BOLD, 28));
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel subtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(FONT, Font.PLAIN, 14));
        label.setForeground(MUTED);
        return label;
    }

    public static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(FONT, Font.BOLD, 13));
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel messageLabel() {
        JLabel label = new JLabel(" "); // blank space keeps the height
        label.setFont(new Font(FONT, Font.PLAIN, 12));
        return label;
    }

    public static void showError(JLabel label, String message) {
        label.setForeground(ERROR);
        label.setText(message);
    }

    public static void showSuccess(JLabel label, String message) {
        label.setForeground(SUCCESS);
        label.setText(message);
    }

    // ---------- Fields ----------

    private static Border fieldBorder(Color color) {
        return BorderFactory.createCompoundBorder(
                new LineBorder(color, 1, true),
                new EmptyBorder(8, 12, 8, 12));
    }

    private static void styleField(JTextField field) {
        field.setFont(new Font(FONT, Font.PLAIN, 14));
        field.setBorder(fieldBorder(BORDER));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(fieldBorder(PRIMARY));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(fieldBorder(BORDER));
            }
        });
    }

    public static JTextField textField() {
        JTextField field = new JTextField();
        styleField(field);
        return field;
    }

    public static JPasswordField passwordField() {
        JPasswordField field = new JPasswordField();
        styleField(field);
        return field;
    }

    // ---------- Buttons ----------

    // Rounded purple button with a hover effect
    public static JButton primaryButton(String text) {
        JButton button = new JButton(text) {
            private boolean hover = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? PRIMARY_HOVER : PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(new Font(FONT, Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 42));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return button;
    }

    // Text-only button that looks like a link
    public static JButton linkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font(FONT, Font.BOLD, 13));
        button.setForeground(PRIMARY);
        button.setBorder(new EmptyBorder(0, 0, 0, 0));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }
}