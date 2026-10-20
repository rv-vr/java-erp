package bberp.ui;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JButton;

/**
 * Flat coloured button. Has a no-arg constructor so the NetBeans GUI Builder
 * can instantiate it as a bean.
 *
 * <p>Not opaque on purpose: the default button UI refills opaque buttons with
 * {@code getBackground()}, which would hide the disabled colour.</p>
 */
public class FlatButton extends JButton {

    private static final Color DISABLED = new Color(0xe0, 0xe0, 0xe0);

    public FlatButton() {
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(isEnabled() ? getBackground() : DISABLED);
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }
}
