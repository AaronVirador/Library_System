package ui;

import model.Book;
import service.LibraryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class LibraryGUI extends JFrame {

    private static final Color BACKGROUND = new Color(243, 244, 246);
    private static final Color ROW_ALT = new Color(249, 250, 251);
    private static final Color GRID = new Color(229, 231, 235);

    private LibraryService service = new LibraryService();

    private JTextField titleField = Theme.textField();
    private JTextField authorField = Theme.textField();
    private JTextField searchField = Theme.textField();

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel countLabel = new JLabel(" ");

    public LibraryGUI() {
        setTitle("Library System");
        setSize(900, 620);
        setMinimumSize(new Dimension(800, 520));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);

        refreshTable(service.getAllBooks());
    }

    // ---------- HEADER ----------

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, Theme.PRIMARY, getWidth(), 0, Theme.ACCENT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel title = new JLabel("Library System");
        title.setFont(new Font(Theme.FONT, Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Manage your books, borrowing and returns");
        subtitle.setFont(new Font(Theme.FONT, Font.PLAIN, 13));
        subtitle.setForeground(new Color(224, 231, 255));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(subtitle);

        JButton logout = new JButton("Log Out");
        logout.setFont(new Font(Theme.FONT, Font.BOLD, 13));
        logout.setForeground(Color.WHITE);
        logout.setContentAreaFilled(false);
        logout.setFocusPainted(false);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.WHITE, 1, true),
                new EmptyBorder(8, 18, 8, 18)));
        logout.addActionListener(e -> logout());

        JPanel right = new JPanel(new GridBagLayout()); // centers the button vertically
        right.setOpaque(false);
        right.add(logout);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ---------- BODY ----------

    private JPanel createBody() {
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setBackground(BACKGROUND);
        body.setBorder(new EmptyBorder(20, 24, 20, 24));

        body.add(createControls(), BorderLayout.NORTH);
        body.add(createTablePanel(), BorderLayout.CENTER);
        body.add(createBottomPanel(), BorderLayout.SOUTH);
        return body;
    }

    // Add book row + search row
    private JPanel createControls() {
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setOpaque(false);

        titleField.setPreferredSize(new Dimension(220, 40));
        authorField.setPreferredSize(new Dimension(200, 40));
        searchField.setPreferredSize(new Dimension(300, 40));

        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        addRow.setOpaque(false);
        addRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        addRow.add(Theme.fieldLabel("Title"));
        addRow.add(titleField);
        addRow.add(Theme.fieldLabel("Author"));
        addRow.add(authorField);
        JButton addButton = Theme.primaryButton("Add Book");
        addButton.setPreferredSize(new Dimension(120, 40));
        addButton.addActionListener(e -> addBook());
        authorField.addActionListener(e -> addButton.doClick()); // Enter key
        addRow.add(addButton);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchRow.add(Theme.fieldLabel("Search"));
        searchRow.add(searchField);
        JButton searchButton = Theme.primaryButton("Search");
        searchButton.setPreferredSize(new Dimension(110, 40));
        searchButton.addActionListener(e -> searchBooks());
        searchField.addActionListener(e -> searchButton.doClick()); // Enter key
        JButton showAllButton = outlineButton("Show All");
        showAllButton.addActionListener(e -> {
            searchField.setText("");
            refreshTable(service.getAllBooks());
        });
        searchRow.add(searchButton);
        searchRow.add(showAllButton);

        controls.add(addRow);
        controls.add(searchRow);
        return controls;
    }

    // Table of books
    private JScrollPane createTablePanel() {
        String[] columns = {"ID", "Title", "Author", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // table is view-only
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(38);
        table.setFont(new Font(Theme.FONT, Font.PLAIN, 14));
        table.setShowVerticalLines(false);
        table.setGridColor(GRID);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(224, 231, 255));
        table.setSelectionForeground(Theme.TEXT);
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        table.getColumnModel().getColumn(3).setMaxWidth(140);

        // Header style
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 40));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        t, value, false, false, row, col);
                label.setOpaque(true);
                label.setBackground(Color.WHITE);
                label.setForeground(Theme.MUTED);
                label.setFont(new Font(Theme.FONT, Font.BOLD, 13));
                label.setBorder(new EmptyBorder(0, 12, 0, 12));
                return label;
            }
        });

        // Row style (striped rows + colored status)
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        t, value, isSelected, false, row, col);
                label.setBorder(new EmptyBorder(0, 12, 0, 12));
                label.setFont(new Font(Theme.FONT, Font.PLAIN, 14));
                label.setForeground(Theme.TEXT);

                if (!isSelected) {
                    label.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                }

                if (col == 3) {
                    label.setFont(new Font(Theme.FONT, Font.BOLD, 13));
                    label.setForeground("Available".equals(value) ? Theme.SUCCESS : Theme.ERROR);
                }
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(GRID, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    // Book count + borrow / return buttons
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        countLabel.setFont(new Font(Theme.FONT, Font.PLAIN, 13));
        countLabel.setForeground(Theme.MUTED);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        JButton returnButton = outlineButton("Return Selected");
        returnButton.addActionListener(e -> returnSelected());

        JButton borrowButton = Theme.primaryButton("Borrow Selected");
        borrowButton.setPreferredSize(new Dimension(160, 40));
        borrowButton.addActionListener(e -> borrowSelected());

        buttons.add(returnButton);
        buttons.add(borrowButton);

        panel.add(countLabel, BorderLayout.WEST);
        panel.add(buttons, BorderLayout.EAST);
        return panel;
    }

    // White button with a purple outline and a hover effect
    private JButton outlineButton(String text) {
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
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? new Color(238, 242, 255) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(Theme.PRIMARY);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(new Font(Theme.FONT, Font.BOLD, 14));
        button.setForeground(Theme.PRIMARY);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 40));
        return button;
    }

    // ---------- ACTIONS ----------

    private void addBook() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();

        if (service.addBook(title, author)) {
            titleField.setText("");
            authorField.setText("");
            refreshTable(service.getAllBooks());
        } else {
            JOptionPane.showMessageDialog(this,
                    "Fields can't be empty or contain commas.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void searchBooks() {
        refreshTable(service.searchBooks(searchField.getText().trim()));
    }

    private void borrowSelected() {
        int id = getSelectedBookId();
        if (id == -1) return;

        JOptionPane.showMessageDialog(this, service.borrowBook(id));
        refreshTable(service.getAllBooks());
    }

    private void returnSelected() {
        int id = getSelectedBookId();
        if (id == -1) return;

        JOptionPane.showMessageDialog(this, service.returnBook(id));
        refreshTable(service.getAllBooks());
    }

    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }

    // Gets the ID of the highlighted row, or -1 if nothing is selected
    private int getSelectedBookId() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book first.");
            return -1;
        }
        return (int) tableModel.getValueAt(row, 0);
    }

    private void refreshTable(List<Book> books) {
        tableModel.setRowCount(0); // clear the table
        for (Book book : books) {
            tableModel.addRow(new Object[]{
                    book.getId(), book.getTitle(), book.getAuthor(), book.getStatus()
            });
        }
        countLabel.setText("Showing " + books.size() + " of "
                + service.getAllBooks().size() + " books");
    }
}