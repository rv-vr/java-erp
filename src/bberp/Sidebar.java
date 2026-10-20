package bberp;

import javax.swing.*;
import java.awt.*;

class Sidebar extends JPanel {
    private final boolean centered;

    Sidebar(boolean centered, int height) {
        this.centered = centered;
        setBackground(App.BROWN);
        setPreferredSize(new Dimension(109, height));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);

        int x = (getWidth() - 57) / 2;
        int y = centered ? (getHeight() - 57) / 2 : 27;
        int lx = x + 9;
        int ly = y + 13;
        g2.fillRect(lx, ly + 13, 19, 11);
        g2.fillRect(lx + 21, ly + 13, 19, 11);
        g2.fillRect(lx + 11, ly, 19, 11);

        g2.setFont(new Font("SansSerif", Font.BOLD, 8));
        String text = "BB  BRICKS";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, x + (57 - fm.stringWidth(text)) / 2, y + 46);
        g2.dispose();
    }
}
