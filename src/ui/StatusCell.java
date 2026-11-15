package ui;

import constants.AppConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** Draws a book's status in the table as a small colored pill (green = Available, amber = Borrowed). */
public class StatusCell implements TableCellRenderer {

    private final JPanel panel = new JPanel(new GridBagLayout());
    private final Pill pill = new Pill();

    public StatusCell() {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0; // without this the pill floats to the middle of the cell
        c.insets = new Insets(0, 20, 0, 0);
        panel.add(pill, c);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int col) {
        boolean available = AppConstants.STATUS_AVAILABLE.equals(value);
        pill.set(String.valueOf(value),
                available ? Theme.PRIMARY_SOFT : Theme.WARNING_BG,
                available ? Theme.SUCCESS : Theme.WARNING_TEXT);
        panel.setBackground(isSelected ? Theme.PRIMARY_SOFT : Color.WHITE);
        return panel;
    }

    /** The rounded colored label itself. */
    private static class Pill extends JLabel {
        private Color background = Color.LIGHT_GRAY;

        Pill() {
            setFont(Theme.font(Font.BOLD, 12));
            setBorder(new EmptyBorder(4, 12, 4, 12));
            setOpaque(false);
        }

        void set(String text, Color bg, Color fg) {
            setText(text);
            background = bg;
            setForeground(fg);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Theme.smooth(g);
            g2.setColor(background);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
