package ui;

import constants.AppConstants;
import service.AuthService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private AuthService auth = new AuthService();
    private CardLayout cardLayout = new CardLayout();
    private JPanel cards = new JPanel(cardLayout);

    // Login fields
    private JTextField loginUser = Theme.textField();
    private JPasswordField loginPass = Theme.passwordField();
    private JLabel loginMsg = Theme.messageLabel();

    // Sign up fields
    private JTextField signUser = Theme.textField();
    private JPasswordField signPass = Theme.passwordField();
    private JPasswordField signConfirm = Theme.passwordField();
    private JComboBox<String> signQuestion = new JComboBox<>(AppConstants.SECURITY_QUESTIONS);
    private JTextField signAnswer = Theme.textField();
    private JLabel signMsg = Theme.messageLabel();

    // Forgot password fields
    private JTextField forgotUser = Theme.textField();
    private JButton forgotContinue = Theme.primaryButton("Continue");
    private JPanel forgotStep2 = new JPanel();
    private JLabel forgotQuestion = new JLabel(" ");
    private JTextField forgotAnswer = Theme.textField();
    private JPasswordField forgotNewPass = Theme.passwordField();
    private JPasswordField forgotConfirm = Theme.passwordField();
    private JLabel forgotMsg = Theme.messageLabel();

    public LoginFrame() {
        setTitle("Library System - Welcome");
        setSize(900, 580);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1, 2));

        cards.setBackground(Color.WHITE);
        cards.add(createLoginPanel(), "login");
        cards.add(createSignUpPanel(), "signup");
        cards.add(createForgotPanel(), "forgot");

        add(createBrandPanel());
        add(cards);
    }

    // ---------- LEFT SIDE: heading / branding ----------

    private JPanel createBrandPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, Theme.PRIMARY, getWidth(), getHeight(), Theme.ACCENT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel line1 = new JLabel("LIBRARY");
        line1.setFont(new Font(Theme.FONT, Font.BOLD, 48));
        line1.setForeground(Color.WHITE);

        JLabel line2 = new JLabel("SYSTEM");
        line2.setFont(new Font(Theme.FONT, Font.PLAIN, 48));
        line2.setForeground(new Color(224, 231, 255));

        JLabel tagline = new JLabel("Manage your books. Simple and smart.");
        tagline.setFont(new Font(Theme.FONT, Font.PLAIN, 14));
        tagline.setForeground(new Color(224, 231, 255));

        text.add(line1);
        text.add(line2);
        text.add(Box.createVerticalStrut(12));
        text.add(tagline);

        panel.add(text);
        return panel;
    }

    // ---------- RIGHT SIDE: forms ----------

    private JPanel formPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        return panel;
    }

    // Adds a component to a panel with a gap above it
    private void addTo(JPanel parent, JComponent component, int gap) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(Box.createVerticalStrut(gap));
        parent.add(component);
    }

    // "Don't have an account? Sign up"
    private JPanel switchRow(String text, String linkText, String card) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel label = new JLabel(text + " ");
        label.setFont(new Font(Theme.FONT, Font.PLAIN, 13));
        label.setForeground(Theme.MUTED);

        JButton link = Theme.linkButton(linkText);
        link.addActionListener(e -> showCard(card));

        row.add(label);
        row.add(link);
        return row;
    }

    private JPanel createLoginPanel() {
        JPanel p = formPanel();

        addTo(p, Theme.title("Welcome back"), 0);
        addTo(p, Theme.subtitle("Log in to continue to your library"), 4);

        addTo(p, Theme.fieldLabel("Username"), 28);
        addTo(p, loginUser, 6);
        addTo(p, Theme.fieldLabel("Password"), 16);
        addTo(p, loginPass, 6);

        JButton forgot = Theme.linkButton("Forgot password?");
        forgot.addActionListener(e -> showCard("forgot"));
        addTo(p, forgot, 10);

        addTo(p, loginMsg, 8);

        JButton loginButton = Theme.primaryButton("Log In");
        loginButton.addActionListener(e -> doLogin());
        loginPass.addActionListener(e -> loginButton.doClick()); // Enter key
        addTo(p, loginButton, 8);

        addTo(p, switchRow("Don't have an account?", "Sign up", "signup"), 20);
        return p;
    }

    private JPanel createSignUpPanel() {
        JPanel p = formPanel();

        addTo(p, Theme.title("Create account"), 0);
        addTo(p, Theme.subtitle("Sign up to start using the library"), 4);

        addTo(p, Theme.fieldLabel("Username"), 20);
        addTo(p, signUser, 6);
        addTo(p, Theme.fieldLabel("Password"), 12);
        addTo(p, signPass, 6);
        addTo(p, Theme.fieldLabel("Confirm password"), 12);
        addTo(p, signConfirm, 6);

        addTo(p, Theme.fieldLabel("Security question (for password recovery)"), 12);
        signQuestion.setFont(new Font(Theme.FONT, Font.PLAIN, 13));
        signQuestion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        addTo(p, signQuestion, 6);
        addTo(p, signAnswer, 6);

        addTo(p, signMsg, 6);

        JButton createButton = Theme.primaryButton("Create Account");
        createButton.addActionListener(e -> doSignUp());
        signAnswer.addActionListener(e -> createButton.doClick());
        addTo(p, createButton, 6);

        addTo(p, switchRow("Already have an account?", "Log in", "login"), 14);
        return p;
    }

    private JPanel createForgotPanel() {
        JPanel p = formPanel();

        addTo(p, Theme.title("Reset password"), 0);
        addTo(p, Theme.subtitle("Enter your username to find your account"), 4);

        addTo(p, Theme.fieldLabel("Username"), 24);
        addTo(p, forgotUser, 6);

        forgotContinue.addActionListener(e -> doFindAccount());
        forgotUser.addActionListener(e -> forgotContinue.doClick());
        addTo(p, forgotContinue, 14);

        // Step 2 (hidden until the account is found)
        forgotStep2.setLayout(new BoxLayout(forgotStep2, BoxLayout.Y_AXIS));
        forgotStep2.setOpaque(false);

        forgotQuestion.setFont(new Font(Theme.FONT, Font.BOLD, 14));
        forgotQuestion.setForeground(Theme.PRIMARY);
        addTo(forgotStep2, forgotQuestion, 0);
        addTo(forgotStep2, forgotAnswer, 6);
        addTo(forgotStep2, Theme.fieldLabel("New password"), 12);
        addTo(forgotStep2, forgotNewPass, 6);
        addTo(forgotStep2, Theme.fieldLabel("Confirm new password"), 12);
        addTo(forgotStep2, forgotConfirm, 6);

        JButton resetButton = Theme.primaryButton("Reset Password");
        resetButton.addActionListener(e -> doReset());
        forgotConfirm.addActionListener(e -> resetButton.doClick());
        addTo(forgotStep2, resetButton, 14);

        forgotStep2.setVisible(false);
        addTo(p, forgotStep2, 0);

        addTo(p, forgotMsg, 8);
        addTo(p, switchRow("Remembered it?", "Back to log in", "login"), 12);
        return p;
    }

    // ---------- Actions ----------

    private void doLogin() {
        String username = loginUser.getText().trim();
        String password = new String(loginPass.getPassword());

        if (auth.login(username, password)) {
            dispose(); // close this window
            new LibraryGUI().setVisible(true); // open the main system
        } else {
            Theme.showError(loginMsg, "Incorrect username or password.");
        }
    }

    private void doSignUp() {
        String username = signUser.getText().trim();

        String result = auth.signUp(
                username,
                new String(signPass.getPassword()),
                new String(signConfirm.getPassword()),
                (String) signQuestion.getSelectedItem(),
                signAnswer.getText());

        if (result == null) {
            showCard("login");
            loginUser.setText(username);
            Theme.showSuccess(loginMsg, "Account created! You can log in now.");
        } else {
            Theme.showError(signMsg, result);
        }
    }

    private void doFindAccount() {
        String question = auth.getSecurityQuestion(forgotUser.getText().trim());

        if (question == null) {
            Theme.showError(forgotMsg, "No account found with that username.");
            return;
        }

        forgotQuestion.setText(question);
        forgotUser.setEditable(false);
        forgotContinue.setVisible(false);
        forgotStep2.setVisible(true);
        forgotMsg.setText(" ");
    }

    private void doReset() {
        String result = auth.resetPassword(
                forgotUser.getText().trim(),
                forgotAnswer.getText(),
                new String(forgotNewPass.getPassword()),
                new String(forgotConfirm.getPassword()));

        if (result == null) {
            showCard("login");
            Theme.showSuccess(loginMsg, "Password reset! Log in with your new password.");
        } else {
            Theme.showError(forgotMsg, result);
        }
    }

    // Switches the screen and clears all fields
    private void showCard(String name) {
        loginUser.setText("");
        loginPass.setText("");
        loginMsg.setText(" ");

        signUser.setText("");
        signPass.setText("");
        signConfirm.setText("");
        signAnswer.setText("");
        signQuestion.setSelectedIndex(0);
        signMsg.setText(" ");

        forgotUser.setText("");
        forgotUser.setEditable(true);
        forgotContinue.setVisible(true);
        forgotStep2.setVisible(false);
        forgotAnswer.setText("");
        forgotNewPass.setText("");
        forgotConfirm.setText("");
        forgotMsg.setText(" ");

        cardLayout.show(cards, name);
    }
}