package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** A small dark message pill with a colored dot. It replaces pop-up dialogs for quick feedback. */
public class Toast extends JComponent {
    private String text = "";
    private Color dot = Theme.MUTED;

    Toast() {
        setFont(Theme.font(Font.PLAIN, 13));
        setVisible(false);
    }

    void set(String message, Color dotColor) {
        text = message;
        dot = dotColor;
        FontMetrics fm = getFontMetrics(getFont());
        setPreferredSize(new Dimension(fm.stringWidth(message) + 52, 38));
        setVisible(true);
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Theme.smooth(g);
        g2.setColor(new Color(31, 41, 55));
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
        g2.setColor(dot);
        g2.fillOval(16, getHeight() / 2 - 4, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, 32, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
        g2.dispose();
    }
}
