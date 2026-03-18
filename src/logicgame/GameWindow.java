package logicgame;

import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {
    private final CardLayout cardLayout    = new CardLayout();
    private final JPanel     mainContainer = new JPanel(cardLayout);

    public static final String HOME         = "HOME";
    public static final String NUMGUESS     = "NUMGUESS";
    public static final String MEMORY       = "MEMORY";
    public static final String SUDOKU       = "SUDOKU";
    public static final String SNAKE        = "SNAKE";
    public static final String TICTACTOE    = "TICTACTOE";
    public static final String HANGMAN      = "HANGMAN";
    public static final String BRICKBREAKER = "BRICKBREAKER";

    public GameWindow() {
        setTitle("◈ LOGIC PUZZLE GAME ◈");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 700);
        setMinimumSize(new Dimension(850, 620));
        setLocationRelativeTo(null);

        mainContainer.setBackground(UITheme.BG_DARK);
        mainContainer.add(new HomePanel(this),        HOME);
        mainContainer.add(new NumberGuessGame(this),  NUMGUESS);
        mainContainer.add(new MemoryCardGame(this),   MEMORY);
        mainContainer.add(new SudokuGame(this),       SUDOKU);
        mainContainer.add(new SnakeGame(this),        SNAKE);
        mainContainer.add(new TicTacToeGame(this),    TICTACTOE);
        mainContainer.add(new HangmanGame(this),      HANGMAN);
        mainContainer.add(new BrickBreakerGame(this), BRICKBREAKER);

        add(mainContainer);
        showScreen(HOME);
    }

    public void showScreen(String name) {
        // Stop snake/brick loop when leaving
        for (Component c : mainContainer.getComponents()) {
            if (c instanceof SnakeGame sg)        sg.stopGame();
            if (c instanceof BrickBreakerGame bg) bg.stopGame();
        }
        cardLayout.show(mainContainer, name);
        for (Component c : mainContainer.getComponents()) {
            if (c instanceof GamePanel gp && c.isVisible()) {
                gp.onShown();
            }
        }
    }
}