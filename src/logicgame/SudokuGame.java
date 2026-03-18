package logicgame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class SudokuGame extends GamePanel {
    private final JTextField[][] cells = new JTextField[9][9];
    private int[][] puzzle, solution;
    private JLabel timerLabel, scoreLabel, errLabel;
    private GameTimer timer;
    private int errors = 0;

    private static final int[][] EASY = {
            {5,3,0,0,7,0,0,0,0},{6,0,0,1,9,5,0,0,0},{0,9,8,0,0,0,0,6,0},
            {8,0,0,0,6,0,0,0,3},{4,0,0,8,0,3,0,0,1},{7,0,0,0,2,0,0,0,6},
            {0,6,0,0,0,0,2,8,0},{0,0,0,4,1,9,0,0,5},{0,0,0,0,8,0,0,7,9}
    };
    private static final int[][] EASY_SOL = {
            {5,3,4,6,7,8,9,1,2},{6,7,2,1,9,5,3,4,8},{1,9,8,3,4,2,5,6,7},
            {8,5,9,7,6,1,4,2,3},{4,2,6,8,5,3,7,9,1},{7,1,3,9,2,4,8,5,6},
            {9,6,1,5,3,7,2,8,4},{2,8,7,4,1,9,6,3,5},{3,4,5,2,8,6,1,7,9}
    };
    private static final int[][] HARD = {
            {0,0,0,2,6,0,7,0,1},{6,8,0,0,7,0,0,9,0},{1,9,0,0,0,4,5,0,0},
            {8,2,0,1,0,0,0,4,0},{0,0,4,6,0,2,9,0,0},{0,5,0,0,0,3,0,2,8},
            {0,0,9,3,0,0,0,7,4},{0,4,0,0,5,0,0,3,6},{7,0,3,0,1,8,0,0,0}
    };
    private static final int[][] HARD_SOL = {
            {4,3,5,2,6,9,7,8,1},{6,8,2,5,7,1,4,9,3},{1,9,7,8,3,4,5,6,2},
            {8,2,6,1,9,5,3,4,7},{3,7,4,6,8,2,9,1,5},{9,5,1,7,4,3,6,2,8},
            {5,1,9,3,2,6,8,7,4},{2,4,8,9,5,7,1,3,6},{7,6,3,4,1,8,2,5,9}
    };

    public SudokuGame(GameWindow window) {
        super(window);
        buildUI();
    }

    private void buildUI() {
        JPanel status = new JPanel(new GridLayout(1, 3));
        status.setBackground(UITheme.BG_PANEL);
        status.setBorder(new EmptyBorder(8, 20, 8, 20));
        timerLabel = statWidget(status, "⏱ TIME",    "00:00", UITheme.ACCENT_CYAN);
        scoreLabel = statWidget(status, "★ SCORE",   "0",     UITheme.SUCCESS);
        errLabel   = statWidget(status, "⚠ ERRORS",  "0",     UITheme.DANGER);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UITheme.BG_DARK);
        top.add(topBar("🔲  SUDOKU", UITheme.ACCENT_PINK), BorderLayout.NORTH);
        top.add(status, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // Grid
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UITheme.BG_DARK);

        JPanel grid = new JPanel(new GridLayout(9, 9, 2, 2));
        grid.setBackground(new Color(30, 0, 60));
        grid.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT_PINK, 2));
        grid.setPreferredSize(new Dimension(468, 468));

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                JTextField tf = new JTextField(1);
                tf.setFont(UITheme.FONT_NUM);
                tf.setHorizontalAlignment(JTextField.CENTER);
                tf.setBackground(UITheme.BG_CARD);
                tf.setForeground(UITheme.TEXT_PRIMARY);
                tf.setCaretColor(UITheme.ACCENT_PINK);
                tf.setSelectionColor(UITheme.ACCENT_PURPLE);

                // Bold lines for 3x3 boxes
                int top2  = (r % 3 == 0) ? 3 : 0;
                int left2 = (c % 3 == 0) ? 3 : 0;
                tf.setBorder(BorderFactory.createMatteBorder(
                        top2, left2, 0, 0, new Color(180, 50, 220)));

                int row = r, col = c;
                tf.addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) {
                        if (tf.isEditable()) tf.setBackground(new Color(40, 10, 60));
                    }
                    public void focusLost(FocusEvent e) {
                        if (tf.isEditable()) tf.setBackground(UITheme.BG_CARD);
                    }
                });
                tf.addKeyListener(new KeyAdapter() {
                    public void keyReleased(KeyEvent e) { validateCell(row, col, tf); }
                });

                cells[r][c] = tf;
                grid.add(tf);
            }
        }
        wrapper.add(grid);
        add(wrapper, BorderLayout.CENTER);

        // South buttons
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        south.setBackground(UITheme.BG_PANEL);

        JButton newGame = new JButton("⟳  NEW GAME");
        UITheme.styleButton(newGame, UITheme.BG_CARD, UITheme.TEXT_DIM);
        newGame.addActionListener(e -> startNewGame());

        JButton hint = new JButton("💡 HINT (−50 pts)");
        UITheme.styleButton(hint, UITheme.BG_CARD, UITheme.WARNING);
        hint.addActionListener(e -> giveHint());

        JButton check = new JButton("✔ CHECK");
        UITheme.styleButton(check, UITheme.BG_CARD, UITheme.SUCCESS);
        check.addActionListener(e -> checkAll());

        south.add(newGame); south.add(hint); south.add(check);
        add(south, BorderLayout.SOUTH);
    }

    @Override
    public void onShown() { startNewGame(); }

    private void startNewGame() {
        errors = 0;
        errLabel.setText("0");
        int level = scoreManager.getLevel();
        puzzle   = deepCopy(level <= 2 ? EASY : HARD);
        solution = level <= 2 ? EASY_SOL : HARD_SOL;

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                JTextField tf = cells[r][c];
                tf.setBackground(UITheme.BG_CARD);
                tf.setForeground(UITheme.TEXT_PRIMARY);
                if (puzzle[r][c] != 0) {
                    tf.setText(String.valueOf(puzzle[r][c]));
                    tf.setEditable(false);
                    tf.setForeground(UITheme.ACCENT_PINK);
                    tf.setBackground(new Color(20, 5, 35));
                } else {
                    tf.setText("");
                    tf.setEditable(true);
                    tf.setForeground(UITheme.ACCENT_CYAN);
                }
            }
        }
        scoreLabel.setText(String.valueOf(scoreManager.getCurrentScore()));

        if (timer != null) timer.stop();
        timer = new GameTimer(0, () -> timerLabel.setText(timer.formatTime()), null);
        timer.start();
    }

    private void validateCell(int r, int c, JTextField tf) {
        String txt = tf.getText().trim();
        if (txt.isEmpty()) { tf.setBackground(new Color(40, 10, 60)); return; }
        int val;
        try { val = Integer.parseInt(txt); } catch (NumberFormatException e) {
            tf.setText(""); return;
        }
        if (val < 1 || val > 9) { tf.setText(""); return; }

        if (val == solution[r][c]) {
            tf.setForeground(UITheme.SUCCESS);
            tf.setBackground(new Color(0, 40, 20));
            puzzle[r][c] = val;
            if (isSolved()) endGame();
        } else {
            tf.setForeground(UITheme.DANGER);
            tf.setBackground(new Color(50, 5, 5));
            errors++;
            errLabel.setText(String.valueOf(errors));
        }
    }

    private void giveHint() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (cells[r][c].isEditable() && cells[r][c].getText().isEmpty()) {
                    cells[r][c].setText(String.valueOf(solution[r][c]));
                    cells[r][c].setForeground(UITheme.WARNING);
                    cells[r][c].setBackground(new Color(40, 30, 0));
                    puzzle[r][c] = solution[r][c];
                    scoreManager.addPoints(-50);
                    scoreLabel.setText(String.valueOf(scoreManager.getCurrentScore()));
                    if (isSolved()) endGame();
                    return;
                }
            }
        }
    }

    private void checkAll() {
        boolean allGood = true;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (cells[r][c].isEditable()) {
                    String t = cells[r][c].getText().trim();
                    if (t.isEmpty()) { allGood = false; continue; }
                    try {
                        int v = Integer.parseInt(t);
                        if (v != solution[r][c]) allGood = false;
                    } catch (NumberFormatException e) { allGood = false; }
                }
            }
        }
        showMessage(allGood ? "✔ Looking Good!" : "⚠ Errors Found",
                allGood ? "No mistakes so far! Keep going." : "Some cells are incorrect.",
                allGood ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    private boolean isSolved() {
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                if (puzzle[r][c] != solution[r][c]) return false;
        return true;
    }

    private void endGame() {
        timer.stop();
        int bonus = timer.getScoreBonus() + Math.max(0, 500 - errors * 30);
        scoreManager.addPoints(bonus);
        scoreManager.saveHighScore("Sudoku");
        scoreLabel.setText(String.valueOf(scoreManager.getCurrentScore()));
        showMessage("🏆 SOLVED!",
                "Puzzle complete!\nErrors: " + errors + "\nTime: " + timer.formatTime() +
                        "\n\n+" + bonus + " points!", JOptionPane.INFORMATION_MESSAGE);
    }

    private int[][] deepCopy(int[][] src) {
        int[][] copy = new int[9][9];
        for (int i = 0; i < 9; i++) copy[i] = src[i].clone();
        return copy;
    }
}