package logicgame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Random;

public class TicTacToeGame extends GamePanel {
    private static final Color X_COLOR = new Color(255, 100, 100);
    private static final Color O_COLOR = new Color(100, 180, 255);
    private static final Color WIN_COLOR = new Color(50, 255, 150);

    private int[] board = new int[9]; // 0=empty,1=player,2=AI
    private JButton[] cells = new JButton[9];
    private JLabel statusLabel, scoreLabel, winsLabel, lossLabel;
    private int wins = 0, losses = 0, draws = 0;
    private boolean playerTurn = true, gameActive = false;

    private static final int[][] WINS = {
            {0,1,2},{3,4,5},{6,7,8},
            {0,3,6},{1,4,7},{2,5,8},
            {0,4,8},{2,4,6}
    };

    public TicTacToeGame(GameWindow window) {
        super(window);
        buildUI();
    }

    private void buildUI() {
        JPanel status = new JPanel(new GridLayout(1, 4));
        status.setBackground(UITheme.BG_PANEL);
        status.setBorder(new EmptyBorder(8, 20, 8, 20));
        statusLabel = statWidget(status, "🎮 STATUS", "PRESS START", UITheme.ACCENT_CYAN);
        scoreLabel  = statWidget(status, "★ SCORE",   "0",           UITheme.SUCCESS);
        winsLabel   = statWidget(status, "✔ WINS",    "0",           new Color(50,220,100));
        lossLabel   = statWidget(status, "✘ LOSSES",  "0",           UITheme.DANGER);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UITheme.BG_DARK);
        top.add(topBar("❌  TIC-TAC-TOE vs AI", new Color(255,160,50)), BorderLayout.NORTH);
        top.add(status, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // ── Board ────────────────────────────────────────────────────────────
        JPanel boardWrap = new JPanel(new GridBagLayout());
        boardWrap.setBackground(UITheme.BG_DARK);

        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 8, 8));
        boardPanel.setBackground(new Color(30, 20, 10));
        boardPanel.setBorder(BorderFactory.createLineBorder(new Color(255,160,50), 2));
        boardPanel.setPreferredSize(new Dimension(390, 390));

        for (int i = 0; i < 9; i++) {
            JButton btn = new JButton();
            btn.setFont(new Font("Monospaced", Font.BOLD, 64));
            btn.setBackground(UITheme.BG_CARD);
            btn.setForeground(X_COLOR);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            int idx = i;
            btn.addActionListener(e -> playerMove(idx));
            cells[i] = btn;
            boardPanel.add(btn);
        }
        boardWrap.add(boardPanel);
        add(boardWrap, BorderLayout.CENTER);

        // ── South ────────────────────────────────────────────────────────────
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        south.setBackground(UITheme.BG_PANEL);

        JButton startBtn = new JButton("▶  NEW GAME");
        UITheme.styleButton(startBtn, new Color(255,160,50), Color.BLACK);
        startBtn.addActionListener(e -> startGame());

        JLabel hint = new JLabel("You are  ✕  |  AI is  ○");
        hint.setFont(UITheme.FONT_BODY);
        hint.setForeground(UITheme.TEXT_DIM);

        south.add(startBtn);
        south.add(hint);
        add(south, BorderLayout.SOUTH);
    }

    @Override public void onShown() { startGame(); }

    private void startGame() {
        board = new int[9];
        playerTurn = true;
        gameActive = true;
        for (JButton btn : cells) {
            btn.setText("");
            btn.setBackground(UITheme.BG_CARD);
            btn.setEnabled(true);
        }
        statusLabel.setText("YOUR TURN  ✕");
        scoreLabel.setText(String.valueOf(scoreManager.getCurrentScore()));
    }

    private void playerMove(int idx) {
        if (!gameActive || !playerTurn || board[idx] != 0) return;
        board[idx] = 1;
        cells[idx].setText("✕");
        cells[idx].setForeground(X_COLOR);
        cells[idx].setEnabled(false);

        if (checkWin(1)) { endGame("YOU WIN! 🎉", true); return; }
        if (isDraw())    { endGame("DRAW! 🤝", false);  return; }

        playerTurn = false;
        statusLabel.setText("AI THINKING...");

        // Small delay so AI feels "alive"
        javax.swing.Timer delay = new javax.swing.Timer(500, e -> {
            aiMove();
            if (checkWin(2)) { endGame("AI WINS! 🤖", false); return; }
            if (isDraw())    { endGame("DRAW! 🤝", false);    return; }
            playerTurn = true;
            statusLabel.setText("YOUR TURN  ✕");
        });
        delay.setRepeats(false);
        delay.start();
    }

    private void aiMove() {
        int level = scoreManager.getLevel();
        int best = -1;

        if (level >= 2) best = findBestMove(); // minimax for medium/hard
        if (best == -1 || (level == 1 && new Random().nextBoolean())) {
            // Random fallback for easy
            java.util.List<Integer> empty = new java.util.ArrayList<>();
            for (int i = 0; i < 9; i++) if (board[i] == 0) empty.add(i);
            if (!empty.isEmpty()) best = empty.get(new Random().nextInt(empty.size()));
        }

        if (best >= 0) {
            board[best] = 2;
            cells[best].setText("○");
            cells[best].setForeground(O_COLOR);
            cells[best].setEnabled(false);
        }
    }

    // ── Minimax ──────────────────────────────────────────────────────────────
    private int findBestMove() {
        int bestVal = Integer.MIN_VALUE, bestMove = -1;
        for (int i = 0; i < 9; i++) {
            if (board[i] == 0) {
                board[i] = 2;
                int val = minimax(0, false);
                board[i] = 0;
                if (val > bestVal) { bestVal = val; bestMove = i; }
            }
        }
        return bestMove;
    }

    private int minimax(int depth, boolean isMax) {
        if (checkWin(2)) return 10 - depth;
        if (checkWin(1)) return depth - 10;
        if (isDraw())    return 0;

        if (isMax) {
            int best = Integer.MIN_VALUE;
            for (int i = 0; i < 9; i++) {
                if (board[i] == 0) { board[i] = 2; best = Math.max(best, minimax(depth+1, false)); board[i] = 0; }
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int i = 0; i < 9; i++) {
                if (board[i] == 0) { board[i] = 1; best = Math.min(best, minimax(depth+1, true)); board[i] = 0; }
            }
            return best;
        }
    }

    private boolean checkWin(int player) {
        for (int[] w : WINS)
            if (board[w[0]] == player && board[w[1]] == player && board[w[2]] == player) {
                if (gameActive) highlightWin(w);
                return true;
            }
        return false;
    }

    private void highlightWin(int[] line) {
        for (int i : line) cells[i].setBackground(new Color(0, 50, 25));
    }

    private boolean isDraw() {
        for (int v : board) if (v == 0) return false;
        return true;
    }

    private void endGame(String msg, boolean playerWon) {
        gameActive = false;
        for (JButton btn : cells) btn.setEnabled(false);
        statusLabel.setText(msg);

        if (playerWon) {
            wins++;
            winsLabel.setText(String.valueOf(wins));
            int pts = scoreManager.getLevel() * 100;
            scoreManager.addPoints(pts);
            scoreManager.saveHighScore("TicTacToe");
            scoreLabel.setText(String.valueOf(scoreManager.getCurrentScore()));
        } else if (msg.contains("DRAW")) {
            draws++;
            scoreManager.addPoints(20);
            scoreLabel.setText(String.valueOf(scoreManager.getCurrentScore()));
        } else {
            losses++;
            lossLabel.setText(String.valueOf(losses));
        }
    }
}