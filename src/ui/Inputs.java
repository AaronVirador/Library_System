package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;

/** Text fields, password fields and dropdowns in the app's rounded style. */
public class Inputs {

    // ---------- Text and password fields (rounded, soft focus ring) ----------

    private static class RoundedTextField extends JTextField {
        RoundedTextField() {
            init(this);
        }
    }

    /**
     * Password field with a small eye button on the right.
     * Click the eye to show the password while typing, click again to hide it.
     */
    private static class RoundedPasswordField extends JPasswordField {
        private static final int EYE_AREA = 42;   // width of the clickable eye zone
        private final char hiddenEchoChar;
        private boolean revealed = false;
        private boolean overEye = false;

        RoundedPasswordField() {
            init(this);
            hiddenEchoChar = getEchoChar();
            setBorder(new EmptyBorder(9, 14, 9, EYE_AREA));   // keep text clear of the eye

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (isEnabled() && inEyeArea(e.getX())) {
                        setRevealed(!revealed);
                        requestFocusInWindow();
                        e.consume();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    updateHover(false);
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    updateHover(isEnabled() && inEyeArea(e.getX()));
                }
            });
            putClientProperty("passwordField.reveal", (Runnable) () -> setRevealed(false));
        }

        private boolean inEyeArea(int x) {
            return x >= getWidth() - EYE_AREA;
        }

        private void updateHover(boolean hover) {
            if (hover == overEye) return;
            overEye = hover;
            setCursor(Cursor.getPredefinedCursor(hover ? Cursor.HAND_CURSOR : Cursor.TEXT_CURSOR));
            setToolTipText(hover ? (revealed ? "Hide password" : "Show password") : null);
            repaint();
        }

        private void setRevealed(boolean show) {
            revealed = show;
            setEchoChar(show ? (char) 0 : hiddenEchoChar);   // 0 = show the real letters
            setToolTipText(overEye ? (show ? "Hide password" : "Show password") : null);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            paintEye((Graphics2D) g);
        }

        /** Open eye = "click to show". Eye with a slash = "click to hide". */
        private void paintEye(Graphics g) {
            Graphics2D g2 = Theme.smooth(g);
            Color color = !isEnabled() ? new Color(209, 213, 219)
                    : revealed ? Theme.PRIMARY
                    : overEye ? Theme.TEXT : Theme.MUTED;
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            float cx = getWidth() - EYE_AREA / 2f - 1, cy = getHeight() / 2f;
            java.awt.geom.Path2D outline = new java.awt.geom.Path2D.Float();   // almond shape
            outline.moveTo(cx - 9, cy);
            outline.quadTo(cx, cy - 9, cx + 9, cy);
            outline.quadTo(cx, cy + 9, cx - 9, cy);
            g2.draw(outline);
            g2.fill(new java.awt.geom.Ellipse2D.Float(cx - 2.6f, cy - 2.6f, 5.2f, 5.2f));   // pupil
            if (revealed) {
                g2.draw(new java.awt.geom.Line2D.Float(cx - 7, cy + 7, cx + 7, cy - 7));   // slash
            }
            g2.dispose();
        }
    }

    /** Hides the password again (used when the screen changes so a revealed password never stays visible). */
    public static void hidePassword(JPasswordField field) {
        Object reset = field.getClientProperty("passwordField.reveal");
        if (reset instanceof Runnable) ((Runnable) reset).run();
    }

    // Shared setup + painting for both field types
    private static void init(JTextField field) {
        field.setOpaque(false);
        field.setFont(Theme.font(Font.PLAIN, 14));
        field.setForeground(Theme.TEXT);
        field.setCaretColor(Theme.PRIMARY);
        field.setSelectionColor(Theme.PRIMARY_SOFT);
        field.setSelectedTextColor(Theme.TEXT);
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
        // Password fields need their own UI, otherwise Swing would show the letters instead of dots
        if (field instanceof JPasswordField) {
            field.setUI(new javax.swing.plaf.basic.BasicPasswordFieldUI() {
                @Override
                protected void paintSafely(Graphics g) {
                    paintFieldBackground(g, field);
                    super.paintSafely(g);
                    paintHint(g, field);
                }
            });
        } else {
            field.setUI(new javax.swing.plaf.basic.BasicTextFieldUI() {
                @Override
                protected void paintSafely(Graphics g) {
                    paintFieldBackground(g, field); // Swing skips this for non-opaque fields
                    super.paintSafely(g);
                    paintHint(g, field);
                }
            });
        }
    }

    /** Gray placeholder text, shown only while the field is empty. */
    private static void paintHint(Graphics g, JTextField field) {
        Object hint = field.getClientProperty("hint");
        if (hint == null || field.getDocument().getLength() > 0) return;
        Graphics2D g2 = Theme.smooth(g);
        g2.setFont(field.getFont());
        g2.setColor(new Color(156, 163, 175));
        Insets in = field.getInsets();
        FontMetrics fm = g2.getFontMetrics();
        int y = (field.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(hint.toString(), in.left, y);
        g2.dispose();
    }

    /** Adds gray placeholder text that disappears when typing. */
    public static <T extends JTextField> T withHint(T field, String hint) {
        field.putClientProperty("hint", hint);
        return field;
    }

    private static void paintFieldBackground(Graphics g, JTextField field) {
        Graphics2D g2 = Theme.smooth(g);
        boolean focused = Boolean.TRUE.equals(field.getClientProperty("focused"));
        int w = field.getWidth(), h = field.getHeight();
        if (focused) { // soft outer glow
            g2.setColor(new Color(Theme.PRIMARY.getRed(), Theme.PRIMARY.getGreen(), Theme.PRIMARY.getBlue(), 40));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));
        }
        g2.setColor(field.isEditable() && field.isEnabled() ? Color.WHITE : new Color(243, 244, 246));
        g2.fill(new RoundRectangle2D.Float(1.5f, 1.5f, w - 3, h - 3, 11, 11));
        g2.setColor(focused ? Theme.PRIMARY : Theme.BORDER);
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

    // ---------- Dropdown ----------

    /** Flat rounded dropdown with a small chevron. */
    public static JComboBox<String> comboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(Theme.font(Font.PLAIN, 13));
        combo.setForeground(Theme.TEXT);
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
                        Graphics2D g2 = Theme.smooth(g);
                        g2.setColor(Theme.MUTED);
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
                Graphics2D g2 = Theme.smooth(g);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(1.5f, 1.5f, c.getWidth() - 3, c.getHeight() - 3, 11, 11));
                super.paint(g, c);
                // border goes on top so nothing covers it
                g2.setColor(combo.hasFocus() || combo.isPopupVisible() ? Theme.PRIMARY : Theme.BORDER);
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
                label.setFont(Theme.font(Font.PLAIN, 13));
                label.setForeground(Theme.TEXT);
                label.setBackground(isSelected ? Theme.PRIMARY_SOFT : Color.WHITE);
                return label;
            }
        });
        return combo;
    }
}
