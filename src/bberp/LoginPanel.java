package bberp;

import bberp.service.AuthService;
import com.zaxxer.hikari.HikariDataSource;

import javax.swing.*;
import java.awt.*;

class LoginPanel extends JPanel {
    LoginPanel(App app, HikariDataSource ds) {
        super(new BorderLayout());
        setBackground(Color.WHITE);

        AuthService auth = new AuthService(ds);
        JTextField user = App.style(new JTextField());
        JPasswordField pass = App.style(new JPasswordField());
        JLabel error = App.label(" ", 11);
        error.setForeground(Color.RED);
        JButton login = App.button("Log in", App.GREEN, Color.WHITE);
        login.setFont(new Font("SansSerif", Font.PLAIN, 12));
        login.setAlignmentX(Component.LEFT_ALIGNMENT);
        login.setMaximumSize(new Dimension(73, 24));

        Runnable submit = () -> {
            error.setText(" ");
            try {
                auth.login(user.getText().trim(), new String(pass.getPassword()));
                app.go(new MenuPanel(app, ds));
            } catch (IllegalArgumentException ex) {
                error.setText(ex.getMessage());
            } catch (RuntimeException ex) {
                error.setText("Database unavailable.");
            }
        };
        login.addActionListener(e -> submit.run());
        user.addActionListener(e -> submit.run());
        pass.addActionListener(e -> submit.run());

        for (JComponent c : new JComponent[]{user, pass}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            c.setPreferredSize(new Dimension(283, 24));
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        }

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        form.add(App.label("Welcome User!", 14));
        form.add(Box.createVerticalStrut(6));
        form.add(App.label("Username", 13));
        form.add(Box.createVerticalStrut(4));
        form.add(user);
        form.add(Box.createVerticalStrut(12));
        form.add(App.label("Password", 13));
        form.add(Box.createVerticalStrut(4));
        form.add(pass);
        form.add(Box.createVerticalStrut(16));
        form.add(login);
        form.add(error);
        form.add(Box.createVerticalGlue());
        form.add(App.label("BB Bricks ERP v0.0.1", 9));

        add(new Sidebar(true, 240), BorderLayout.WEST);
        add(form, BorderLayout.CENTER);
    }
}
