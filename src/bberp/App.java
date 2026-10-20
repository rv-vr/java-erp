package bberp;

import bberp.common.DB;
import com.zaxxer.hikari.HikariDataSource;

import javax.swing.*;
import java.awt.*;

public class App {
    static final Color BROWN = new Color(0x69, 0x2e, 0x16);
    static final Color GREEN = new Color(0x0f, 0x7f, 0x4b);
    static final Color FIELD = new Color(0xf3, 0xf3, 0xf3);

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

    static JButton button(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        b.setBackground(bg);
        b.setForeground(fg);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(true);
        return b;
    }

    static <T extends JComponent> T style(T c) {
        c.setBackground(FIELD);
        c.setBorder(BorderFactory.createEmptyBorder(4, 7, 4, 7));
        c.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return c;
    }

    static JLabel label(String text, int size) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, size));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::new);
    }
}
