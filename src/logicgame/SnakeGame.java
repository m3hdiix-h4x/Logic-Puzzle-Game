package logicgame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Random;

public class SnakeGame extends GamePanel {
    private static final int TILE = 20, COLS = 28, ROWS = 24;
    private static final Color SNAKE_HEAD = new Color(50, 255, 120);
    private static final Color SNAKE_BODY = new Color(30, 180, 80);
    private static final Color FOOD_COLOR = new Color(255, 80, 80);

    private final LinkedList<Point> snake = new LinkedList<>();
    private Point food;
    private int dx = 1, dy = 0;
    private int nextDx = 1, nextDy = 0;
    private boolean running = false, gameOver = false;
    private int score = 0, highScore = 0;
    private javax.swing.Timer loop;

    private JLabel scoreLabel, hiLabel, statusLabel;
    private GameCanvas canvas;

    public SnakeGame(GameWindow window) {
        super(window);
        buildUI();
    }

    private void buildUI() {
        // ── Status bar ────────────────────────────────────────────────────────
        JPanel status = new JPanel(new GridLayout(1, 3));
        status.setBackground(UITheme.BG_PANEL);
        status.setBorder(new EmptyBorder(8, 20, 8, 20));
        scoreLabel  = statWidget(status, "🍎 SCORE", "0",     new Color(50, 220, 100));
        hiLabel     = statWidget(status, "🏆 BEST",  "0",     UITheme.WARNING);
        statusLabel = statWidget(status, "⚡ STATUS", "READY", UITheme.ACCENT_CYAN);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UITheme.BG_DARK);
        top.add(topBar("🐍  SNAKE", new Color(50, 220, 100)), BorderLayout.NORTH);
        top.add(status, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // ── Canvas ────────────────────────────────────────────────────────────
        canvas = new GameCanvas();
        canvas.setPreferredSize(new Dimension(COLS * TILE, ROWS * TILE));
        canvas.setMinimumSize(new Dimension(COLS * TILE, ROWS * TILE));
        canvas.setBackground(new Color(8, 20, 12));
        canvas.setFocusable(true);
        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKey(e.getKeyCode());
            }
        });

        // Center the canvas
        JPanel canvasWrap = new JPanel(new GridBagLayout());
        canvasWrap.setBackground(UITheme.BG_DARK);
        canvasWrap.add(canvas);
        add(canvasWrap, BorderLayout.CENTER);

        // ── South ─────────────────────────────────────────────────────────────
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        south.setBackground(UITheme.BG_PANEL);

        JButton startBtn = new JButton("▶  START / RESTART");
        UITheme.styleButton(startBtn, new Color(50, 220, 100), Color.BLACK);
        startBtn.addActionListener(e -> startGame());

        JLabel hint = new JLabel("  Arrow keys or WASD to move");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.TEXT_DIM);

        south.add(startBtn);
        south.add(hint);
        add(south, BorderLayout.SOUTH);

        // ── Game loop timer ───────────────────────────────────────────────────
        loop = new javax.swing.Timer(130, e -> gameLoop());
        loop.setCoalesce(true);

        // Draw initial state
        resetBoard();
    }

    // Called every time this screen becomes visible
    @Override
    public void onShown() {
        resetBoard();
        canvas.requestFocusInWindow();
        SwingUtilities.invokeLater(() -> canvas.requestFocusInWindow());
    }

    public void stopGame() {
        loop.stop();
        running = false;
    }

    private void resetBoard() {
        loop.stop();
        snake.clear();
        snake.add(new Point(8, 12));
        snake.add(new Point(7, 12));
        snake.add(new Point(6, 12));
        dx = 1; dy = 0;
        nextDx = 1; nextDy = 0;
        score = 0;
        gameOver = false;
        running = false;
        spawnFood();
        scoreLabel.setText("0");
        statusLabel.setText("READY");
        canvas.repaint();
    }

    private void startGame() {
        resetBoard();
        loop.setDelay(getSpeed());
        running = true;
        loop.start();
        statusLabel.setText("PLAYING");
        canvas.requestFocusInWindow();
    }

    private int getSpeed() {
        return switch (scoreManager.getLevel()) {
            case 1 -> 140;
            case 2 -> 100;
            case 3 -> 65;
            default -> 130;
        };
    }

    private void spawnFood() {
        Random rnd = new Random();
        do {
            food = new Point(rnd.nextInt(COLS), rnd.nextInt(ROWS));
        } while (snake.contains(food));
    }

    private void gameLoop() {
        if (!running) return;

        dx = nextDx;
        dy = nextDy;

        Point head = snake.getFirst();
        Point next = new Point(head.x + dx, head.y + dy);

        // Wall collision
        if (next.x < 0 || next.x >= COLS || next.y < 0 || next.y >= ROWS) {
            endGame(); return;
        }
        // Self collision
        if (snake.contains(next)) {
            endGame(); return;
        }

        snake.addFirst(next);

        if (next.equals(food)) {
            score += 10;
            scoreLabel.setText(String.valueOf(score));
            spawnFood();
        } else {
            snake.removeLast();
        }

        canvas.repaint();
    }

    private void handleKey(int key) {
        switch (key) {
            case KeyEvent.VK_UP,    KeyEvent.VK_W -> { if (dy == 0) { nextDx = 0;  nextDy = -1; } }
            case KeyEvent.VK_DOWN,  KeyEvent.VK_S -> { if (dy == 0) { nextDx = 0;  nextDy =  1; } }
            case KeyEvent.VK_LEFT,  KeyEvent.VK_A -> { if (dx == 0) { nextDx = -1; nextDy =  0; } }
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> { if (dx == 0) { nextDx =  1; nextDy =  0; } }
        }
    }

    private void endGame() {
        running = false;
        gameOver = true;
        loop.stop();
        if (score > highScore) highScore = score;
        hiLabel.setText(String.valueOf(highScore));
        scoreManager.addPoints(score);
        scoreManager.saveHighScore("Snake");
        statusLabel.setText("GAME OVER");
        canvas.repaint();
    }

    // ── Inner canvas panel ─────────────────────────────────────────────────────
    private class GameCanvas extends JPanel {

        public GameCanvas() {
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int W = getWidth();
            int H = getHeight();

            // Background
            g2.setColor(new Color(8, 20, 12));
            g2.fillRect(0, 0, W, H);

            // Grid dots
            g2.setColor(new Color(18, 40, 22));
            for (int x = 0; x < COLS; x++)
                for (int y = 0; y < ROWS; y++)
                    g2.fillRect(x * TILE + TILE/2 - 1, y * TILE + TILE/2 - 1, 2, 2);

            // Food
            if (food != null) {
                g2.setColor(FOOD_COLOR);
                g2.fillOval(food.x * TILE + 3, food.y * TILE + 3, TILE - 6, TILE - 6);
                g2.setColor(new Color(255, 180, 180));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(food.x * TILE + 3, food.y * TILE + 3, TILE - 6, TILE - 6);
                // Shine
                g2.setColor(new Color(255, 255, 255, 120));
                g2.fillOval(food.x * TILE + 5, food.y * TILE + 5, 5, 5);
            }

            // Snake body
            for (int i = snake.size() - 1; i >= 1; i--) {
                Point p = snake.get(i);
                float ratio = 1f - (float) i / snake.size();
                int green = (int)(80 + ratio * 100);
                g2.setColor(new Color(20, green, 40));
                g2.fillRoundRect(p.x * TILE + 2, p.y * TILE + 2,
                        TILE - 4, TILE - 4, 6, 6);
            }

            // Snake head
            if (!snake.isEmpty()) {
                Point h = snake.getFirst();
                g2.setColor(gameOver ? new Color(180, 50, 50) : SNAKE_HEAD);
                g2.fillRoundRect(h.x * TILE + 1, h.y * TILE + 1,
                        TILE - 2, TILE - 2, 8, 8);

                // Eyes
                g2.setColor(new Color(5, 10, 5));
                int ex1 = h.x * TILE + 5,  ey1 = h.y * TILE + 5;
                int ex2 = h.x * TILE + 11, ey2 = h.y * TILE + 5;
                if (dy == -1) { ey1 = h.y*TILE+4;  ey2 = h.y*TILE+4;  ex1 = h.x*TILE+5; ex2 = h.x*TILE+11; }
                if (dy ==  1) { ey1 = h.y*TILE+12; ey2 = h.y*TILE+12; ex1 = h.x*TILE+5; ex2 = h.x*TILE+11; }
                if (dx == -1) { ex1 = h.x*TILE+4;  ex2 = h.x*TILE+4;  ey1 = h.y*TILE+5; ey2 = h.y*TILE+11; }
                g2.fillOval(ex1, ey1, 4, 4);
                g2.fillOval(ex2, ey2, 4, 4);
            }

            // ── Overlays ──────────────────────────────────────────────────────
            if (gameOver) {
                g2.setColor(new Color(0, 0, 0, 170));
                g2.fillRect(0, 0, W, H);

                g2.setFont(new Font("Monospaced", Font.BOLD, 38));
                g2.setColor(FOOD_COLOR);
                drawCentered(g2, "GAME OVER", W, H / 2 - 30);

                g2.setFont(new Font("Monospaced", Font.BOLD, 18));
                g2.setColor(UITheme.WARNING);
                drawCentered(g2, "Score: " + score, W, H / 2 + 10);

                g2.setFont(UITheme.FONT_SMALL);
                g2.setColor(UITheme.TEXT_DIM);
                drawCentered(g2, "Press START to play again", W, H / 2 + 40);
            }

            if (!running && !gameOver) {
                g2.setColor(new Color(0, 0, 0, 130));
                g2.fillRect(0, 0, W, H);

                g2.setFont(new Font("Monospaced", Font.BOLD, 28));
                g2.setColor(SNAKE_HEAD);
                drawCentered(g2, "PRESS  START  TO  PLAY", W, H / 2 - 10);

                g2.setFont(UITheme.FONT_SMALL);
                g2.setColor(UITheme.TEXT_DIM);
                drawCentered(g2, "Use Arrow Keys or WASD", W, H / 2 + 24);
            }

            g2.dispose();
        }

        private void drawCentered(Graphics2D g2, String text, int W, int y) {
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, (W - fm.stringWidth(text)) / 2, y);
        }
    }
}