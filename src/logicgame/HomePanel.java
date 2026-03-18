package logicgame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class HomePanel extends JPanel {
    private final GameWindow   window;
    private final ScoreManager sm = ScoreManager.getInstance();
    private JLabel scoreLabel, levelLabel;

    public HomePanel(GameWindow window) {
        this.window = window;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        // ── Header ──────────────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setBackground(UITheme.BG_PANEL);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(20, 0, 14, 0));

        JLabel title = new JLabel("◈  LOGIC PUZZLE GAME  ◈", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.ACCENT_CYAN);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("7 GAMES  ·  TRAIN YOUR BRAIN  ·  BEAT THE CLOCK",
                SwingConstants.CENTER);
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_DIM);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(sub);
        add(header, BorderLayout.NORTH);

        // ── Game grid ────────────────────────────────────────────────────────
        JPanel center = new JPanel(new GridLayout(2, 4, 14, 14));
        center.setBackground(UITheme.BG_DARK);
        center.setBorder(new EmptyBorder(18, 28, 8, 28));

        center.add(gameCard("🔢", "NUMBER GUESS",
                "Guess the secret number!", UITheme.ACCENT_CYAN,
                GameWindow.NUMGUESS, "NumberGuess"));
        center.add(gameCard("🃏", "MEMORY CARDS",
                "Flip & match all pairs.", UITheme.ACCENT_PURPLE,
                GameWindow.MEMORY, "MemoryCard"));
        center.add(gameCard("🔲", "SUDOKU",
                "Fill the 9×9 grid.", UITheme.ACCENT_PINK,
                GameWindow.SUDOKU, "Sudoku"));
        center.add(gameCard("🐍", "SNAKE",
                "Eat. Grow. Survive!", new Color(50, 220, 100),
                GameWindow.SNAKE, "Snake"));
        center.add(gameCard("❌", "TIC-TAC-TOE",
                "Beat the AI. 3 in a row!", new Color(255, 160, 50),
                GameWindow.TICTACTOE, "TicTacToe"));
        center.add(gameCard("🔤", "HANGMAN",
                "Guess the word. Save the man!", new Color(255, 100, 100),
                GameWindow.HANGMAN, "Hangman"));
        center.add(gameCard("🧱", "BRICK BREAKER",
                "Break all the bricks!", new Color(100, 200, 255),
                GameWindow.BRICKBREAKER, "BrickBreaker"));

        // Filler card
        JPanel filler = new JPanel();
        filler.setBackground(UITheme.BG_DARK);
        center.add(filler);

        add(center, BorderLayout.CENTER);

        // ── Bottom bar ───────────────────────────────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(UITheme.BG_PANEL);
        bottom.setBorder(new EmptyBorder(10, 28, 10, 28));

        scoreLabel = new JLabel("SCORE: " + sm.getCurrentScore(), SwingConstants.LEFT);
        scoreLabel.setFont(UITheme.FONT_H2);
        scoreLabel.setForeground(UITheme.SUCCESS);

        levelLabel = new JLabel("LEVEL: " + sm.getLevelName(), SwingConstants.CENTER);
        levelLabel.setFont(UITheme.FONT_H2);
        levelLabel.setForeground(UITheme.WARNING);

        JPanel lvlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        lvlBtns.setBackground(UITheme.BG_PANEL);
        JLabel lbl = new JLabel("DIFFICULTY: ");
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(UITheme.TEXT_DIM);
        lvlBtns.add(lbl);
        addLevelBtn(lvlBtns, "EASY",   1, UITheme.SUCCESS);
        addLevelBtn(lvlBtns, "MEDIUM", 2, UITheme.WARNING);
        addLevelBtn(lvlBtns, "HARD",   3, UITheme.DANGER);

        JButton resetBtn = new JButton("RESET SCORE");
        UITheme.styleButton(resetBtn, UITheme.BG_CARD, UITheme.DANGER);
        resetBtn.addActionListener(e -> {
            sm.resetScore();
            scoreLabel.setText("SCORE: 0");
        });
        lvlBtns.add(Box.createHorizontalStrut(12));
        lvlBtns.add(resetBtn);

        bottom.add(scoreLabel, BorderLayout.WEST);
        bottom.add(levelLabel, BorderLayout.CENTER);
        bottom.add(lvlBtns,    BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
    }

    private void addLevelBtn(JPanel parent, String name, int lvl, Color color) {
        JButton b = new JButton(name);
        UITheme.styleButton(b, UITheme.BG_CARD, color);
        b.addActionListener(e -> {
            sm.setLevel(lvl);
            levelLabel.setText("LEVEL: " + sm.getLevelName());
        });
        parent.add(b);
    }

    private JPanel gameCard(String icon, String title, String desc,
                            Color accent, String screen, String scoreKey) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel iconLbl = new JLabel(icon, SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        iconLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Monospaced", Font.BOLD, 13));
        titleLbl.setForeground(accent);
        titleLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel hsLbl = new JLabel("BEST: " + sm.getHighScore(scoreKey), SwingConstants.CENTER);
        hsLbl.setFont(UITheme.FONT_SMALL);
        hsLbl.setForeground(UITheme.TEXT_DIM);
        hsLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel descLbl = new JLabel("<html><center>" + desc + "</center></html>",
                SwingConstants.CENTER);
        descLbl.setFont(UITheme.FONT_SMALL);
        descLbl.setForeground(UITheme.TEXT_DIM);
        descLbl.setAlignmentX(CENTER_ALIGNMENT);

        JButton play = new JButton("▶  PLAY");
        UITheme.styleButton(play, accent, Color.BLACK);
        play.setAlignmentX(CENTER_ALIGNMENT);
        play.setMaximumSize(new Dimension(130, 34));
        play.addActionListener(e -> window.showScreen(screen));

        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { card.setBorder(new EmptyBorder(13,13,13,13)); card.repaint(); }
            public void mouseExited(MouseEvent e)  { card.setBorder(new EmptyBorder(16,16,16,16)); card.repaint(); }
        });

        card.add(iconLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(2));
        card.add(hsLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(descLbl);
        card.add(Box.createVerticalStrut(10));
        card.add(play);
        return card;
    }
}