package bberp;

import bberp.common.AppSession;
import bberp.service.AuthService;
import com.zaxxer.hikari.HikariDataSource;

import javax.swing.*;
import java.awt.*;

class MenuPanel extends JPanel {
    MenuPanel(App app, HikariDataSource ds) {
        super(new BorderLayout());
        setBackground(Color.WHITE);

        String name = AppSession.user() == null ? "guest" : AppSession.user().getName();

        JButton salesOrders = App.button("Sales Orders", App.GREEN, Color.WHITE);
        salesOrders.addActionListener(e -> app.go(new SalesOrderListPanel(app, ds)));
        JButton logout = App.button("Log out", App.FIELD, Color.BLACK);
        logout.addActionListener(e -> {
            new AuthService(ds).logout();
            app.go(new LoginPanel(app, ds));
        });

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        content.add(App.label("Main Menu", 18));
        content.add(Box.createVerticalStrut(4));
        content.add(App.label("Signed in as " + name, 11));
        content.add(Box.createVerticalStrut(16));
        for (JButton b : new JButton[]{salesOrders, logout}) {
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(200, 32));
            b.setPreferredSize(new Dimension(200, 32));
            content.add(b);
            content.add(Box.createVerticalStrut(8));
        }
        content.add(Box.createVerticalGlue());

        add(new Sidebar(false, 480), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
    }
}
