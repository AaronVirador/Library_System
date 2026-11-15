package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/** Every kind of button in the app: main action, secondary, danger, link and filter chip. */
public class Buttons {

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
                Graphics2D g2 = Theme.smooth(g);
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

        button.setFont(Theme.font(Font.BOLD, 13));
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
        return roundedButton(text, Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null);
    }

    /** White button with a soft border, for secondary actions. */
    public static JButton outlineButton(String text) {
        return roundedButton(text, Color.WHITE, Theme.PRIMARY_SOFT, Theme.PRIMARY, Theme.BORDER);
    }

    /** Subtle button for use on the dark green panel. */
    public static JButton ghostButton(String text) {
        return roundedButton(text, new Color(255, 255, 255, 0), new Color(255, 255, 255, 30),
                Color.WHITE, new Color(255, 255, 255, 110));
    }

    /** White button with red text, for risky actions such as delete. */
    public static JButton dangerButton(String text) {
        return roundedButton(text, Color.WHITE, new Color(254, 242, 242), Theme.ERROR, new Color(252, 165, 165));
    }

    /** Solid red button for confirming a risky action. */
    public static JButton dangerSolidButton(String text) {
        return roundedButton(text, Theme.ERROR, new Color(185, 28, 28), Color.WHITE, null);
    }

    /** Small pill-shaped toggle used for filters (use inside a ButtonGroup). */
    public static JToggleButton chip(String text) {
        JToggleButton button = new JToggleButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Theme.smooth(g);
                boolean on = isSelected();
                int w = getWidth(), h = getHeight();
                g2.setColor(on ? Theme.PRIMARY : (getModel().isRollover() ? Theme.PRIMARY_SOFT : Color.WHITE));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
                if (!on) {
                    g2.setColor(Theme.BORDER);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, h, h));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(Theme.font(Font.BOLD, 12));
        button.setForeground(Theme.MUTED);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(0, 16, 0, 16));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width, 34));
        button.addItemListener(e -> button.setForeground(button.isSelected() ? Color.WHITE : Theme.MUTED));
        return button;
    }

    /** Text-only button that looks like a link. */
    public static JButton linkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(Theme.font(Font.BOLD, 13));
        button.setForeground(Theme.ACCENT);
        button.setBorder(new EmptyBorder(0, 0, 0, 0));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }
}
