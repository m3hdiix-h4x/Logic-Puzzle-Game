package logicgame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class BrickBreakerGame extends GamePanel {
    private static final int W = 600, H = 440;
    private static final int ROWS = 5, COLS = 10;
    private static final int BRICK_W = 54, BRICK_H = 20, GAP = 4;

    private javax.swing.Timer loop;
    private boolean running = false, gameOver = false, won = false;

    // Ball
    private double bx, by, bdx, bdy;
    private static final int BALL_R = 9;

    // Paddle
    private int px, py = H - 40;
    private static final int PAD_W = 90, PAD_H = 12;

    // Bricks  [row][col] alive?
    private boolean[][] bricks;
    private Color[][]   brickColors;

    private int score = 0, lives = 3;
    private JLabel scoreLabel, livesLabel, statusLabel;
    private GameCanvas canvas;

    private static final Color[] ROW_COLORS = {
            new Color(255, 60, 100),
            new Color(255, 140, 40),
            new Color(255, 220, 40),
            new Color(60,  200, 100),
            new Color(60,  140, 255)
    };

    public BrickBreakerGame(GameWindow window) {
        super(window);
        buildUI();
    }

    private void buildUI() {
        JPanel status = new JPanel(new GridLayout(1, 3));
        status.setBackground(UITheme.BG_PANEL);
        status.setBorder(new EmptyBorder(8, 20, 8, 20));
        scoreLabel  = statWidget(status, "★ SCORE",  "0", UITheme.SUCCESS);
        livesLabel  = statWidget(status, "❤ LIVES",  "3", UITheme.DANGER);
        statusLabel = statWidget(status, "⚡ STATUS", "PRESS START", UITheme.ACCENT_CYAN);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UITheme.BG_DARK);
        top.add(topBar("🧱  BRICK BREAKER", new Color(100,200,255)), BorderLayout.NORTH);
        top.add(status, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        canvas = new GameCanvas();
        canvas.setPreferredSize(new Dimension(W, H));
        canvas.setBackground(new Color(5, 8, 20));
        canvas.setFocusable(true);
        canvas.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int speed = 28;
                if (e.getKeyCode() == KeyEvent.VK_LEFT  || e.getKeyCode() == KeyEvent.VK_A)
                    px = Math.max(0, px - speed);
                if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D)
                    px = Math.min(W - PAD_W, px + speed);
            }
        });
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                px = Math.max(0, Math.min(W - PAD_W, e.getX() - PAD_W / 2));
            }
        });

        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(UITheme.BG_DARK);
        wrap.add(canvas);
        add(wrap, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        south.setBackground(UITheme.BG_PANEL);
        JButton startBtn = new JButton("▶  START / RESTART");
        UITheme.styleButton(startBtn, new Color(100,200,255), Color.BLACK);
        startBtn.addActionListener(e -> startGame());
        JLabel hint = new JLabel("← → Arrow keys or Mouse to move paddle");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.TEXT_DIM);
        south.add(startBtn);
        south.add(hint);
        add(south, BorderLayout.SOUTH);

        loop = new javax.swing.Timer(14, e -> gameLoop());
    }

    @Override public void onShown() { canvas.requestFocusInWindow(); resetBoard(); }
    public void stopGame() { loop.stop(); running = false; }

    private void resetBoard() {
        bricks = new boolean[ROWS][COLS];
        brickColors = new Color[ROWS][COLS];
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) {
                bricks[r][c] = true;
                brickColors[r][c] = ROW_COLORS[r];
            }
        score = 0; lives = 3;
        gameOver = false; won = false; running = false;
        px = W / 2 - PAD_W / 2;
        resetBall();
        scoreLabel.setText("0");
        livesLabel.setText("3");
        statusLabel.setText("PRESS START");
        loop.stop();
        canvas.repaint();
    }

    private void resetBall() {
        bx = W / 2.0; by = py - 20;
        double angle = Math.toRadians(220 + new java.util.Random().nextInt(100));
        double speed = getSpeed();
        bdx = Math.cos(angle) * speed;
        bdy = Math.sin(angle) * speed;
        if (bdy > 0) bdy = -bdy;
    }

    private double getSpeed() {
        return switch (scoreManager.getLevel()) {
            case 1 -> 3.5;
            case 2 -> 5.0;
            case 3 -> 7.0;
            default -> 4.0;
        };
    }

    private void startGame() {
        resetBoard();
        running = true;
        statusLabel.setText("PLAYING");
        loop.start();
        canvas.requestFocusInWindow();
    }

    private void gameLoop() {
        if (!running) return;
        bx += bdx; by += bdy;

        // Wall bounces
        if (bx - BALL_R < 0)  { bx = BALL_R;      bdx = Math.abs(bdx); }
        if (bx + BALL_R > W)  { bx = W - BALL_R;  bdx = -Math.abs(bdx); }
        if (by - BALL_R < 0)  { by = BALL_R;       bdy = Math.abs(bdy); }

        // Paddle
        if (by + BALL_R >= py && by + BALL_R <= py + PAD_H
                && bx >= px && bx <= px + PAD_W) {
            double rel = (bx - (px + PAD_W / 2.0)) / (PAD_W / 2.0);
            double angle = rel * 65;
            double spd = getSpeed() * 1.02;
            bdx = Math.sin(Math.toRadians(angle)) * spd;
            bdy = -Math.abs(Math.cos(Math.toRadians(angle)) * spd);
        }

        // Ball lost
        if (by > H + 20) {
            lives--;
            livesLabel.setText(String.valueOf(lives));
            if (lives <= 0) { endGame(false); return; }
            resetBall();
        }

        // Brick collision
        int brickOffX = (W - (COLS * (BRICK_W + GAP) - GAP)) / 2;
        int brickOffY = 60;
        outer:
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (!bricks[r][c]) continue;
                int bLeft  = brickOffX + c * (BRICK_W + GAP);
                int bTop   = brickOffY + r * (BRICK_H + GAP);
                int bRight = bLeft + BRICK_W, bBottom = bTop + BRICK_H;

                if (bx + BALL_R > bLeft && bx - BALL_R < bRight
                        && by + BALL_R > bTop && by - BALL_R < bBottom) {
                    bricks[r][c] = false;
                    score += (ROWS - r) * 10;
                    scoreLabel.setText(String.valueOf(score));

                    double overlapL = (bx + BALL_R) - bLeft;
                    double overlapR = bRight - (bx - BALL_R);
                    double overlapT = (by + BALL_R) - bTop;
                    double overlapB = bBottom - (by - BALL_R);
                    double minH = Math.min(overlapL, overlapR);
                    double minV = Math.min(overlapT, overlapB);
                    if (minH < minV) bdx = -bdx; else bdy = -bdy;
                    break outer;
                }
            }
        }

        // Check win
        boolean allGone = true;
        for (boolean[] row : bricks) for (boolean b : row) if (b) { allGone = false; break; }
        if (allGone) endGame(true);

        canvas.repaint();
    }

    private void endGame(boolean playerWon) {
        running = false; won = playerWon; gameOver = true;
        loop.stop();
        int pts = playerWon ? score + lives * 200 : score;
        scoreManager.addPoints(pts);
        scoreManager.saveHighScore("BrickBreaker");
        scoreLabel.setText(String.valueOf(scoreManager.getCurrentScore()));
        statusLabel.setText(playerWon ? "YOU WIN! 🎉" : "GAME OVER 💀");
        canvas.repaint();
    }

    // ── Canvas ────────────────────────────────────────────────────────────────
    private class GameCanvas extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int brickOffX = (W - (COLS * (BRICK_W + GAP) - GAP)) / 2;
            int brickOffY = 60;

            // Bricks
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    if (!bricks[r][c]) continue;
                    int x = brickOffX + c * (BRICK_W + GAP);
                    int y = brickOffY + r * (BRICK_H + GAP);
                    g2.setColor(brickColors[r][c]);
                    g2.fillRoundRect(x, y, BRICK_W, BRICK_H, 6, 6);
                    g2.setColor(brickColors[r][c].brighter());
                    g2.drawRoundRect(x, y, BRICK_W, BRICK_H, 6, 6);
                }
            }

            // Paddle
            GradientPaint gp = new GradientPaint(px, py,
                    new Color(100, 200, 255), px + PAD_W, py, new Color(50, 100, 200));
            g2.setPaint(gp);
            g2.fillRoundRect(px, py, PAD_W, PAD_H, 8, 8);

            // Ball
            g2.setColor(Color.WHITE);
            g2.fillOval((int)(bx - BALL_R), (int)(by - BALL_R), BALL_R*2, BALL_R*2);
            g2.setColor(new Color(200, 230, 255));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval((int)(bx - BALL_R), (int)(by - BALL_R), BALL_R*2, BALL_R*2);

            // Overlay
            if (gameOver || !running) {
                g2.setColor(new Color(0,0,0,150));
                g2.fillRect(0, 0, W, H);
                if (gameOver) {
                    g2.setFont(new Font("Monospaced", Font.BOLD, 36));
                    g2.setColor(won ? UITheme.SUCCESS : UITheme.DANGER);
                    String msg = won ? "🎉  YOU WIN!" : "💀  GAME OVER";
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(msg, (W - fm.stringWidth(msg)) / 2, H/2 - 20);
                    g2.setFont(UITheme.FONT_BODY);
                    g2.setColor(UITheme.TEXT_DIM);
                    String sub = "Score: " + score + "   Press START to replay";
                    fm = g2.getFontMetrics();
                    g2.drawString(sub, (W - fm.stringWidth(sub)) / 2, H/2 + 20);
                } else {
                    g2.setFont(new Font("Monospaced", Font.BOLD, 28));
                    g2.setColor(new Color(100, 200, 255));
                    String msg = "🧱  PRESS START";
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(msg, (W - fm.stringWidth(msg)) / 2, H/2);
                }
            }
            g2.dispose();
        }
    }
}