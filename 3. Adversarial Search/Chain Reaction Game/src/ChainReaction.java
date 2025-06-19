import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class ChainReaction {

    private static final int BOARD_ROWS = 9;
    private static final int BOARD_COLS = 6;
    private static final int CELL_SIZE = 50;
    private static final int[][] board = new int[BOARD_ROWS][BOARD_COLS]; // Store orb counts (positive for Red, negative for Blue)
    
    private JFrame frame;
    private JButton[][] buttons;
    private boolean redTurn = true; // Red starts first
    private Stack<GameState> historyStack = new Stack<>(); // Stack to hold game states for undo functionality

    // Constructor to initialize the Game UI
    public ChainReaction(String gameMode) {
        frame = new JFrame("Chain Reaction Game");
        frame.setLayout(new GridLayout(BOARD_ROWS, BOARD_COLS));
        buttons = new JButton[BOARD_ROWS][BOARD_COLS];

        // Initialize the board with buttons
        for (int i = 0; i < BOARD_ROWS; i++) {
            for (int j = 0; j < BOARD_COLS; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                buttons[i][j].setBackground(Color.WHITE);
                buttons[i][j].addActionListener(new CellClickListener(i, j));
                frame.add(buttons[i][j]);
            }
        }

        // Push initial game state onto the stack (before any moves)
        historyStack.push(new GameState(board, redTurn));

        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // Action listener for user click on the board (Human vs AI)
    private class CellClickListener implements ActionListener {
        private int row;
        private int col;

        public CellClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (board[row][col] == 0 || board[row][col] == (redTurn ? 1 : -1)) { // Cell is empty or already occupied by current player
                board[row][col] += redTurn ? 1 : -1; // Increment orb count (Red = +1, Blue = -1)
                buttons[row][col].setBackground(redTurn ? Color.RED : Color.BLUE); // Update cell color
                System.out.println("Cell clicked: (" + row + "," + col + ")");

                // Save the current game state to the stack
                historyStack.push(new GameState(board, redTurn));

                // Process explosions (if any)
                processExplosions(board);
                printBoard();
                redTurn = !redTurn; // Switch turns
            }
        }
    }

    // Process explosions after a move
    private void processExplosions(int[][] board) {
        boolean explosionOccurred = true;
        while (explosionOccurred) {
            explosionOccurred = false;
            for (int i = 0; i < BOARD_ROWS; i++) {
                for (int j = 0; j < BOARD_COLS; j++) {
                    if (Math.abs(board[i][j]) >= getCriticalMass(i, j)) {
                        explosionOccurred = true;
                        board = explodeCell(board, i, j);
                    }
                }
            }
        }
    }

    // Get the critical mass of a cell based on its position
    private int getCriticalMass(int row, int col) {
        if ((row == 0 || row == 8) && (col == 0 || col == 5)) {
            return 2; // Corners
        } else if (row == 0 || row == 8 || col == 0 || col == 5) {
            return 3; // Edges
        } else {
            return 4; // Interior cells
        }
    }

    // Explode a cell and distribute orbs to neighboring cells
    private int[][] explodeCell(int[][] board, int row, int col) {
        int criticalMass = getCriticalMass(row, col);
        int orbs = Math.abs(board[row][col]) - criticalMass;

        // Set the current cell to 0 (after explosion)
        board[row][col] = 0;

        // Define the neighbors (up, down, left, right)
        int[][] neighbors = {
            {row - 1, col}, {row + 1, col}, {row, col - 1}, {row, col + 1}
        };

        // Distribute orbs to the neighbors
        for (int[] neighbor : neighbors) {
            int r = neighbor[0];
            int c = neighbor[1];
            if (r >= 0 && r < 9 && c >= 0 && c < 6) {
                if (board[r][c] == 0) {
                    board[r][c] += orbs;
                } else {
                    // If the neighboring cell is occupied by the opponent, change it
                    if ((board[row][col] > 0 && board[r][c] < 0) || (board[row][col] < 0 && board[r][c] > 0)) {
                        board[r][c] += orbs;  // Add the orbs to the opponent's cell
                    }
                }
            }
        }
        return board;
    }
    

    // Print the board in the terminal (update after each move)
    private void printBoard() {
        System.out.println("\nCurrent Board State:");
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 6; j++) {
                if (board[i][j] == 0) {
                    System.out.print("0 ");
                } else {
                    System.out.print((board[i][j] == 1 ? "R" : "B") + " ");
                }
            }
            System.out.println();
        }
    }

    // Check if the game is over (all cells are filled or one player wins)
    private boolean isGameOver(int[][] board) {
        for (int i = 0; i < BOARD_ROWS; i++) {
            for (int j = 0; j < BOARD_COLS; j++) {
                if (board[i][j] == 0) { // Check for empty spaces
                    return false;
                }
            }
        }
        return true;
    }

    // Handle Human vs AI mode
    public void HumanVsAI() {
        System.out.println("Game Mode: Human vs AI");
        while (!isGameOver(board)) {
            // Human's turn (Red)
            if (redTurn) {
                System.out.println("Human's turn (Red). Waiting for move...");
                continue;
            }

            // AI's turn (Blue)
            else {
                System.out.println("AI's turn (Blue). Making a move...");
                MinimaxAI blueAI = new MinimaxAI();
                int[] move = blueAI.getBestMove(board);  // AI makes its move
                board[move[0]][move[1]] = -1;  // Blue's move
                buttons[move[0]][move[1]].setBackground(Color.BLUE);  // Update UI
                processMove();  // Process the move (check explosions, etc.)
                printBoard();  // Display board after move
                redTurn = true;  // Switch turns to Human
            }
        }
        endGame();
    }

    // Handle AI vs AI mode
    public void AIvsAI() {
        System.out.println("Game Mode: AI vs AI");
        MinimaxAI redAI = new MinimaxAI();
        MinimaxAI blueAI = new MinimaxAI();

        while (!isGameOver(board)) {
            // Red's turn (AI)
            if (redTurn) {
                System.out.println("Red AI's turn. Making a move...");
                int[] move = redAI.getBestMove(board); // Get the best move for Red
                board[move[0]][move[1]] = 1;  // Red's move
                buttons[move[0]][move[1]].setBackground(Color.RED);  // Update UI
                processMove();  // Process the move
                printBoard();  // Display board after move
            }
            // Blue's turn (AI)
            else {
                System.out.println("Blue AI's turn. Making a move...");
                int[] move = blueAI.getBestMove(board); // Get the best move for Blue
                board[move[0]][move[1]] = -1;  // Blue's move
                buttons[move[0]][move[1]].setBackground(Color.BLUE);  // Update UI
                processMove();  // Process the move
                printBoard();  // Display board after move
            }
            redTurn = !redTurn;  // Switch turns
        }
        endGame();
    }

    // End game method (Display results and finalize)
    private void endGame() {
        System.out.println("Game Over");
        if (isRedWinner(board)) {
            System.out.println("Red wins!");
        } else {
            System.out.println("Blue wins!");
        }
    }

    // Check if Red player wins (all blue cells are cleared)
    private boolean isRedWinner(int[][] board) {
        for (int i = 0; i < BOARD_ROWS; i++) {
            for (int j = 0; j < BOARD_COLS; j++) {
                if (board[i][j] == -1) {
                    return false; // If any blue cell is still present, red has not won
                }
            }
        }
        return true; // No blue cells left, red wins
    }

    // Process the current move (Human or AI) and write to the file
    private void processMove() {
        if (redTurn) {
            GameFileCommunication.writeGameState(board, "Human Move:");
        } else {
            GameFileCommunication.writeGameState(board, "AI Move:");
        }

        // Process explosions after every move
        Explosion.processExplosions(board);
    }

    public static void main(String[] args) {
        String gameMode = JOptionPane.showInputDialog("Enter game mode: \n1. Human Vs AI \n2. AI vs AI");

        if (gameMode == null || (!gameMode.equals("1") && !gameMode.equals("2"))) {
            System.out.println("Invalid Selection. Exiting game ....");
            return;
        }

        ChainReaction game = new ChainReaction(gameMode);
        if (gameMode.equals("1")) {
            game.HumanVsAI();
        } else {
            game.AIvsAI();
        }
    }
}