package logicgame;

import javax.swing.*;
import java.awt.*;

public abstract class GamePanel extends JPanel {
    protected final GameWindow    window;
    protected final ScoreManager  scoreManager = ScoreManager.getInstance();

    public GamePanel(GameWindow window) {
        this.window = window;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
    }

    public void onShown() {}

    protected JButton makeBackButton() {
        JButton btn = new JButton("← MENU");
        UITheme.styleButton(btn, UITheme.BG_CARD, UITheme.TEXT_DIM);
        btn.addActionListener(e -> window.showScreen(GameWindow.HOME));
        return btn;
    }

    protected JLabel makeTitle(String text, Color color) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(UITheme.FONT_TITLE);
        lbl.setForeground(color);
        return lbl;
    }

    protected JPanel topBar(String title, Color color) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.BG_PANEL);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        bar.add(makeBackButton(), BorderLayout.WEST);
        bar.add(makeTitle(title, color), BorderLayout.CENTER);
        return bar;
    }

    protected JLabel statWidget(JPanel parent, String label, String val, Color color) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBackground(UITheme.BG_PANEL);
        JLabel l = new JLabel(label, SwingConstants.CENTER);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.TEXT_DIM);
        JLabel v = new JLabel(val, SwingConstants.CENTER);
        v.setFont(UITheme.FONT_H2);
        v.setForeground(color);
        p.add(l); p.add(v);
        parent.add(p);
        return v;
    }

    protected void showMessage(String title, String msg, int type) {
        JOptionPane.showMessageDialog(this, msg, title, type);
    }
}