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
import java.util.ArrayList;
import java.util.List;

/**
 * The main window. The admin sees every book and who borrowed it, and can add and
 * delete books. A student can search, borrow and return books.
 */
public class LibraryGUI extends JFrame {

    private static final Color ROW_LINE = new Color(240, 242, 245);

    private static final String FILTER_ALL = "All";
    private static final String FILTER_AVAILABLE = "Available";
    private static final String FILTER_BORROWED = "Borrowed";

    private final String username;
    private final boolean admin;
    private LibraryService service = new LibraryService();

    private JTextField titleField;
    private JTextField authorField;
    private JTextField searchField;

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel countLabel = new JLabel(" ");
    private String filter = FILTER_ALL;

    // Summary numbers at the top
    private JLabel totalValue = statValue();
    private JLabel availableValue = statValue();
    private JLabel borrowedValue = statValue();

    // Small message that appears next to the buttons
    private Toast toast = new Toast();
    private Timer toastTimer;

    public LibraryGUI(String username, boolean admin) {
        this.username = username;
        this.admin = admin;

        titleField = Theme.withHint(Theme.textField(), "Book title");
        authorField = Theme.withHint(Theme.textField(), "Author name");
        searchField = Theme.withHint(Theme.textField(), admin
                ? "Search title, author or borrower..."
                : "Search by title or author...");

        setTitle(admin ? "Library System - Admin" : "Library System");
        setSize(1100, admin ? 840 : 760);
        setMinimumSize(new Dimension(940, 680));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);

        refreshTable();
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

        JPanel right = new JPanel(new GridBagLayout()); // centers things vertically
        right.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 0, 18);
        right.add(createUserChip(), c);
        c.insets = new Insets(0, 0, 0, 0);
        right.add(logout, c);

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

    // Round avatar with the first letter + name + role
    private JPanel createUserChip() {
        JComponent avatar = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Theme.smooth(g);
                g2.setColor(admin ? Theme.GOLD : Theme.PRIMARY_SOFT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(admin ? new Color(113, 63, 18) : Theme.PRIMARY);
                g2.setFont(Theme.font(Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                String letter = username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase();
                g2.drawString(letter, (getWidth() - fm.stringWidth(letter)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(38, 38));

        JLabel name = new JLabel(username);
        name.setFont(Theme.font(Font.BOLD, 13));
        name.setForeground(Theme.TEXT);

        JLabel role = new JLabel(admin ? "Administrator" : "Student");
        role.setFont(Theme.font(Font.PLAIN, 11));
        role.setForeground(admin ? Theme.WARNING_TEXT : Theme.MUTED);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        text.add(name);
        text.add(role);

        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        chip.setOpaque(false);
        chip.add(avatar);
        chip.add(text);
        return chip;
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
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(stats);
        if (admin) { // only the admin can add books
            JComponent add = createAddCard();
            add.setAlignmentX(Component.LEFT_ALIGNMENT);
            top.add(Box.createVerticalStrut(16));
            top.add(add);
        }

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
        row.add(statCard(admin ? "Borrowed" : "Borrowed by you", borrowedValue,
                Theme.WARNING_TEXT, Theme.WARNING_BG));
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

    // Card with the "add book" form (admin only)
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

    // Card holding the filters, search bar, table and book count
    private JPanel createTableCard() {
        JPanel card = Theme.card(new BorderLayout());

        // title + filter chips
        JLabel tableTitle = new JLabel("Books");
        tableTitle.setFont(Theme.font(Font.BOLD, 16));
        tableTitle.setForeground(Theme.TEXT);

        JPanel leftBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftBar.setOpaque(false);
        leftBar.add(tableTitle);
        leftBar.add(Box.createHorizontalStrut(10));

        ButtonGroup group = new ButtonGroup();
        for (String name : new String[]{FILTER_ALL, FILTER_AVAILABLE, FILTER_BORROWED}) {
            JToggleButton chip = Theme.chip(name);
            chip.addActionListener(e -> {
                filter = name;
                refreshTable();
            });
            group.add(chip);
            leftBar.add(chip);
            if (name.equals(FILTER_ALL)) chip.setSelected(true);
        }

        // search bar
        searchField.setPreferredSize(new Dimension(300, 40));
        searchField.getDocument().addDocumentListener(new DocumentListener() { // live search
            public void insertUpdate(DocumentEvent e) { refreshTable(); }
            public void removeUpdate(DocumentEvent e) { refreshTable(); }
            public void changedUpdate(DocumentEvent e) { refreshTable(); }
        });

        JButton clearButton = Theme.outlineButton("Clear");
        clearButton.setPreferredSize(new Dimension(80, 40));
        clearButton.addActionListener(e -> searchField.setText(""));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchBar.setOpaque(false);
        searchBar.add(searchField);
        searchBar.add(clearButton);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(16, 20, 14, 10));
        toolbar.add(leftBar, BorderLayout.WEST);
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
        String[] columns = admin
                ? new String[]{"ID", "TITLE", "AUTHOR", "STATUS", "BORROWED BY"}
                : new String[]{"ID", "TITLE", "AUTHOR", "STATUS"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // table is view-only
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Integer.class : String.class; // so IDs sort as numbers
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
        table.setAutoCreateRowSorter(true); // click a column title to sort
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

        table.getColumnModel().getColumn(0).setMinWidth(70);
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(3).setMinWidth(160);
        table.getColumnModel().getColumn(3).setMaxWidth(190);
        table.getColumnModel().getColumn(3).setPreferredWidth(170);
        if (admin) {
            table.getColumnModel().getColumn(4).setMinWidth(150);
            table.getColumnModel().getColumn(4).setMaxWidth(220);
            table.getColumnModel().getColumn(4).setPreferredWidth(190);
        }

        // Header style (with a small arrow on the sorted column)
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        header.setPreferredSize(new Dimension(0, 40));
        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        t, value, false, false, row, col);
                String text = String.valueOf(value);
                if (t.getRowSorter() != null && !t.getRowSorter().getSortKeys().isEmpty()) {
                    RowSorter.SortKey key = t.getRowSorter().getSortKeys().get(0);
                    if (key.getColumn() == t.convertColumnIndexToModel(col)) {
                        text += key.getSortOrder() == SortOrder.ASCENDING ? "  ▲" : "  ▼";
                    }
                }
                label.setText(text);
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
        DefaultTableCellRenderer rowRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        t, value, isSelected, false, row, col);
                label.setBorder(new EmptyBorder(0, 20, 0, 12));
                label.setHorizontalAlignment(SwingConstants.LEFT); // numbers default to the right
                label.setFont(Theme.font(col == 1 ? Font.BOLD : Font.PLAIN, 14));
                label.setForeground(col == 0 || col == 2 ? Theme.MUTED : Theme.TEXT);
                label.setBackground(isSelected ? Theme.PRIMARY_SOFT : Color.WHITE);

                if (col == 4) { // "borrowed by" column (admin)
                    boolean empty = value == null || value.toString().isEmpty();
                    label.setText(empty ? "—" : value.toString());
                    label.setForeground(empty ? new Color(156, 163, 175) : Theme.TEXT);
                    if (!empty) label.setFont(Theme.font(Font.BOLD, 14));
                }
                return label;
            }
        };
        table.setDefaultRenderer(Object.class, rowRenderer);
        table.setDefaultRenderer(Integer.class, rowRenderer); // the ID column holds numbers
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        Theme.styleScrollPane(scrollPane);
        return scrollPane;
    }

    // Shows the status as a small colored pill
    private static class StatusRenderer implements TableCellRenderer {
        private final JPanel panel = new JPanel(new GridBagLayout());
        private final Pill pill = new Pill();

        StatusRenderer() {
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            c.weightx = 1.0; // without this the pill floats to the middle of the cell
            c.insets = new Insets(0, 20, 0, 0);
            panel.add(pill, c);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            boolean available = FILTER_AVAILABLE.equals(value);
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

    // Message pill + action buttons
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        if (admin) {
            JButton deleteButton = Theme.dangerButton("Delete Selected");
            deleteButton.setPreferredSize(new Dimension(160, 42));
            deleteButton.addActionListener(e -> deleteSelected());
            buttons.add(deleteButton);
        } else {
            JButton returnButton = Theme.outlineButton("Return Selected");
            returnButton.setPreferredSize(new Dimension(150, 42));
            returnButton.addActionListener(e -> returnSelected());

            JButton borrowButton = Theme.primaryButton("Borrow Selected");
            borrowButton.setPreferredSize(new Dimension(160, 42));
            borrowButton.addActionListener(e -> borrowSelected());

            buttons.add(returnButton);
            buttons.add(borrowButton);
        }

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

    // LibraryService success messages start with "You " or "Deleted"; anything else is a problem
    private int kindOf(String message) {
        return message.startsWith("You ") || message.startsWith("Deleted") ? SUCCESS : ERROR;
    }

    // ---------- ACTIONS ----------

    private void addBook() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();

        if (service.addBook(title, author)) {
            titleField.setText("");
            authorField.setText("");
            refreshTable();
            showToast("Book added to the library.", SUCCESS);
        } else {
            showToast("Fields can't be empty or contain commas.", ERROR);
        }
    }

    private void borrowSelected() {
        int id = getSelectedBookId();
        if (id == -1) return;

        String result = service.borrowBook(id, username);
        refreshTable();
        selectBook(id);
        showToast(result, kindOf(result));
    }

    private void returnSelected() {
        int id = getSelectedBookId();
        if (id == -1) return;

        String result = service.returnBook(id, username);
        refreshTable();
        selectBook(id);
        showToast(result, kindOf(result));
    }

    private void deleteSelected() {
        int id = getSelectedBookId();
        if (id == -1) return;

        int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
        String title = String.valueOf(tableModel.getValueAt(modelRow, 1));

        // A borrowed book can't be deleted, so say so before asking "are you sure?"
        if (!FILTER_AVAILABLE.equals(tableModel.getValueAt(modelRow, 3))) {
            String who = String.valueOf(tableModel.getValueAt(modelRow, 4));
            showToast("Can't delete: this book is borrowed" + (who.isEmpty() ? "." : " by " + who + "."), ERROR);
            return;
        }

        boolean sure = ConfirmDialog.ask(this, "Delete this book?",
                "\"" + title + "\" will be permanently removed from the library.", "Delete");
        if (!sure) return;

        String result = service.deleteBook(id);
        refreshTable();
        showToast(result, kindOf(result));
    }

    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }

    // Gets the ID of the highlighted row, or -1 if nothing is selected
    private int getSelectedBookId() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showToast("Please select a book first.", INFO);
            return -1;
        }
        return (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
    }

    // Highlights the row of a book (if it is still in the list)
    private void selectBook(int id) {
        for (int modelRow = 0; modelRow < tableModel.getRowCount(); modelRow++) {
            if ((int) tableModel.getValueAt(modelRow, 0) == id) {
                int viewRow = table.convertRowIndexToView(modelRow);
                if (viewRow >= 0) table.setRowSelectionInterval(viewRow, viewRow);
                return;
            }
        }
    }

    // Books that match the search box and the All / Available / Borrowed filter
    private List<Book> visibleBooks() {
        String query = searchField.getText().trim();
        List<Book> base = query.isEmpty() ? service.getAllBooks() : service.searchBooks(query, admin);
        if (filter.equals(FILTER_ALL)) return base;

        List<Book> result = new ArrayList<>();
        for (Book book : base) {
            if (book.getStatus().equals(filter)) result.add(book);
        }
        return result;
    }

    private void refreshTable() {
        List<Book> books = visibleBooks();

        tableModel.setRowCount(0); // clear the table
        for (Book book : books) {
            String status = book.getStatus();
            // a student sees which borrowed books are their own
            if (!admin && FILTER_BORROWED.equals(status) && book.getBorrowedBy().equalsIgnoreCase(username)) {
                status = "Borrowed by you";
            }
            if (admin) {
                tableModel.addRow(new Object[]{book.getId(), book.getTitle(), book.getAuthor(),
                        status, book.getBorrowedBy()});
            } else {
                tableModel.addRow(new Object[]{book.getId(), book.getTitle(), book.getAuthor(), status});
            }
        }

        List<Book> all = service.getAllBooks();
        int available = 0;
        for (Book book : all) {
            if (FILTER_AVAILABLE.equals(book.getStatus())) available++;
        }
        totalValue.setText(String.valueOf(all.size()));
        availableValue.setText(String.valueOf(available));
        borrowedValue.setText(String.valueOf(admin ? all.size() - available : service.countBorrowedBy(username)));
        countLabel.setText("Showing " + books.size() + " of " + all.size() + " books");
    }
}