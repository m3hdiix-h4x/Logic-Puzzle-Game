package logicgame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class HangmanGame extends GamePanel {
    private static final String[][] WORDS = {
            // Easy
            {"CAT","DOG","SUN","BALL","FISH","BIRD","CAKE","TREE","MOON","STAR"},
            // Medium
            {"PYTHON","BRIDGE","CASTLE","ROCKET","PLANET","JUNGLE","COOKIE","WIZARD"},
            // Hard
            {"ALGORITHM","LABYRINTH","XYLOPHONE","QUARANTINE","PHOSPHORUS","Byzantine"}
    };

    private String word = "";
    private Set<Character> guessed = new HashSet<>();
    private int wrong = 0;
    private static final int MAX_WRONG = 6;
    private boolean gameActive = false;

    private JLabel wordLabel, statusLabel, scoreLabel, wrongLabel;
    private HangmanCanvas hangCanvas;
    private JPanel letterPanel;

    public HangmanGame(GameWindow window) {
        super(window);
        buildUI();
    }

    private void buildUI() {
        JPanel status = new JPanel(new GridLayout(1, 3));
        status.setBackground(UITheme.BG_PANEL);
        status.setBorder(new EmptyBorder(8, 20, 8, 20));
        scoreLabel = statWidget(status, "★ SCORE",  "0", UITheme.SUCCESS);
        wrongLabel = statWidget(status, "💀 WRONG", "0/" + MAX_WRONG, UITheme.DANGER);
        statusLabel= statWidget(status, "🔤 STATUS","PRESS START", UITheme.ACCENT_CYAN);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UITheme.BG_DARK);
        top.add(topBar("🔤  HANGMAN", new Color(255,100,100)), BorderLayout.NORTH);
        top.add(status, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // ── Center: hangman + word ────────────────────────────────────────────
        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBackground(UITheme.BG_DARK);
        center.setBorder(new EmptyBorder(10, 30, 0, 30));

        hangCanvas = new HangmanCanvas();
        hangCanvas.setPreferredSize(new Dimension(260, 280));
        hangCanvas.setBackground(new Color(10, 10, 25));

        wordLabel = new JLabel("", SwingConstants.CENTER);
        wordLabel.setFont(new Font("Monospaced", Font.BOLD, 34));
        wordLabel.setForeground(UITheme.ACCENT_CYAN);

        center.add(hangCanvas, BorderLayout.CENTER);
        center.add(wordLabel,  BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        // ── Letter buttons ────────────────────────────────────────────────────
        JPanel southAll = new JPanel(new BorderLayout());
        southAll.setBackground(UITheme.BG_PANEL);

        letterPanel = new JPanel(new GridLayout(2, 13, 4, 4));
        letterPanel.setBackground(UITheme.BG_PANEL);
        letterPanel.setBorder(new EmptyBorder(8, 20, 4, 20));

        for (char c = 'A'; c <= 'Z'; c++) {
            JButton btn = new JButton(String.valueOf(c));
            UITheme.styleButton(btn, UITheme.BG_CARD, UITheme.TEXT_PRIMARY);
            btn.setFont(UITheme.FONT_BTN);
            btn.setPreferredSize(new Dimension(46, 34));
            char letter = c;
            btn.addActionListener(e -> guess(letter, btn));
            letterPanel.add(btn);
        }

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        btnRow.setBackground(UITheme.BG_PANEL);
        JButton startBtn = new JButton("▶  NEW WORD");
        UITheme.styleButton(startBtn, new Color(255,100,100), Color.BLACK);
        startBtn.addActionListener(e -> startGame());
        btnRow.add(startBtn);

        southAll.add(letterPanel, BorderLayout.CENTER);
        southAll.add(btnRow,      BorderLayout.SOUTH);
        add(southAll, BorderLayout.SOUTH);
    }

    @Override public void onShown() { startGame(); }

    private void startGame() {
        int level = scoreManager.getLevel() - 1;
        String[] pool = WORDS[level];
        word = pool[new Random().nextInt(pool.length)];
        guessed.clear();
        wrong = 0;
        gameActive = true;

        updateWordLabel();
        wrongLabel.setText("0/" + MAX_WRONG);
        statusLabel.setText("GUESS THE WORD!");
        scoreLabel.setText(String.valueOf(scoreManager.getCurrentScore()));
        hangCanvas.repaint();

        for (Component c : letterPanel.getComponents()) {
            JButton btn = (JButton) c;
            UITheme.styleButton(btn, UITheme.BG_CARD, UITheme.TEXT_PRIMARY);
            btn.setEnabled(true);
        }
    }

    private void guess(char letter, JButton btn) {
        if (!gameActive || guessed.contains(letter)) return;
        guessed.add(letter);
        btn.setEnabled(false);

        if (word.indexOf(letter) >= 0) {
            btn.setBackground(new Color(0, 50, 25));
            btn.setForeground(UITheme.SUCCESS);
            if (isWordSolved()) endGame(true);
            else updateWordLabel();
        } else {
            wrong++;
            btn.setBackground(new Color(50, 0, 0));
            btn.setForeground(UITheme.DANGER);
            wrongLabel.setText(wrong + "/" + MAX_WRONG);
            hangCanvas.repaint();
            if (wrong >= MAX_WRONG) endGame(false);
        }
    }

    private void updateWordLabel() {
        StringBuilder sb = new StringBuilder();
        for (char c : word.toCharArray()) {
            sb.append(guessed.contains(c) ? c : '_').append(' ');
        }
        wordLabel.setText(sb.toString().trim());
    }

    private boolean isWordSolved() {
        for (char c : word.toCharArray()) if (!guessed.contains(c)) return false;
        return true;
    }

    private void endGame(boolean won) {
        gameActive = false;
        if (won) {
            int pts = (MAX_WRONG - wrong) * 50 + scoreManager.getLevel() * 80;
            scoreManager.addPoints(pts);
            scoreManager.saveHighScore("Hangman");
            scoreLabel.setText(String.valueOf(scoreManager.getCurrentScore()));
            statusLabel.setText("YOU GOT IT! 🎉 +" + pts);
            statusLabel.setForeground(UITheme.SUCCESS);
        } else {
            statusLabel.setText("WORD: " + word + " 💀");
            statusLabel.setForeground(UITheme.DANGER);
            wordLabel.setText(word.chars()
                    .mapToObj(c -> String.valueOf((char)c) + " ")
                    .reduce("", String::concat).trim());
            wordLabel.setForeground(UITheme.DANGER);
        }
        hangCanvas.repaint();
    }

    // ── Hangman drawing ────────────────────────────────────────────────────────
    private class HangmanCanvas extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Color gallow = new Color(100, 80, 160);
            g2.setColor(gallow);
            // Base
            g2.drawLine(30, 260, 200, 260);
            // Pole
            g2.drawLine(80, 260, 80, 30);
            // Top beam
            g2.drawLine(80, 30, 180, 30);
            // Rope
            g2.drawLine(180, 30, 180, 70);

            g2.setColor(new Color(255, 100, 100));
            // Head
            if (wrong >= 1) g2.drawOval(162, 70, 36, 36);
            // Body
            if (wrong >= 2) g2.drawLine(180, 106, 180, 180);
            // Left arm
            if (wrong >= 3) g2.drawLine(180, 120, 145, 155);
            // Right arm
            if (wrong >= 4) g2.drawLine(180, 120, 215, 155);
            // Left leg
            if (wrong >= 5) g2.drawLine(180, 180, 145, 220);
            // Right leg
            if (wrong >= 6) g2.drawLine(180, 180, 215, 220);

            // Face when dead
            if (wrong >= 6) {
                g2.setColor(UITheme.DANGER);
                g2.setFont(new Font("Monospaced", Font.BOLD, 12));
                g2.drawString("x  x", 167, 91);
                g2.drawArc(167, 94, 24, 12, 0, -180);
            } else if (wrong == 0) {
                g2.setColor(UITheme.SUCCESS);
                g2.setFont(UITheme.FONT_SMALL);
                g2.drawString("^  ^", 167, 91);
                g2.drawArc(167, 88, 24, 12, 0, 180);
            }
        }
    }
}