package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/** A big clickable card for choosing a role (Student or Admin) on the first screen. */
public class RoleTile extends JButton {
    private boolean hover = false;
    private final String title, description;
    private final boolean admin;

    public RoleTile(String title, String description, boolean admin) {
        this.title = title;
        this.description = description;
        this.admin = admin;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(300, 88));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
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
        int w = getWidth(), h = getHeight();

        g2.setColor(hover ? Theme.PRIMARY_SOFT : Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 16, 16));
        g2.setColor(hover ? Theme.PRIMARY : Theme.BORDER);
        g2.setStroke(new BasicStroke(1.4f));
        g2.draw(new RoundRectangle2D.Float(0.7f, 0.7f, w - 1.4f, h - 1.4f, 16, 16));

        // icon
        int d = 52, x = 18, y = (h - d) / 2, cx = x + d / 2;
        g2.setColor(admin ? Theme.WARNING_BG : Theme.PRIMARY_SOFT);
        g2.fillOval(x, y, d, d);
        g2.setColor(admin ? Theme.WARNING_TEXT : Theme.PRIMARY);
        if (admin) { // shield with a check mark
            Path2D shield = new Path2D.Float();
            shield.moveTo(cx, y + 12);
            shield.lineTo(cx + 11, y + 16);
            shield.lineTo(cx + 11, y + 27);
            shield.quadTo(cx + 11, y + 36, cx, y + 41);
            shield.quadTo(cx - 11, y + 36, cx - 11, y + 27);
            shield.lineTo(cx - 11, y + 16);
            shield.closePath();
            g2.fill(shield);
            g2.setColor(Theme.WARNING_BG);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(cx - 5, y + 26, cx - 1, y + 30);
            g2.drawLine(cx - 1, y + 30, cx + 6, y + 22);
        } else { // person
            g2.fillOval(cx - 6, y + 12, 12, 12);
            g2.fillArc(cx - 12, y + 28, 24, 24, 0, 180);
        }

        // texts
        g2.setFont(Theme.font(Font.BOLD, 16));
        g2.setColor(Theme.TEXT);
        g2.drawString(title, 88, h / 2 - 3);
        g2.setFont(Theme.font(Font.PLAIN, 12));
        g2.setColor(Theme.MUTED);
        g2.drawString(description, 88, h / 2 + 16);

        // arrow
        g2.setColor(hover ? Theme.PRIMARY : Theme.MUTED);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(w - 30, h / 2 - 6, w - 24, h / 2);
        g2.drawLine(w - 24, h / 2, w - 30, h / 2 + 6);
        g2.dispose();
    }
}
