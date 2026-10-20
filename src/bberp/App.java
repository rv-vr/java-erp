package bberp;

import bberp.common.DB;
import com.zaxxer.hikari.HikariDataSource;

import javax.swing.*;
import java.awt.Color;

public class App {
    static final Color BROWN = new Color(0x69, 0x2e, 0x16);

    private final HikariDataSource ds;
    private final JFrame frame = new JFrame("BB Bricks ERP");

    App() {
        this.ds = DB.create();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                ds.close();
            }
        });
        go(new LoginPanel(this, ds));
        frame.setVisible(true);
    }

    HikariDataSource ds() {
        return ds;
    }

    void go(JPanel screen) {
        frame.setContentPane(screen);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    static String money(java.math.BigDecimal v) {
        return String.format("$%,.2f", v == null ? java.math.BigDecimal.ZERO : v);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::new);
    }
}
