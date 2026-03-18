package logicgame;

import java.awt.*;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

public class UITheme {
    public static final Color BG_DARK       = new Color(10, 10, 20);
    public static final Color BG_PANEL      = new Color(18, 18, 35);
    public static final Color BG_CARD       = new Color(25, 28, 50);
    public static final Color ACCENT_CYAN   = new Color(0, 220, 255);
    public static final Color ACCENT_PURPLE = new Color(150, 80, 255);
    public static final Color ACCENT_PINK   = new Color(255, 60, 160);
    public static final Color TEXT_PRIMARY  = new Color(220, 230, 255);
    public static final Color TEXT_DIM      = new Color(110, 120, 160);
    public static final Color SUCCESS       = new Color(50, 255, 150);
    public static final Color DANGER        = new Color(255, 70, 70);
    public static final Color WARNING       = new Color(255, 200, 50);

    public static final Font FONT_TITLE = new Font("Monospaced", Font.BOLD, 32);
    public static final Font FONT_H2    = new Font("Monospaced", Font.BOLD, 20);
    public static final Font FONT_BODY  = new Font("Monospaced", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Monospaced", Font.PLAIN, 12);
    public static final Font FONT_BTN   = new Font("Monospaced", Font.BOLD, 14);
    public static final Font FONT_NUM   = new Font("Monospaced", Font.BOLD, 24);

    public static Border glowBorder(Color color) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        );
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 70, 120), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );
    }

    public static void styleButton(javax.swing.JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(FONT_BTN);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
    }
}