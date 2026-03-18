package logicgame;

import javax.swing.*;
import javax.swing.Timer;          // ← ADD this line explicitly
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;             // ← use specific imports instead of java.util.*
public class MemoryCardGame extends GamePanel {
    private static final String[] SYMBOLS = {
            "🐉","🦊","🌙","⭐","🔥","💎","🎯","🎲",
            "🌈","🦋","🏆","⚡","🎸","🚀","🌸","🦄"
    };

    private JLabel timerLabel, scoreLabel, movesLabel, matchLabel;
    private JPanel gridPanel;
    private GameTimer timer;

    private final List<JButton> cards   = new ArrayList<>();
    private final List<String>  symbols = new ArrayList<>();
    private boolean[] matched;
    private JButton firstCard  = null, secondCard = null;
    private int     firstIdx   = -1;
    private boolean checking   = false;
    private int moves = 0, matchCount = 0, pairs;

    public MemoryCardGame(GameWindow window) {
        super(window);
        buildUI();
    }

    private void buildUI() {
        JPanel status = new JPanel(new GridLayout(1, 4));
        status.setBackground(UITheme.BG_PANEL);
        status.setBorder(new EmptyBorder(8, 20, 8, 20));
        timerLabel = statWidget(status, "⏱ TIME",     "00:00", UITheme.ACCENT_CYAN);
        scoreLabel = statWidget(status, "★ SCORE",    "0",     UITheme.SUCCESS);
        movesLabel = statWidget(status, "🔄 MOVES",   "0",     UITheme.ACCENT_PURPLE);
        matchLabel = statWidget(status, "✔ MATCHED",  "0/0",   UITheme.WARNING);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UITheme.BG_DARK);
        top.add(topBar("🃏  MEMORY CARDS", UITheme.ACCENT_PURPLE), BorderLayout.NORTH);
        top.add(status, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        gridPanel = new JPanel();
        gridPanel.setBackground(UITheme.BG_DARK);
        add(gridPanel, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout());
        south.setBackground(UITheme.BG_PANEL);
        JButton ng = new JButton("⟳  NEW GAME");
        UITheme.styleButton(ng, UITheme.BG_CARD, UITheme.TEXT_DIM);
        ng.addActionListener(e -> startNewGame());
        south.add(ng);
        add(south, BorderLayout.SOUTH);
    }

    @Override
    public void onShown() { startNewGame(); }

    private void startNewGame() {
        int level = scoreManager.getLevel();
        int cols  = (level == 3) ? 6 : 4;
        int rows  = (level == 1) ? 4 : (level == 2) ? 5 : 4;
        pairs     = (cols * rows) / 2;
        matched   = new boolean[cols * rows];
        moves = 0; matchCount = 0;
        firstCard = null; secondCard = null; firstIdx = -1; checking = false;

        symbols.clear();
        for (int i = 0; i < pairs; i++) {
            symbols.add(SYMBOLS[i % SYMBOLS.length]);
            symbols.add(SYMBOLS[i % SYMBOLS.length]);
        }
        Collections.shuffle(symbols);

        gridPanel.removeAll();
        gridPanel.setLayout(new GridLayout(rows, cols, 8, 8));
        gridPanel.setBorder(new EmptyBorder(16, 40, 16, 40));
        cards.clear();

        for (int i = 0; i < cols * rows; i++) {
            JButton btn = new JButton("?");
            btn.setFont(UITheme.FONT_NUM);
            btn.setBackground(UITheme.BG_CARD);
            btn.setForeground(UITheme.ACCENT_PURPLE);
            btn.setFocusPainted(false);
            btn.setBorder(UITheme.glowBorder(UITheme.ACCENT_PURPLE));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            int idx = i;
            btn.addActionListener(e -> handleFlip(idx));
            cards.add(btn);
            gridPanel.add(btn);
        }
        gridPanel.revalidate();
        gridPanel.repaint();

        movesLabel.setText("0");
        matchLabel.setText("0/" + pairs);
        scoreLabel.setText(String.valueOf(scoreManager.getCurrentScore()));

        if (timer != null) timer.stop();
        timer = new GameTimer(0, () -> timerLabel.setText(timer.formatTime()), null);
        timer.start();
    }

    private void handleFlip(int idx) {
        if (checking || matched[idx] || cards.get(idx) == firstCard) return;
        JButton btn = cards.get(idx);
        btn.setText(symbols.get(idx));
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        btn.setBackground(new Color(40, 30, 80));

        if (firstCard == null) {
            firstCard = btn; firstIdx = idx;
            return;
        }

        secondCard = btn;
        checking   = true;
        moves++;
        movesLabel.setText(String.valueOf(moves));

        Timer delay = new Timer(750, e -> {
            if (symbols.get(firstIdx).equals(symbols.get(idx))) {
                matched[firstIdx] = true;
                matched[idx]      = true;
                styleMatched(firstCard);
                styleMatched(secondCard);
                matchCount++;
                matchLabel.setText(matchCount + "/" + pairs);
                if (matchCount == pairs) endGame();
            } else {
                resetCard(firstCard);
                resetCard(secondCard);
            }
            firstCard = null; secondCard = null; firstIdx = -1; checking = false;
        });
        delay.setRepeats(false);
        delay.start();
    }

    private void styleMatched(JButton btn) {
        btn.setBackground(new Color(0, 55, 30));
        btn.setBorder(UITheme.glowBorder(UITheme.SUCCESS));
    }

    private void resetCard(JButton btn) {
        btn.setText("?");
        btn.setFont(UITheme.FONT_NUM);
        btn.setBackground(UITheme.BG_CARD);
        btn.setBorder(UITheme.glowBorder(UITheme.ACCENT_PURPLE));
    }

    private void endGame() {
        timer.stop();
        int bonus = Math.max(0, 1000 - moves * 15) + timer.getScoreBonus();
        scoreManager.addPoints(bonus);
        scoreManager.saveHighScore("MemoryCard");
        scoreLabel.setText(String.valueOf(scoreManager.getCurrentScore()));
        showMessage("🎉 COMPLETE!",
                "All pairs matched!\nMoves: " + moves + "\nTime: " + timer.formatTime() +
                        "\n\n+ " + bonus + " points added!", JOptionPane.INFORMATION_MESSAGE);
    }
}