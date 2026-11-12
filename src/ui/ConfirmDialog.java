package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;

/** A small "Are you sure?" window in the same style as the rest of the app. */
public class ConfirmDialog extends JDialog {

    private boolean confirmed = false;

    private ConfirmDialog(Window owner, String title, String message, String confirmText) {
        super(owner, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);

        JPanel content = new JPanel(new BorderLayout(18, 0));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1),
                new EmptyBorder(24, 24, 20, 24)));

        // red warning circle
        JComponent icon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Theme.smooth(g);
                g2.setColor(new Color(254, 226, 226));
                g2.fill(new Ellipse2D.Float(0, 0, getWidth(), getHeight()));
                g2.setColor(Theme.ERROR);
                g2.setFont(Theme.font(Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                String mark = "!";
                g2.drawString(mark, (getWidth() - fm.stringWidth(mark)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        icon.setPreferredSize(new Dimension(44, 44));
        JPanel iconWrap = new JPanel(new BorderLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(icon, BorderLayout.NORTH);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.font(Font.BOLD, 17));
        titleLabel.setForeground(Theme.TEXT);

        JLabel messageLabel = new JLabel("<html><div style='width:280px'>" + escape(message) + "</div></html>");
        messageLabel.setFont(Theme.font(Font.PLAIN, 13));
        messageLabel.setForeground(Theme.MUTED);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        text.add(titleLabel);
        text.add(Box.createVerticalStrut(6));
        text.add(messageLabel);

        JButton cancel = Theme.outlineButton("Cancel");
        cancel.setPreferredSize(new Dimension(100, 40));
        cancel.addActionListener(e -> dispose());

        JButton confirm = Theme.dangerSolidButton(confirmText);
        confirm.setPreferredSize(new Dimension(110, 40));
        confirm.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(20, 0, 0, 0));
        buttons.add(cancel);
        buttons.add(confirm);

        content.add(iconWrap, BorderLayout.WEST);
        content.add(text, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        setContentPane(content);

        // Esc = cancel
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setLocationRelativeTo(owner);
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Shows the window and returns true if the user pressed the confirm button. */
    public static boolean ask(Component parent, String title, String message, String confirmText) {
        ConfirmDialog dialog = new ConfirmDialog(SwingUtilities.getWindowAncestor(parent),
                title, message, confirmText);
        dialog.setVisible(true); // waits here until closed
        return dialog.confirmed;
    }
}