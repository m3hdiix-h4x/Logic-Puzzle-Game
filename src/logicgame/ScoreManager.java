package logicgame;

import java.util.HashMap;
import java.util.Map;

public class ScoreManager {
    private static ScoreManager instance;
    private final Map<String, Integer> highScores = new HashMap<>();
    private int currentScore = 0;
    private int level = 1;

    private ScoreManager() {
        highScores.put("NumberGuess",  0);
        highScores.put("MemoryCard",   0);
        highScores.put("Sudoku",       0);
        highScores.put("Snake",        0);   // ← ADD
        highScores.put("TicTacToe",    0);   // ← ADD
        highScores.put("Hangman",      0);   // ← ADD
        highScores.put("BrickBreaker", 0);   // ← ADD
    }

    public static ScoreManager getInstance() {
        if (instance == null) instance = new ScoreManager();
        return instance;
    }

    public void addPoints(int pts)   { currentScore += pts; }
    public void resetScore()         { currentScore = 0; }
    public int  getCurrentScore()    { return currentScore; }
    public int  getLevel()           { return level; }
    public void setLevel(int l)      { level = Math.max(1, Math.min(3, l)); }

    public int getHighScore(String game) {
        return highScores.getOrDefault(game, 0);
    }

    public void saveHighScore(String game) {
        if (currentScore > highScores.getOrDefault(game, 0))
            highScores.put(game, currentScore);
    }

    public String getLevelName() {
        return switch (level) {
            case 1 -> "EASY";
            case 2 -> "MEDIUM";
            case 3 -> "HARD";
            default -> "EASY";
        };
    }
}