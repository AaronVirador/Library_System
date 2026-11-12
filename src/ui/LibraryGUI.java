package ui;

import model.Book;
import service.LibraryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class LibraryGUI extends JFrame {

    private static final Color ROW_LINE = new Color(240, 242, 245);

    private LibraryService service = new LibraryService();

    private JTextField titleField = Theme.withHint(Theme.textField(), "Book title");
    private JTextField authorField = Theme.withHint(Theme.textField(), "Author name");
    private JTextField searchField = Theme.withHint(Theme.textField(), "Search by title or author...");

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel countLabel = new JLabel(" ");

    // Summary numbers at the top
    private JLabel totalValue = statValue();
    private JLabel availableValue = statValue();
    private JLabel borrowedValue = statValue();

    // Small message that appears next to the buttons
    private Toast toast = new Toast();
    private Timer toastTimer;

    public LibraryGUI() {
        setTitle("Library System");
        setSize(1060, 800);
        setMinimumSize(new Dimension(920, 680));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);

        refreshTable(service.getAllBooks());
    }

    // ---------- HEADER ----------

    private JPanel createHeader() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(14, 28, 14, 28));

        JLabel title = new JLabel("Library System");
        title.setFont(Theme.font(Font.BOLD, 20));
        title.setForeground(Theme.TEXT);

        JLabel subtitle = new JLabel("Dasmariñas East Integrated High School");
        subtitle.setFont(Theme.font(Font.PLAIN, 12));
        subtitle.setForeground(Theme.MUTED);

        JPanel names = new JPanel();
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        names.setOpaque(false);
        names.add(title);
        names.add(subtitle);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);
        left.add(Theme.logo(46));
        left.add(names);

        JButton logout = Theme.outlineButton("Log Out");
        logout.setPreferredSize(new Dimension(100, 38));
        logout.addActionListener(e -> logout());

        JPanel right = new JPanel(new GridBagLayout()); // centers the button vertically
        right.setOpaque(false);
        right.add(logout);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        // thin strip in the logo colors
        JPanel strip = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                int w = getWidth(), h = getHeight();
                g.setColor(Theme.PRIMARY);
                g.fillRect(0, 0, w / 2, h);
                g.setColor(Theme.GOLD);
                g.fillRect(w / 2, 0, w / 4, h);
                g.setColor(Theme.ACCENT);
                g.fillRect(w / 2 + w / 4, 0, w - (w / 2 + w / 4), h);
            }
        };
        strip.setPreferredSize(new Dimension(0, 3));

        wrapper.add(header, BorderLayout.CENTER);
        wrapper.add(strip, BorderLayout.SOUTH);
        return wrapper;
    }

    // ---------- BODY ----------

    private JPanel createBody() {
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(Theme.BACKGROUND);
        body.setBorder(new EmptyBorder(22, 28, 22, 28));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        JComponent stats = createStats();
        JComponent add = createAddCard();
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        add.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(stats);
        top.add(Box.createVerticalStrut(16));
        top.add(add);

        body.add(top, BorderLayout.NORTH);
        body.add(createTableCard(), BorderLayout.CENTER);
        body.add(createBottomPanel(), BorderLayout.SOUTH);
        return body;
    }

    // Three summary cards: total / available / borrowed
    private JComponent createStats() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        row.setPreferredSize(new Dimension(0, 84));
        row.add(statCard("Total books", totalValue, Theme.ACCENT, Theme.ACCENT_SOFT));
        row.add(statCard("Available", availableValue, Theme.SUCCESS, Theme.PRIMARY_SOFT));
        row.add(statCard("Borrowed", borrowedValue, Theme.WARNING_TEXT, Theme.WARNING_BG));
        return row;
    }

    private static JLabel statValue() {
        JLabel label = new JLabel("0");
        label.setFont(Theme.font(Font.BOLD, 26));
        label.setForeground(Theme.TEXT);
        return label;
    }

    private JPanel statCard(String name, JLabel value, Color dot, Color dotBg) {
        JPanel card = Theme.card(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        // colored badge on the left
        JComponent badge = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Theme.smooth(g);
                g2.setColor(dotBg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(dot);
                int d = 12;
                g2.fillOval((getWidth() - d) / 2, (getHeight() - d) / 2, d, d);
                g2.dispose();
            }
        };
        badge.setPreferredSize(new Dimension(44, 44));

        JLabel label = new JLabel(name);
        label.setFont(Theme.font(Font.PLAIN, 12));
        label.setForeground(Theme.MUTED);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        text.add(label);
        text.add(value);

        JPanel badgeWrap = new JPanel(new GridBagLayout());
        badgeWrap.setOpaque(false);
        badgeWrap.add(badge);

        card.add(badgeWrap, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    // Card with the "add book" form
    private JComponent createAddCard() {
        JPanel card = Theme.card(new GridBagLayout());
        card.setBorder(new EmptyBorder(16, 20, 18, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JButton addButton = Theme.primaryButton("+  Add Book");
        addButton.setPreferredSize(new Dimension(130, 42));
        addButton.addActionListener(e -> addBook());
        authorField.addActionListener(e -> addButton.doClick()); // Enter key

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.insets = new Insets(0, 0, 6, 12);
        card.add(Theme.fieldLabel("Title"), c);
        c.gridx = 1; c.insets = new Insets(0, 0, 6, 12);
        card.add(Theme.fieldLabel("Author"), c);

        c.gridx = 0; c.gridy = 1; c.weightx = 1; c.insets = new Insets(0, 0, 0, 12);
        card.add(titleField, c);
        c.gridx = 1;
        card.add(authorField, c);
        c.gridx = 2; c.weightx = 0; c.insets = new Insets(0, 0, 0, 0);
        card.add(addButton, c);
        return card;
    }

    // Card holding the search bar, the table and the book count
    private JPanel createTableCard() {
        JPanel card = Theme.card(new BorderLayout());

        // search bar
        searchField.setPreferredSize(new Dimension(340, 40));
        searchField.getDocument().addDocumentListener(new DocumentListener() { // live search
            public void insertUpdate(DocumentEvent e) { searchBooks(); }
            public void removeUpdate(DocumentEvent e) { searchBooks(); }
            public void changedUpdate(DocumentEvent e) { searchBooks(); }
        });

        JButton clearButton = Theme.outlineButton("Show All");
        clearButton.setPreferredSize(new Dimension(100, 40));
        clearButton.addActionListener(e -> {
            searchField.setText("");
            refreshTable(service.getAllBooks());
        });

        JLabel tableTitle = new JLabel("Books");
        tableTitle.setFont(Theme.font(Font.BOLD, 16));
        tableTitle.setForeground(Theme.TEXT);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchBar.setOpaque(false);
        searchBar.add(searchField);
        searchBar.add(clearButton);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(16, 20, 14, 10));
        toolbar.add(tableTitle, BorderLayout.WEST);
        toolbar.add(searchBar, BorderLayout.EAST);

        // footer with the count
        countLabel.setFont(Theme.font(Font.PLAIN, 12));
        countLabel.setForeground(Theme.MUTED);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 20, 12, 20));
        footer.add(countLabel, BorderLayout.WEST);

        card.add(toolbar, BorderLayout.NORTH);
        card.add(createTable(), BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    // Table of books
    private JScrollPane createTable() {
        String[] columns = {"ID", "TITLE", "AUTHOR", "STATUS"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // table is view-only
            }
        };

        table = new JTable(tableModel) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getRowCount() == 0) { // empty state
                    Graphics2D g2 = Theme.smooth(g);
                    g2.setFont(Theme.font(Font.PLAIN, 14));
                    g2.setColor(Theme.MUTED);
                    String msg = "No books found";
                    int w = g2.getFontMetrics().stringWidth(msg);
                    g2.drawString(msg, (getWidth() - w) / 2, 60);
                    g2.dispose();
                }
            }
        };
        table.setRowHeight(46);
        table.setFont(Theme.font(Font.PLAIN, 14));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(ROW_LINE);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(Theme.PRIMARY_SOFT);
        table.setSelectionForeground(Theme.TEXT);
        table.setFocusable(true);
        table.getColumnModel().getColumn(0).setMinWidth(70);
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(3).setMinWidth(150);
        table.getColumnModel().getColumn(3).setMaxWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);

        // Header style
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        header.setPreferredSize(new Dimension(0, 40));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        t, value, false, false, row, col);
                label.setOpaque(true);
                label.setBackground(new Color(249, 250, 251));
                label.setForeground(Theme.MUTED);
                label.setFont(Theme.font(Font.BOLD, 11));
                label.setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(1, 0, 1, 0, Theme.BORDER),
                        new EmptyBorder(0, 20, 0, 12)));
                return label;
            }
        });

        // Row style
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        t, value, isSelected, false, row, col);
                label.setBorder(new EmptyBorder(0, 20, 0, 12));
                label.setFont(Theme.font(col == 1 ? Font.BOLD : Font.PLAIN, 14));
                label.setForeground(col == 0 || col == 2 ? Theme.MUTED : Theme.TEXT);
                if (isSelected) label.setForeground(Theme.TEXT);
                label.setBackground(isSelected ? Theme.PRIMARY_SOFT : Color.WHITE);
                return label;
            }
        });
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        Theme.styleScrollPane(scrollPane);
        return scrollPane;
    }

    // Shows "Available" / "Borrowed" as a small colored pill
    private static class StatusRenderer implements TableCellRenderer {
        private final JPanel panel = new JPanel(new GridBagLayout());
        private final Pill pill = new Pill();

        StatusRenderer() {
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(0, 20, 0, 0);
            panel.add(pill, c);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            boolean available = "Available".equals(value);
            pill.set(String.valueOf(value),
                    available ? Theme.PRIMARY_SOFT : Theme.WARNING_BG,
                    available ? Theme.SUCCESS : Theme.WARNING_TEXT);
            panel.setBackground(isSelected ? Theme.PRIMARY_SOFT : Color.WHITE);
            return panel;
        }
    }

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

    // Message pill + borrow / return buttons
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        JButton returnButton = Theme.outlineButton("Return Selected");
        returnButton.setPreferredSize(new Dimension(150, 42));
        returnButton.addActionListener(e -> returnSelected());

        JButton borrowButton = Theme.primaryButton("Borrow Selected");
        borrowButton.setPreferredSize(new Dimension(160, 42));
        borrowButton.addActionListener(e -> borrowSelected());

        buttons.add(returnButton);
        buttons.add(borrowButton);

        JPanel toastWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        toastWrap.setOpaque(false);
        toastWrap.add(toast);

        panel.add(toastWrap, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.EAST);
        panel.setPreferredSize(new Dimension(0, 42));
        return panel;
    }

    // ---------- TOAST (replaces pop-up dialogs) ----------

    private static final int INFO = 0, SUCCESS = 1, ERROR = 2;

    private static class Toast extends JComponent {
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

    private void showToast(String message, int kind) {
        Color dot = kind == SUCCESS ? new Color(74, 222, 128)
                : kind == ERROR ? new Color(248, 113, 113)
                : Theme.SKY;
        toast.set(message, dot);

        if (toastTimer != null) toastTimer.stop();
        toastTimer = new Timer(4000, e -> toast.setVisible(false));
        toastTimer.setRepeats(false);
        toastTimer.start();
    }

    // The service returns plain text, so pick the toast color from its wording
    private int kindOf(String message) {
        String m = message.toLowerCase();
        boolean failed = m.contains("already") || m.contains("not ") || m.contains("cannot")
                || m.contains("can't") || m.contains("invalid") || m.contains("fail")
                || m.contains("unable") || m.contains("error");
        return failed ? ERROR : SUCCESS;
    }

    // ---------- ACTIONS ----------

    private void addBook() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();

        if (service.addBook(title, author)) {
            titleField.setText("");
            authorField.setText("");
            refreshTable(service.getAllBooks());
            showToast("Book added to the library.", SUCCESS);
        } else {
            showToast("Fields can't be empty or contain commas.", ERROR);
        }
    }

    private void searchBooks() {
        String query = searchField.getText().trim();
        refreshTable(query.isEmpty() ? service.getAllBooks() : service.searchBooks(query));
    }

    private void borrowSelected() {
        int id = getSelectedBookId();
        if (id == -1) return;

        String result = service.borrowBook(id);
        refreshTable(currentList());
        selectBook(id);
        showToast(result, kindOf(result));
    }

    private void returnSelected() {
        int id = getSelectedBookId();
        if (id == -1) return;

        String result = service.returnBook(id);
        refreshTable(currentList());
        selectBook(id);
        showToast(result, kindOf(result));
    }

    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }

    // Keeps the current search results after borrowing / returning
    private List<Book> currentList() {
        String query = searchField.getText().trim();
        return query.isEmpty() ? service.getAllBooks() : service.searchBooks(query);
    }

    // Gets the ID of the highlighted row, or -1 if nothing is selected
    private int getSelectedBookId() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showToast("Please select a book first.", INFO);
            return -1;
        }
        return (int) tableModel.getValueAt(row, 0);
    }

    private void selectBook(int id) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if ((int) tableModel.getValueAt(row, 0) == id) {
                table.setRowSelectionInterval(row, row);
                return;
            }
        }
    }

    private void refreshTable(List<Book> books) {
        tableModel.setRowCount(0); // clear the table
        for (Book book : books) {
            tableModel.addRow(new Object[]{
                    book.getId(), book.getTitle(), book.getAuthor(), book.getStatus()
            });
        }

        List<Book> all = service.getAllBooks();
        int available = 0;
        for (Book book : all) {
            if ("Available".equals(book.getStatus())) available++;
        }
        totalValue.setText(String.valueOf(all.size()));
        availableValue.setText(String.valueOf(available));
        borrowedValue.setText(String.valueOf(all.size() - available));
        countLabel.setText("Showing " + books.size() + " of " + all.size() + " books");
    }
}