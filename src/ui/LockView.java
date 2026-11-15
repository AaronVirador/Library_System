package ui;

import constants.AppConstants;
import util.LoginGuard;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Supplier;

/** Connects one login form to the lock: shows the countdown and disables the inputs. */
public class LockView {
    public final LockPanel panel = new LockPanel();
    private final JButton button;
    private final String buttonText;
    private final Supplier<String> key; // which account is being logged in
    private final JComponent[] inputs;
    private boolean wasLocked = false;

    public LockView(JButton button, Supplier<String> key, JComponent... inputs) {
        this.button = button;
        this.buttonText = button.getText();
        this.key = key;
        this.inputs = inputs;
    }

    public void refresh() {
        long left = LoginGuard.millisLeft(key.get());
        boolean locked = left > 0;

        if (locked) panel.set(left);
        panel.setVisible(locked);
        button.setEnabled(!locked);
        button.setText(locked ? "Locked" : buttonText);
        for (JComponent input : inputs) input.setEnabled(!locked);

        if (wasLocked && !locked) inputs[0].requestFocusInWindow(); // ready to type again
        wasLocked = locked;
    }
}

/** The red box that counts down while the account is locked. */
class LockPanel extends JPanel {
    private long millisLeft = 0;

    LockPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(300, 68));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        setVisible(false);
    }

    void set(long millisLeft) {
        this.millisLeft = millisLeft;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Theme.smooth(g);
        int w = getWidth(), h = getHeight();

        g2.setColor(new Color(254, 242, 242));
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));
        g2.setColor(new Color(252, 165, 165));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 14, 14));

        g2.setFont(Theme.font(Font.BOLD, 13));
        g2.setColor(new Color(153, 27, 27));
        g2.drawString("Too many failed attempts", 16, 25);
        g2.setFont(Theme.font(Font.PLAIN, 12));
        g2.setColor(new Color(185, 28, 28));
        g2.drawString("Please wait before trying again.", 16, 43);

        long seconds = (millisLeft + 999) / 1000; // round up so it never shows 0s while locked
        String time = seconds + "s";
        g2.setFont(Theme.font(Font.BOLD, 24));
        g2.setColor(new Color(185, 28, 28));
        g2.drawString(time, w - 16 - g2.getFontMetrics().stringWidth(time), 36);

        // draining bar
        int barX = 16, barW = w - 32, barY = h - 12;
        g2.setColor(new Color(254, 202, 202));
        g2.fill(new RoundRectangle2D.Float(barX, barY, barW, 4, 4, 4));
        float ratio = Math.min(1f, millisLeft / (AppConstants.LOCKOUT_SECONDS * 1000f));
        g2.setColor(new Color(220, 38, 38));
        g2.fill(new RoundRectangle2D.Float(barX, barY, barW * ratio, 4, 4, 4));
        g2.dispose();
    }
}
