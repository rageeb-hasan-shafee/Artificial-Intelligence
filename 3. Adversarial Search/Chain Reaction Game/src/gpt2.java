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
    private JPanel[][] panels;
    private boolean redTurn = true; // Red starts first
    private Stack<GameState> historyStack = new Stack<>(); // Stack to hold game states for undo functionality

    // Constructor to initialize the Game UI
    public ChainReaction(String gameMode) {
        frame = new JFrame("Chain Reaction Game");
        frame.setLayout(new GridLayout(BOARD_ROWS, BOARD_COLS));
        panels = new JPanel[BOARD_ROWS][BOARD_COLS];

        // Initialize the board with panels for each cell
        for (int i = 0; i < BOARD_ROWS; i++) {
            for (int j = 0; j < BOARD_COLS; j++) {
                panels[i][j] = new JPanel();
                panels[i][j].setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                panels[i][j].setBackground(Color.WHITE);  // Initial background is white
                panels[i][j].addMouseListener(new CellClickListener(i, j)); // Use mouse listener to capture clicks
                frame.add(panels[i][j]);
            }
        }

        // Push initial game state onto the stack (before any moves)
        historyStack.push(new GameState(board, redTurn));

        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // Action listener for user click on the board (Human vs AI)
    private class CellClickListener extends MouseAdapter {
        private int row;
        private int col;

        public CellClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (board[row][col] == 0 || board[row][col] == (redTurn ? 1 : -1)) { // Cell is empty or already occupied by current player
                board[row][col] += redTurn ? 1 : -1; // Increment orb count (Red = +1, Blue = -1)
                processExplosions(board);
                updateBoard();
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

    // Repaint the board (update after each move)
    private void updateBoard() {
        for (int i = 0; i < BOARD_ROWS; i++) {
            for (int j = 0; j < BOARD_COLS; j++) {
                panels[i][j].removeAll(); // Clear previous balls
                int orbCount = Math.abs(board[i][j]);
                Color orbColor = (board[i][j] > 0) ? Color.RED : Color.BLUE;

                // Draw the balls inside the cells
                for (int k = 0; k < orbCount; k++) {
                    int x = CELL_SIZE / 2 + (k % 2) * 15; // Slight offset for stacking
                    int y = CELL_SIZE / 2 + (k / 2) * 15;
                    panels[i][j].add(new Ball(x, y, orbColor)); // Add a new ball at the given position
                }
                panels[i][j].revalidate();
                panels[i][j].repaint();
            }
        }
    }

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

    // Ball class to represent balls inside the cells
    class Ball extends JComponent {
        private int x, y;
        private Color color;

        public Ball(int x, int y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
            setBounds(x, y, 10, 10); // Set size of the ball
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(color);
            g.fillOval(0, 0, 10, 10); // Draw a filled circle
        }
    }

    // Handle Human vs AI mode
    public void HumanVsAI() {
        System.out.println("Game Mode: Human vs AI");
        while (!isGameOver(board)) {
            if (redTurn) {
                System.out.println("Human's turn (Red). Waiting for move...");
                continue;
            }

            else {
                System.out.println("AI's turn (Blue). Making a move...");
                MinimaxAI blueAI = new MinimaxAI();
                int[] move = blueAI.getBestMove(board);  // AI makes its move
                board[move[0]][move[1]] = -1;  // Blue's move
                processMove();  // Process the move (check explosions, etc.)
                updateBoard();  // Display board after move
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
            if (redTurn) {
                System.out.println("Red AI's turn. Making a move...");
                int[] move = redAI.getBestMove(board); // Get the best move for Red
                board[move[0]][move[1]] = 1;  // Red's move
                processMove();  // Process the move
                updateBoard();  // Display board after move
            }
            else {
                System.out.println("Blue AI's turn. Making a move...");
                int[] move = blueAI.getBestMove(board); // Get the best move for Blue
                board[move[0]][move[1]] = -1;  // Blue's move
                processMove();  // Process the move
                updateBoard();  // Display board after move
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
