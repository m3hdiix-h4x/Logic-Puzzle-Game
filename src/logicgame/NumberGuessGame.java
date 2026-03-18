package logicgame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Random;

public class NumberGuessGame extends GamePanel {
    private int secret, maxNum, maxTries, triesLeft, roundScore;
    private GameTimer timer;

    private JLabel timerLabel, scoreLabel, triesLabel, feedbackLabel, hintLabel;
    private JTextField inputField;
    private JButton guessBtn;
    private JPanel historyPanel;
    private JScrollPane historyScroll;

    public NumberGuessGame(GameWindow window) {
        super(window);
        buildUI();
    }

    private void buildUI() {
        // ── Top ─────────────────────────────────────────────────────────────
        JPanel status = new JPanel(new GridLayout(1, 3));
        status.setBackground(UITheme.BG_PANEL);
        status.setBorder(new EmptyBorder(8, 20, 8, 20));
        timerLabel = statWidget(status, "⏱ TIME",       "01:00", UITheme.ACCENT_CYAN);
        scoreLabel = statWidget(status, "★ SCORE",      "0",     UITheme.SUCCESS);
        triesLabel = statWidget(status, "💡 TRIES LEFT", "—",     UITheme.WARNING);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UITheme.BG_DARK);
        top.add(topBar("🔢  NUMBER GUESS", UITheme.ACCENT_CYAN), BorderLayout.NORTH);
        top.add(status, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // ── Center ──────────────────────────────────────────────────────────
        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setBackground(UITheme.BG_DARK);
        center.setBorder(new EmptyBorder(20, 36, 20, 36));

        // Left: input
        JPanel inputArea = new JPanel();
        inputArea.setBackground(UITheme.BG_CARD);
        inputArea.setLayout(new BoxLayout(inputArea, BoxLayout.Y_AXIS));
        inputArea.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel prompt = new JLabel("GUESS THE NUMBER", SwingConstants.CENTER);
        prompt.setFont(UITheme.FONT_H2);
        prompt.setForeground(UITheme.ACCENT_CYAN);
        prompt.setAlignmentX(CENTER_ALIGNMENT);

        hintLabel = new JLabel("Range: 1 – ???", SwingConstants.CENTER);
        hintLabel.setFont(UITheme.FONT_BODY);
        hintLabel.setForeground(UITheme.TEXT_DIM);
        hintLabel.setAlignmentX(CENTER_ALIGNMENT);

        inputField = new JTextField(8);
        inputField.setFont(UITheme.FONT_NUM);
        inputField.setBackground(UITheme.BG_DARK);
        inputField.setForeground(UITheme.ACCENT_CYAN);
        inputField.setCaretColor(UITheme.ACCENT_CYAN);
        inputField.setBorder(UITheme.glowBorder(UITheme.ACCENT_CYAN));
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.setMaximumSize(new Dimension(200, 52));
        inputField.setAlignmentX(CENTER_ALIGNMENT);
        inputField.addActionListener(e -> handleGuess());

        guessBtn = new JButton("SUBMIT GUESS");
        UITheme.styleButton(guessBtn, UITheme.ACCENT_CYAN, Color.BLACK);
        guessBtn.setAlignmentX(CENTER_ALIGNMENT);
        guessBtn.setMaximumSize(new Dimension(200, 40));
        guessBtn.addActionListener(e -> handleGuess());

        feedbackLabel = new JLabel("—", SwingConstants.CENTER);
        feedbackLabel.setFont(new Font("Monospaced", Font.BOLD, 26));
        feedbackLabel.setForeground(UITheme.WARNING);
        feedbackLabel.setAlignmentX(CENTER_ALIGNMENT);

        JButton newGame = new JButton("⟳  NEW GAME");
        UITheme.styleButton(newGame, UITheme.BG_PANEL, UITheme.TEXT_DIM);
        newGame.setAlignmentX(CENTER_ALIGNMENT);
        newGame.setMaximumSize(new Dimension(200, 36));
        newGame.addActionListener(e -> startNewGame());

        inputArea.add(prompt);
        inputArea.add(Box.createVerticalStrut(8));
        inputArea.add(hintLabel);
        inputArea.add(Box.createVerticalStrut(24));
        inputArea.add(inputField);
        inputArea.add(Box.createVerticalStrut(12));
        inputArea.add(guessBtn);
        inputArea.add(Box.createVerticalStrut(18));
        inputArea.add(feedbackLabel);
        inputArea.add(Box.createVerticalStrut(24));
        inputArea.add(newGame);

        // Right: history
        JPanel histBox = new JPanel(new BorderLayout(0, 6));
        histBox.setBackground(UITheme.BG_CARD);
        histBox.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel histTitle = new JLabel("GUESS HISTORY", SwingConstants.CENTER);
        histTitle.setFont(UITheme.FONT_SMALL);
        histTitle.setForeground(UITheme.TEXT_DIM);
        histBox.add(histTitle, BorderLayout.NORTH);

        historyPanel = new JPanel();
        historyPanel.setLayout(new BoxLayout(historyPanel, BoxLayout.Y_AXIS));
        historyPanel.setBackground(UITheme.BG_CARD);

        historyScroll = new JScrollPane(historyPanel);
        historyScroll.setBorder(null);
        historyScroll.getViewport().setBackground(UITheme.BG_CARD);
        histBox.add(historyScroll, BorderLayout.CENTER);

        center.add(inputArea, BorderLayout.CENTER);
        center.add(histBox, BorderLayout.EAST);
        add(center, BorderLayout.CENTER);
    }

    @Override
    public void onShown() { startNewGame(); }

    private void startNewGame() {
        int level = scoreManager.getLevel();
        maxNum    = level == 1 ? 50  : level == 2 ? 100 : 200;
        maxTries  = level == 1 ? 10  : level == 2 ? 7   : 5;
        int limit = level == 1 ? 90  : level == 2 ? 60  : 45;

        secret    = new Random().nextInt(maxNum) + 1;
        triesLeft = maxTries;
        roundScore = 0;

        historyPanel.removeAll();
        historyPanel.revalidate();
        historyPanel.repaint();

        hintLabel.setText("Range: 1 – " + maxNum);
        feedbackLabel.setText("⚡ GAME ON!");
        feedbackLabel.setForeground(UITheme.ACCENT_CYAN);
        triesLabel.setText(String.valueOf(triesLeft));
        scoreLabel.setText(String.valueOf(scoreManager.getCurrentScore()));
        inputField.setText("");
        inputField.setEnabled(true);
        guessBtn.setEnabled(true);

        if (timer != null) timer.stop();
        timer = new GameTimer(limit,
                () -> timerLabel.setText(timer.formatTime()),
                this::timeUp);
        timer.start();
    }

    private void handleGuess() {
        if (!inputField.isEnabled()) return;
        int guess;
        try {
            guess = Integer.parseInt(inputField.getText().trim());
        } catch (NumberFormatException ex) {
            feedbackLabel.setText("⚠ Enter a valid number!");
            feedbackLabel.setForeground(UITheme.DANGER);
            return;
        }
        if (guess < 1 || guess > maxNum) {
            feedbackLabel.setText("Out of range (1–" + maxNum + ")");
            feedbackLabel.setForeground(UITheme.DANGER);
            return;
        }

        triesLeft--;
        inputField.setText("");

        if (guess == secret) {
            roundScore = 100 + timer.getScoreBonus() + triesLeft * 20;
            addHistory(guess, "✔", UITheme.SUCCESS);
            endGame(true);
            return;
        }

        String hint; Color hintColor;
        if (guess < secret) {
            int d = secret - guess;
            hint      = d > 20 ? "▲▲ MUCH HIGHER" : d > 5 ? "▲ HIGHER" : "▲ SLIGHTLY HIGHER";
            hintColor = UITheme.WARNING;
            addHistory(guess, "▲", UITheme.WARNING);
        } else {
            int d = guess - secret;
            hint      = d > 20 ? "▼▼ MUCH LOWER" : d > 5 ? "▼ LOWER" : "▼ SLIGHTLY LOWER";
            hintColor = new Color(100, 180, 255);
            addHistory(guess, "▼", new Color(100, 180, 255));
        }

        feedbackLabel.setText(hint);
        feedbackLabel.setForeground(hintColor);
        triesLabel.setText(String.valueOf(triesLeft));

        if (triesLeft <= 0) endGame(false);
    }

    private void addHistory(int guess, String arrow, Color color) {
        JLabel e = new JLabel(String.format("  %s  %3d", arrow, guess));
        e.setFont(UITheme.FONT_BODY);
        e.setForeground(color);
        historyPanel.add(e);
        historyPanel.revalidate();
        JScrollBar bar = historyScroll.getVerticalScrollBar();
        bar.setValue(bar.getMaximum());
    }

    private void endGame(boolean won) {
        timer.stop();
        inputField.setEnabled(false);
        guessBtn.setEnabled(false);
        scoreManager.addPoints(roundScore);
        scoreManager.saveHighScore("NumberGuess");
        scoreLabel.setText(String.valueOf(scoreManager.getCurrentScore()));

        if (won) {
            feedbackLabel.setText("🏆 CORRECT! +" + roundScore + " pts");
            feedbackLabel.setForeground(UITheme.SUCCESS);
        } else {
            feedbackLabel.setText("💀 Answer was: " + secret);
            feedbackLabel.setForeground(UITheme.DANGER);
        }
    }

    private void timeUp() {
        endGame(false);
        feedbackLabel.setText("⏰ TIME'S UP!  Answer: " + secret);
        feedbackLabel.setForeground(UITheme.DANGER);
    }
}