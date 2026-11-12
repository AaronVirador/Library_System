package ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * Central place for the look of the whole app.
 * Colors come straight from the school logo:
 * forest green ring, royal blue, sun yellow and sky blue.
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

    // ---------- Text fields (rounded, soft focus ring) ----------

    private static class RoundedTextField extends JTextField {
        RoundedTextField() {
            init(this);
        }
    }

    private static class RoundedPasswordField extends JPasswordField {
        RoundedPasswordField() {
            init(this);
        }
    }

    // Shared setup + painting for both field types
    private static void init(JTextField field) {
        field.setOpaque(false);
        field.setFont(font(Font.PLAIN, 14));
        field.setForeground(TEXT);
        field.setCaretColor(PRIMARY);
        field.setSelectionColor(PRIMARY_SOFT);
        field.setSelectedTextColor(TEXT);
        field.setBorder(new EmptyBorder(9, 14, 9, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setPreferredSize(new Dimension(200, 42));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.putClientProperty("focused", Boolean.TRUE);
                field.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.putClientProperty("focused", Boolean.FALSE);
                field.repaint();
            }
        });
        field.setUI(new javax.swing.plaf.basic.BasicTextFieldUI() {
            @Override
            protected void paintSafely(Graphics g) {
                paintFieldBackground(g, field); // Swing skips this for non-opaque fields
                super.paintSafely(g);
                // placeholder text
                Object hint = field.getClientProperty("hint");
                if (hint != null && field.getDocument().getLength() == 0) {
                    Graphics2D g2 = smooth(g);
                    g2.setFont(field.getFont());
                    g2.setColor(new Color(156, 163, 175));
                    Insets in = field.getInsets();
                    FontMetrics fm = g2.getFontMetrics();
                    int y = (field.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(hint.toString(), in.left, y);
                    g2.dispose();
                }
            }
        });
    }

    /** Adds gray placeholder text that disappears when typing. */
    public static <T extends JTextField> T withHint(T field, String hint) {
        field.putClientProperty("hint", hint);
        return field;
    }

    private static void paintFieldBackground(Graphics g, JTextField field) {
        Graphics2D g2 = smooth(g);
        boolean focused = Boolean.TRUE.equals(field.getClientProperty("focused"));
        int w = field.getWidth(), h = field.getHeight();
        if (focused) { // soft outer glow
            g2.setColor(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 40));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));
        }
        g2.setColor(field.isEditable() && field.isEnabled() ? Color.WHITE : new Color(243, 244, 246));
        g2.fill(new RoundRectangle2D.Float(1.5f, 1.5f, w - 3, h - 3, 11, 11));
        g2.setColor(focused ? PRIMARY : BORDER);
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Float(1.5f, 1.5f, w - 3, h - 3, 11, 11));
        g2.dispose();
    }

    public static JTextField textField() {
        return new RoundedTextField();
    }

    public static JPasswordField passwordField() {
        return new RoundedPasswordField();
    }

    // ---------- Buttons ----------

    /** Shared base: a rounded button with hover + pressed colors. */
    private static JButton roundedButton(String text, Color fill, Color fillHover,
                                         Color textColor, Color outline) {
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
                Graphics2D g2 = smooth(g);
                Color base = hover ? fillHover : fill;
                if (getModel().isPressed()) base = base.darker();
                if (!isEnabled()) base = new Color(229, 231, 235);
                g2.setColor(base);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                if (outline != null && isEnabled()) {
                    g2.setColor(outline);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, getWidth() - 1.2f, getHeight() - 1.2f, 12, 12));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(font(Font.BOLD, 13));
        button.setForeground(textColor);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(110, 42));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return button;
    }

    /** Solid green main-action button. */
    public static JButton primaryButton(String text) {
        return roundedButton(text, PRIMARY, PRIMARY_HOVER, Color.WHITE, null);
    }

    /** White button with a soft border, for secondary actions. */
    public static JButton outlineButton(String text) {
        return roundedButton(text, Color.WHITE, PRIMARY_SOFT, PRIMARY, BORDER);
    }

    /** Subtle button for use on the dark green panel. */
    public static JButton ghostButton(String text) {
        return roundedButton(text, new Color(255, 255, 255, 0), new Color(255, 255, 255, 30),
                Color.WHITE, new Color(255, 255, 255, 110));
    }

    /** White button with red text, for risky actions such as delete. */
    public static JButton dangerButton(String text) {
        return roundedButton(text, Color.WHITE, new Color(254, 242, 242), ERROR, new Color(252, 165, 165));
    }

    /** Solid red button for confirming a risky action. */
    public static JButton dangerSolidButton(String text) {
        return roundedButton(text, ERROR, new Color(185, 28, 28), Color.WHITE, null);
    }

    /** Small pill-shaped toggle used for filters (use inside a ButtonGroup). */
    public static JToggleButton chip(String text) {
        JToggleButton button = new JToggleButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = smooth(g);
                boolean on = isSelected();
                int w = getWidth(), h = getHeight();
                g2.setColor(on ? PRIMARY : (getModel().isRollover() ? PRIMARY_SOFT : Color.WHITE));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
                if (!on) {
                    g2.setColor(BORDER);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, h, h));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(font(Font.BOLD, 12));
        button.setForeground(MUTED);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(0, 16, 0, 16));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width, 34));
        button.addItemListener(e -> button.setForeground(button.isSelected() ? Color.WHITE : MUTED));
        return button;
    }

    /** Text-only button that looks like a link. */
    public static JButton linkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(font(Font.BOLD, 13));
        button.setForeground(ACCENT);
        button.setBorder(new EmptyBorder(0, 0, 0, 0));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    // ---------- Dropdown ----------

    /** Flat rounded dropdown with a small chevron. */
    public static JComboBox<String> comboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(font(Font.PLAIN, 13));
        combo.setForeground(TEXT);
        combo.setBackground(Color.WHITE);
        combo.setOpaque(false);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        combo.setPreferredSize(new Dimension(200, 42));
        combo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrow = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = smooth(g);
                        g2.setColor(MUTED);
                        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        int cx = getWidth() / 2, cy = getHeight() / 2;
                        g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
                        g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
                        g2.dispose();
                    }
                };
                arrow.setBorder(new EmptyBorder(0, 0, 0, 0));
                arrow.setOpaque(false);
                arrow.setContentAreaFilled(false);
                arrow.setFocusPainted(false);
                return arrow;
            }

            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = smooth(g);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(1.5f, 1.5f, c.getWidth() - 3, c.getHeight() - 3, 11, 11));
                super.paint(g, c);
                // border goes on top so nothing covers it
                g2.setColor(combo.hasFocus() || combo.isPopupVisible() ? PRIMARY : BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(1.5f, 1.5f, c.getWidth() - 3, c.getHeight() - 3, 11, 11));
                g2.dispose();
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                // background is painted in paint() so the rounded corners stay clean
            }
        });
        combo.setBorder(new EmptyBorder(0, 10, 0, 4));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, false);
                label.setBorder(new EmptyBorder(8, 12, 8, 12));
                label.setFont(font(Font.PLAIN, 13));
                label.setForeground(TEXT);
                label.setBackground(isSelected ? PRIMARY_SOFT : Color.WHITE);
                return label;
            }
        });
        return combo;
    }

    // ---------- Scroll bar ----------

    /** Thin, rounded scroll bar without arrow buttons. */
    public static void styleScrollPane(JScrollPane scroll) {
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                trackColor = Color.WHITE;
            }

            @Override
            protected JButton createDecreaseButton(int o) {
                return zeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int o) {
                return zeroButton();
            }

            private JButton zeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                g.setColor(Color.WHITE);
                g.fillRect(r.x, r.y, r.width, r.height);
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                if (r.isEmpty()) return;
                Graphics2D g2 = smooth(g);
                g2.setColor(new Color(203, 208, 216));
                g2.fill(new RoundRectangle2D.Float(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8));
                g2.dispose();
            }
        });
    }

    // ---------- Cards ----------

    /** White rounded panel with a thin border (use as a "card"). */
    public static JPanel card(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = smooth(g);
                g2.setColor(SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    // ---------- Logo ----------

    private static BufferedImage logoImage;
    private static boolean logoTried = false;

    private static BufferedImage loadLogo() {
        if (logoTried) return logoImage;
        logoTried = true;
        String[] names = {"logo.jpg", "logo.png"};
        try {
            for (String name : names) {
                InputStream in = Theme.class.getResourceAsStream("/" + name);
                if (in != null) {
                    logoImage = ImageIO.read(in);
                    return logoImage;
                }
                for (String dir : new String[]{"resources/", "src/", "", "../resources/"}) {
                    File f = new File(dir + name);
                    if (f.exists()) {
                        logoImage = ImageIO.read(f);
                        return logoImage;
                    }
                }
            }
        } catch (Exception ignored) {
            // falls back to the drawn placeholder below
        }
        return null;
    }

    /**
     * Shrinks an image in gentle steps (halving, then a final resize).
     * Doing it in one big jump is what makes logos look pixelated.
     */
    private static BufferedImage scaleSmooth(BufferedImage src, int targetWidth) {
        int targetHeight = Math.max(1, src.getHeight() * targetWidth / src.getWidth());
        BufferedImage img = src;
        int w = src.getWidth(), h = src.getHeight();

        while (w / 2 >= targetWidth) { // halve until we are close to the target size
            w /= 2;
            h /= 2;
            img = resize(img, w, h, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
        if (w != targetWidth) {
            img = resize(img, targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        }
        return img;
    }

    /** Cuts the image into a circle with a smooth (anti-aliased) edge. */
    private static BufferedImage circleMask(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new java.awt.geom.Ellipse2D.Float(0, 0, w, h));
        g2.setComposite(AlphaComposite.SrcIn);
        g2.drawImage(src, 0, 0, null);
        g2.dispose();
        return out;
    }

    private static BufferedImage resize(BufferedImage src, int w, int h, Object interpolation) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(src, 0, 0, w, h, null);
        g2.dispose();
        return out;
    }

    /** The school logo drawn as a smooth circle at any size. */
    public static JComponent logo(int size) {
        JComponent c = new JComponent() {
            private BufferedImage cached;
            private int cachedWidth = -1;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = smooth(g);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                BufferedImage img = loadLogo();
                if (img != null) {
                    // render at the real screen resolution (sharper on high-DPI screens)
                    double scale = Math.max(1.0, g2.getTransform().getScaleX());
                    int pixels = (int) Math.round(getWidth() * scale);
                    if (cached == null || cachedWidth != pixels) {
                        cached = circleMask(scaleSmooth(img, pixels));
                        cachedWidth = pixels;
                    }
                    g2.drawImage(cached, 0, 0, getWidth(), getHeight(), null);
                } else { // simple placeholder if the image file is missing
                    g2.setColor(PRIMARY);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.setColor(GOLD);
                    g2.fillOval(getWidth() / 4, getHeight() / 4, getWidth() / 2, getHeight() / 2);
                }
                g2.dispose();
            }
        };
        Dimension d = new Dimension(size, size);
        c.setPreferredSize(d);
        c.setMinimumSize(d);
        c.setMaximumSize(d);
        return c;
    }
}