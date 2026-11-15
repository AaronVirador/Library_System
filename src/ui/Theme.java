package ui;

import javax.swing.*;
import java.awt.*;

/**
 * The look of the whole app in one place: colors, fonts and text labels.
 * The colors come straight from the school logo: forest green ring, royal blue, sun yellow, sky blue.
 *
 * The other style files build on this one:
 *   Inputs  - text fields, password fields, dropdowns
 *   Buttons - buttons, links and filter chips
 *   Panels  - cards and the slim scroll bar
 *   Logo    - finding and drawing the school logo
 */
public class Theme {

    // ---------- Brand colors (from the logo) ----------
    public static final Color PRIMARY       = new Color(15, 107, 47);    // forest green
    public static final Color PRIMARY_HOVER = new Color(10, 84, 36);
    public static final Color PRIMARY_DARK  = new Color(8, 61, 27);      // deep green (brand panel)
    public static final Color PRIMARY_SOFT  = new Color(231, 245, 236);  // green tint
    public static final Color ACCENT        = new Color(31, 58, 158);    // royal blue (links, focus)
    public static final Color ACCENT_SOFT   = new Color(232, 240, 253);
    public static final Color GOLD          = new Color(246, 201, 14);   // sun yellow
    public static final Color SKY           = new Color(97, 201, 252);   // sky blue

    // ---------- Neutrals ----------
    public static final Color BACKGROUND = new Color(247, 248, 250);
    public static final Color SURFACE    = Color.WHITE;
    public static final Color TEXT       = new Color(17, 24, 39);
    public static final Color MUTED      = new Color(100, 108, 122);
    public static final Color BORDER     = new Color(224, 228, 234);
    public static final Color ROW_ALT    = new Color(250, 251, 252);

    // ---------- Status colors ----------
    public static final Color ERROR        = new Color(217, 45, 32);
    public static final Color SUCCESS      = new Color(22, 128, 61);
    public static final Color WARNING_TEXT = new Color(161, 88, 8);
    public static final Color WARNING_BG   = new Color(254, 243, 199);

    // ---------- Fonts ----------
    public static final String FONT = pickFont();

    private static String pickFont() {
        String[] wanted = {"Segoe UI", "Inter", "SF Pro Text", "Helvetica Neue", "Ubuntu", "Roboto"};
        java.util.Set<String> installed = new java.util.HashSet<>(java.util.Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String name : wanted) {
            if (installed.contains(name)) return name;
        }
        return Font.SANS_SERIF;
    }

    public static Font font(int style, int size) {
        return new Font(FONT, style, size);
    }

    /** Turns on smooth shapes + text for custom painting. */
    public static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    // ---------- Labels ----------

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.BOLD, 28));
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel subtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.PLAIN, 14));
        label.setForeground(MUTED);
        return label;
    }

    public static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.BOLD, 12));
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel messageLabel() {
        JLabel label = new JLabel(" "); // blank space keeps the height
        label.setFont(font(Font.PLAIN, 12));
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
}
