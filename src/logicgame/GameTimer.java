package logicgame;

import javax.swing.Timer;

public class GameTimer {
    private final Timer timer;
    private int seconds;
    private final int limit;
    private boolean running = false;

    public GameTimer(int limitSeconds, Runnable onTick, Runnable onExpire) {
        this.limit   = limitSeconds;
        this.seconds = 0;
        timer = new Timer(1000, e -> {
            seconds++;
            if (onTick != null) onTick.run();
            if (limit > 0 && seconds >= limit) {
                stop();
                if (onExpire != null) onExpire.run();
            }
        });
    }

    public void start()  { seconds = 0; running = true; timer.start(); }
    public void stop()   { running = false; timer.stop(); }
    public void reset()  { stop(); seconds = 0; }

    public int     getSeconds()   { return seconds; }
    public int     getRemaining() { return Math.max(0, limit - seconds); }
    public boolean isRunning()    { return running; }

    public String formatTime() {
        int s = (limit > 0) ? getRemaining() : seconds;
        return String.format("%02d:%02d", s / 60, s % 60);
    }

    public int getScoreBonus() {
        if (limit > 0) return getRemaining() * 10;
        if (seconds < 30)  return 500;
        if (seconds < 60)  return 300;
        if (seconds < 120) return 150;
        return 50;
    }
}