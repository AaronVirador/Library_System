package ui;

import constants.AppConstants;
import service.AuthService;
import util.LoginGuard;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class LoginFrame extends JFrame {

    private AuthService auth = new AuthService();
    private CardLayout cardLayout = new CardLayout();
    private JPanel cards = new JPanel(cardLayout);

    // Student login fields
    private JTextField loginUser = Inputs.withHint(Inputs.textField(), "Enter your username");
    private JPasswordField loginPass = Inputs.withHint(Inputs.passwordField(), "Enter your password");
    private JLabel loginMsg = Theme.messageLabel();
    private JButton loginButton = Buttons.primaryButton("Log In");

    // Admin login fields (the username is fixed)
    private JTextField adminUser = Inputs.textField();
    private JPasswordField adminPass = Inputs.withHint(Inputs.passwordField(), "Enter the admin password");
    private JLabel adminMsg = Theme.messageLabel();
    private JButton adminButton = Buttons.primaryButton("Log In as Admin");

    // Sign up fields
    private JTextField signUser = Inputs.withHint(Inputs.textField(), "Choose a username");
    private JPasswordField signPass = Inputs.passwordField();
    private JPasswordField signConfirm = Inputs.passwordField();
    private JComboBox<String> signQuestion = Inputs.comboBox(AppConstants.SECURITY_QUESTIONS);
    private JTextField signAnswer = Inputs.withHint(Inputs.textField(), "Your answer");
    private JLabel signMsg = Theme.messageLabel();

    // Forgot password fields
    private JTextField forgotUser = Inputs.withHint(Inputs.textField(), "Enter your username");
    private JButton forgotContinue = Buttons.primaryButton("Continue");
    private JPanel forgotStep2 = new JPanel();
    private JLabel forgotQuestion = new JLabel(" ");
    private JTextField forgotAnswer = Inputs.withHint(Inputs.textField(), "Your answer");
    private JPasswordField forgotNewPass = Inputs.passwordField();
    private JPasswordField forgotConfirm = Inputs.passwordField();
    private JLabel forgotMsg = Theme.messageLabel();

    // Shows the "wait 30 seconds" countdown and disables the form while locked
    private LockView studentLock;
    private LockView adminLock;
    private Timer lockTimer;

    public LoginFrame() {
        setTitle("Library System - Welcome");
        Logo.applyWindowIcon(this);
        setSize(980, 700);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        adminUser.setText(AppConstants.ADMIN_USERNAME);
        adminUser.setEditable(false);

        cards.setBackground(Color.WHITE);
        cards.add(createRolePanel(), "role");
        cards.add(createLoginPanel(), "login");
        cards.add(createAdminPanel(), "admin");
        cards.add(createSignUpPanel(), "signup");
        cards.add(createForgotPanel(), "forgot");

        add(createBrandPanel(), BorderLayout.WEST);
        add(cards, BorderLayout.CENTER);

        lockTimer = new Timer(250, e -> {
            studentLock.refresh();
            adminLock.refresh();
        });
        lockTimer.start();
    }

    @Override
    public void dispose() {
        lockTimer.stop();
        super.dispose();
    }

    // ---------- LEFT SIDE: branding ----------

    private JPanel createBrandPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Theme.smooth(g);
                g2.setPaint(new GradientPaint(0, 0, Theme.PRIMARY, 0, getHeight(), Theme.PRIMARY_DARK));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // soft decorative circles (very subtle)
                g2.setColor(new Color(255, 255, 255, 9));
                g2.fill(new Ellipse2D.Float(-120, getHeight() - 220, 300, 300));
                g2.setColor(new Color(246, 201, 14, 22));
                g2.fill(new Ellipse2D.Float(getWidth() - 150, -110, 280, 280));
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(400, 0));

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JComponent logo = Logo.component(150);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel school = new JLabel("Dasmariñas East");
        school.setFont(Theme.font(Font.BOLD, 15));
        school.setForeground(Theme.GOLD);
        school.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel line1 = new JLabel("Library");
        line1.setFont(Theme.font(Font.BOLD, 44));
        line1.setForeground(Color.WHITE);
        line1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel line2 = new JLabel("System");
        line2.setFont(Theme.font(Font.PLAIN, 44));
        line2.setForeground(new Color(255, 255, 255, 200));
        line2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("<html>Find, borrow and return books<br>in just a few clicks.</html>");
        tagline.setFont(Theme.font(Font.PLAIN, 14));
        tagline.setForeground(new Color(255, 255, 255, 190));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel values = new JLabel("SERVICE  ·  LEADERSHIP  ·  EXCELLENCE");
        values.setFont(Theme.font(Font.BOLD, 11));
        values.setForeground(new Color(255, 255, 255, 150));
        values.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(logo);
        text.add(Box.createVerticalStrut(28));
        text.add(school);
        text.add(Box.createVerticalStrut(2));
        text.add(line1);
        text.add(line2);
        text.add(Box.createVerticalStrut(14));
        text.add(tagline);
        text.add(Box.createVerticalStrut(40));
        text.add(values);

        panel.add(text);
        return panel;
    }

    // ---------- RIGHT SIDE: forms ----------

    private JPanel formPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(36, 64, 36, 64));
        return panel;
    }

    // Adds a component to a panel with a gap above it
    private void addTo(JPanel parent, JComponent component, int gap) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(Box.createVerticalStrut(gap));
        parent.add(component);
    }

    // Pushes the form content to the vertical middle
    private void center(JPanel p) {
        p.add(Box.createVerticalGlue());
    }

    // "Don't have an account? Sign up"
    private JPanel switchRow(String text, String linkText, String card) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel label = new JLabel(text + " ");
        label.setFont(Theme.font(Font.PLAIN, 13));
        label.setForeground(Theme.MUTED);

        JButton link = Buttons.linkButton(linkText);
        link.addActionListener(e -> showCard(card));

        row.add(label);
        row.add(link);
        return row;
    }

    // ---------- Role picker ----------

    private JPanel createRolePanel() {
        JPanel p = formPanel();
        p.add(Box.createVerticalGlue());

        addTo(p, Theme.title("Welcome"), 0);
        addTo(p, Theme.subtitle("Choose how you want to log in"), 6);

        RoleTile student = new RoleTile("Student", "Borrow and return books", false);
        student.addActionListener(e -> showCard("login"));
        RoleTile admin = new RoleTile("Admin", "Manage books and see who borrowed them", true);
        admin.addActionListener(e -> showCard("admin"));

        addTo(p, student, 32);
        addTo(p, admin, 14);
        center(p);
        return p;
    }

    // ---------- Lockout countdown (see LockView.java) ----------

    // ---------- Student login ----------

    private JPanel createLoginPanel() {
        JPanel p = formPanel();
        p.add(Box.createVerticalGlue());

        addTo(p, Theme.title("Welcome back"), 0);
        addTo(p, Theme.subtitle("Log in to continue to your library"), 6);

        addTo(p, Theme.fieldLabel("Username"), 28);
        addTo(p, loginUser, 8);
        addTo(p, Theme.fieldLabel("Password"), 16);
        addTo(p, loginPass, 8);

        JButton forgot = Buttons.linkButton("Forgot password?");
        forgot.addActionListener(e -> showCard("forgot"));
        addTo(p, forgot, 12);

        addTo(p, loginMsg, 8);

        studentLock = new LockView(loginButton, () -> loginUser.getText(), loginPass);
        addTo(p, studentLock.panel, 4);

        loginButton.addActionListener(e -> doLogin());
        loginPass.addActionListener(e -> loginButton.doClick()); // Enter key
        addTo(p, loginButton, 8);

        addTo(p, switchRow("Don't have an account?", "Sign up", "signup"), 20);
        addTo(p, switchRow("Not a student?", "Change role", "role"), 8);
        center(p);
        return p;
    }

    // ---------- Admin login ----------

    private JPanel createAdminPanel() {
        JPanel p = formPanel();
        p.add(Box.createVerticalGlue());

        addTo(p, Theme.title("Admin login"), 0);
        addTo(p, Theme.subtitle("Enter the administrator password"), 6);

        addTo(p, Theme.fieldLabel("Username"), 28);
        addTo(p, adminUser, 8);
        addTo(p, Theme.fieldLabel("Password"), 16);
        addTo(p, adminPass, 8);

        addTo(p, adminMsg, 12);

        adminLock = new LockView(adminButton, () -> AppConstants.ADMIN_USERNAME, adminPass);
        addTo(p, adminLock.panel, 4);

        adminButton.addActionListener(e -> doAdminLogin());
        adminPass.addActionListener(e -> adminButton.doClick()); // Enter key
        addTo(p, adminButton, 8);

        addTo(p, switchRow("Not an admin?", "Change role", "role"), 22);
        center(p);
        return p;
    }

    // ---------- Sign up ----------

    private JPanel createSignUpPanel() {
        JPanel p = formPanel();
        p.add(Box.createVerticalGlue());

        addTo(p, Theme.title("Create account"), 0);
        addTo(p, Theme.subtitle("Sign up to start using the library"), 6);

        addTo(p, Theme.fieldLabel("Username"), 18);
        addTo(p, signUser, 6);
        addTo(p, Theme.fieldLabel("Password"), 12);
        addTo(p, signPass, 6);
        addTo(p, Theme.fieldLabel("Confirm password"), 12);
        addTo(p, signConfirm, 6);

        addTo(p, Theme.fieldLabel("Security question (for password recovery)"), 12);
        addTo(p, signQuestion, 6);
        addTo(p, signAnswer, 8);

        addTo(p, signMsg, 8);

        JButton createButton = Buttons.primaryButton("Create Account");
        createButton.addActionListener(e -> doSignUp());
        signAnswer.addActionListener(e -> createButton.doClick());
        addTo(p, createButton, 6);

        addTo(p, switchRow("Already have an account?", "Log in", "login"), 14);
        center(p);
        return p;
    }

    // ---------- Forgot password ----------

    private JPanel createForgotPanel() {
        JPanel p = formPanel();
        p.add(Box.createVerticalGlue());

        addTo(p, Theme.title("Reset password"), 0);
        addTo(p, Theme.subtitle("Enter your username to find your account"), 6);

        addTo(p, Theme.fieldLabel("Username"), 28);
        addTo(p, forgotUser, 8);

        forgotContinue.addActionListener(e -> doFindAccount());
        forgotUser.addActionListener(e -> forgotContinue.doClick());
        addTo(p, forgotContinue, 16);

        // Step 2 (hidden until the account is found)
        forgotStep2.setLayout(new BoxLayout(forgotStep2, BoxLayout.Y_AXIS));
        forgotStep2.setOpaque(false);

        forgotQuestion.setFont(Theme.font(Font.BOLD, 14));
        forgotQuestion.setForeground(Theme.PRIMARY);
        addTo(forgotStep2, forgotQuestion, 0);
        addTo(forgotStep2, forgotAnswer, 8);
        addTo(forgotStep2, Theme.fieldLabel("New password"), 14);
        addTo(forgotStep2, forgotNewPass, 6);
        addTo(forgotStep2, Theme.fieldLabel("Confirm new password"), 14);
        addTo(forgotStep2, forgotConfirm, 6);

        JButton resetButton = Buttons.primaryButton("Reset Password");
        resetButton.addActionListener(e -> doReset());
        forgotConfirm.addActionListener(e -> resetButton.doClick());
        addTo(forgotStep2, resetButton, 16);

        forgotStep2.setVisible(false);
        addTo(p, forgotStep2, 0);

        addTo(p, forgotMsg, 10);
        addTo(p, switchRow("Remembered it?", "Back to log in", "login"), 12);
        center(p);
        return p;
    }

    // ---------- Actions ----------

    private void doLogin() {
        String username = loginUser.getText().trim();
        String password = new String(loginPass.getPassword());

        if (LoginGuard.millisLeft(username) > 0) return; // still locked

        if (auth.login(username, password)) {
            LoginGuard.reset(username);
            dispose(); // close this window
            new LibraryGUI(auth.getStoredUsername(username), false).setVisible(true);
        } else {
            boolean locked = LoginGuard.recordFailure(username);
            loginPass.setText("");
            if (locked) {
                loginMsg.setText(" ");
                studentLock.refresh();
            } else {
                Theme.showError(loginMsg, "Incorrect username or password. "
                        + LoginGuard.attemptsLeft(username) + " attempt(s) left.");
            }
        }
    }

    private void doAdminLogin() {
        String key = AppConstants.ADMIN_USERNAME;
        String password = new String(adminPass.getPassword());

        if (LoginGuard.millisLeft(key) > 0) return; // still locked

        if (auth.adminLogin(password)) {
            LoginGuard.reset(key);
            dispose();
            new LibraryGUI(AppConstants.ADMIN_USERNAME, true).setVisible(true);
        } else {
            boolean locked = LoginGuard.recordFailure(key);
            adminPass.setText("");
            if (locked) {
                adminMsg.setText(" ");
                adminLock.refresh();
            } else {
                Theme.showError(adminMsg, "Incorrect password. "
                        + LoginGuard.attemptsLeft(key) + " attempt(s) left.");
            }
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

        adminPass.setText("");
        adminMsg.setText(" ");

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

        // every password goes back to hidden
        for (JPasswordField field : new JPasswordField[]{loginPass, adminPass, signPass, signConfirm,
                forgotNewPass, forgotConfirm}) {
            Inputs.hidePassword(field);
        }

        cardLayout.show(cards, name);
        studentLock.refresh();
        adminLock.refresh();
    }
}
