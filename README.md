# ◈ Logic Puzzle Game

A complete Java Swing desktop application featuring **7 interactive games** with a unified score system, countdown timers, and 3 difficulty levels.

Built as a Java GUI project using **IntelliJ IDEA** and **Java Swing**.

---

## 🎮 Games

| Game | Description |
|------|-------------|
| 🔢 Number Guess | Guess the secret number with Higher/Lower hints. Race the clock! |
| 🃏 Memory Cards | Flip and match emoji pairs. Fewer moves = more points. |
| 🔲 Sudoku | Fill the 9×9 grid. Use hints wisely — they cost points! |
| 🐍 Snake | Classic snake arcade. Eat, grow, survive! |
| ❌ Tic-Tac-Toe | Play against a Minimax AI. Can you beat it? |
| 🔤 Hangman | Guess the hidden word before the man is hanged. |
| 🧱 Brick Breaker | Destroy all bricks with your paddle. Mouse or keyboard! |

---

## ✨ Features

- **Score System** — Global score accumulates across all games with speed & accuracy bonuses
- **Timer** — Countdown for Number Guess, count-up for Sudoku & Memory Cards
- **3 Difficulty Levels** — Easy / Medium / Hard affecting speed, tries, and AI behavior
- **High Scores** — Per-game best score saved during the session
- **Dark Sci-Fi UI** — Custom dark theme with glow borders, emoji icons, and Monospaced fonts

---

## 🗂️ Project Structure
```
LogicPuzzleGame/
├── src/
│   └── logicgame/
│       ├── Main.java             # Entry point
│       ├── GameWindow.java       # JFrame + CardLayout navigation
│       ├── GamePanel.java        # Abstract base class for all games
│       ├── HomePanel.java        # Main menu with game cards
│       ├── UITheme.java          # Colors, fonts, and shared styles
│       ├── ScoreManager.java     # Singleton score & high score manager
│       ├── GameTimer.java        # Countdown / count-up timer
│       ├── NumberGuessGame.java  # 🔢 Number Guess
│       ├── MemoryCardGame.java   # 🃏 Memory Cards
│       ├── SudokuGame.java       # 🔲 Sudoku
│       ├── SnakeGame.java        # 🐍 Snake
│       ├── TicTacToeGame.java    # ❌ Tic-Tac-Toe vs AI
│       ├── HangmanGame.java      # 🔤 Hangman
│       └── BrickBreakerGame.java # 🧱 Brick Breaker
```

---

## 🧠 Technical Highlights

- **OOP** — All games extend `GamePanel` (abstract class) for shared behavior
- **CardLayout** — Seamless screen switching without recreating panels
- **Singleton Pattern** — `ScoreManager` shares state across all games
- **Swing Timer** — Non-blocking game loops for Snake and Brick Breaker
- **Custom Painting** — `paintComponent()` override for Snake, Brick Breaker & Hangman
- **Minimax Algorithm** — Tic-Tac-Toe AI with depth-based scoring
- **AABB Collision** — Double-precision ball physics in Brick Breaker

---

## 🚀 How to Run

### Requirements
- Java **JDK 17** or higher
- IntelliJ IDEA (Community Edition is free)

### Steps

1. Clone the repository
```bash
   git clone https://github.com/YourUsername/LogicPuzzleGame.git
```
2. Open the project in **IntelliJ IDEA**
3. Go to `File → Project Structure → SDK` → select **Java 17+**
4. Open `src/logicgame/Main.java`
5. Click the green **▶ Run** button

---

## 🕹️ Controls

| Game | Controls |
|------|----------|
| Snake | Arrow Keys or WASD |
| Brick Breaker | ← → Arrow Keys or Mouse |
| Tic-Tac-Toe | Click on a cell |
| Hangman | Click letter buttons |
| Others | On-screen buttons |

---

## 📁 Tech Stack

| | |
|---|---|
| Language | Java 17 |
| UI Framework | Java Swing |
| IDE | IntelliJ IDEA |
| Build System | IntelliJ (no Maven/Gradle) |

---

## 👤 Author

Made with ❤️ as a Java GUI project.
