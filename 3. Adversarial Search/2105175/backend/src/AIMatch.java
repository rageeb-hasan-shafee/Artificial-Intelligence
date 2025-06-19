import java.nio.file.*;

public class AIMatch {
    private final GameState gameState;
    private final MinimaxAgent agent1;
    private final MinimaxAgent agent2;
    private final int agent1Depth;
    private final int agent2Depth;
    private int moveCount;
    private boolean isRunning;
    private char currentPlayer;
    private static final int MOVE_DELAY = 2000; // 2 seconds delay between moves
    private static final int MAX_MOVES = 100; // Maximum moves to prevent infinite games
    
    public AIMatch(MinimaxAgent agent1, int depth1, MinimaxAgent agent2, int depth2) {
        this.gameState = new GameState(9, 6);
        this.agent1 = agent1;
        this.agent2 = agent2;
        this.agent1Depth = depth1;
        this.agent2Depth = depth2;
        this.moveCount = 0;
        this.isRunning = true;
        this.currentPlayer = 'B'; // Blue starts first
    }
    
    public void makeAIMove() {
        try {
            // Determine which agent and depth to use based on current player
            MinimaxAgent currentAgent = (currentPlayer == 'B') ? agent1 : agent2;
            int depth = (currentPlayer == 'B') ? agent1Depth : agent2Depth;
            
            System.out.println("\n=== " + currentPlayer + " AI's Turn ===");
            System.out.println("Move " + (moveCount + 1) + " - " + currentPlayer + " AI analyzing position...");
            
            long startTime = System.currentTimeMillis();
            Move move = currentAgent.findBestMove(gameState, depth);
            long endTime = System.currentTimeMillis();
            
            if (move != null && isValidChainReactionMove(move.row, move.col, currentPlayer)) {
                boolean success = gameState.makeMove(move.row, move.col, currentPlayer);
                if (success) {
                    moveCount++;
                    System.out.println(currentPlayer + " AI placed at: (" + move.row + "," + move.col + ")");
                    System.out.println("Decision time: " + (endTime - startTime) + "ms");
                    writeGameState(currentPlayer);
                    printGameState();
                    
                    // Switch to the other player
                    currentPlayer = (currentPlayer == 'B') ? 'R' : 'B';
                } else {
                    System.err.println("Failed to execute move for " + currentPlayer);
                }
            } else {
                System.err.println("Invalid move generated for " + currentPlayer + ": " + move);
                // Try to find any valid move as fallback
                Move fallbackMove = findAnyValidMove(currentPlayer);
                if (fallbackMove != null) {
                    gameState.makeMove(fallbackMove.row, fallbackMove.col, currentPlayer);
                    moveCount++;
                    currentPlayer = (currentPlayer == 'B') ? 'R' : 'B';
                }
            }
            
            Thread.sleep(MOVE_DELAY);
            
        } catch (Exception e) {
            System.err.println("Error in AI move: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean isValidChainReactionMove(int row, int col, char player) {
        // Check bounds
        if (row < 0 || row >= gameState.getRows() || col < 0 || col >= gameState.getCols()) {
            return false;
        }
        
        int[][] board = gameState.getBoard();
        char[][] colors = gameState.getColors();
        
        // Chain Reaction rules:
        // 1. If cell is empty, player can place
        // 2. If cell belongs to the player, they can add to it
        // 3. If cell belongs to opponent, player CANNOT place there
        
        if (board[row][col] == 0) {
            // Empty cell - always valid
            return true;
        } else if (colors[row][col] == player) {
            // Player's own cell - can add to it
            return true;
        } else {
            // Opponent's cell - cannot place there
            return false;
        }
    }
    
    private Move findAnyValidMove(char player) {
        for (int i = 0; i < gameState.getRows(); i++) {
            for (int j = 0; j < gameState.getCols(); j++) {
                if (isValidChainReactionMove(i, j, player)) {
                    return new Move(i, j);
                }
            }
        }
        return null;
    }

    private void printGameState() {
        System.out.println("\nCurrent Board:");
        int[][] board = gameState.getBoard();
        char[][] colors = gameState.getColors();
        
        // Print column headers
        System.out.print("   ");
        for (int j = 0; j < board[0].length; j++) {
            System.out.print(" " + j + "  ");
        }
        System.out.println();
        
        for (int i = 0; i < board.length; i++) {
            System.out.print(i + ": ");
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == 0) {
                    System.out.print(" 0  ");
                } else {
                    System.out.print(" " + board[i][j] + colors[i][j] + " ");
                }
            }
            System.out.println();
        }
        
        System.out.println("\n--- Game Statistics ---");
        System.out.println("Blue orbs: " + gameState.getOrbCount('B') + " in " + gameState.getCellCount('B') + " cells");
        System.out.println("Red orbs:  " + gameState.getOrbCount('R') + " in " + gameState.getCellCount('R') + " cells");
        System.out.println("Move count: " + moveCount);
        System.out.println("Next player: " + currentPlayer);
        System.out.println("----------------------\n");
    }
    
    private void writeGameState(char player) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("AI ").append(player).append(" Move:\n");
            
            int[][] board = gameState.getBoard();
            char[][] colors = gameState.getColors();
            
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[0].length; j++) {
                    if (board[i][j] == 0) {
                        sb.append("0");
                    } else {
                        sb.append(board[i][j]).append(colors[i][j]);
                    }
                    if (j < board[0].length - 1) sb.append(" ");
                }
                sb.append("\n");
            }
            
            Files.writeString(Paths.get(GameEngine.GAME_STATE_FILE), sb.toString());
            Thread.sleep(500); 
            
        } catch (Exception e) {
            System.err.println("Error writing game state: " + e.getMessage());
        }
    }

    public void runMatch() {
        System.out.println("Starting AI vs AI match...");
        System.out.println("Blue AI (Player 1) depth: " + agent1Depth);
        System.out.println("Red AI (Player 2) depth: " + agent2Depth);
        System.out.println("Blue AI starts first");
        
        long startTime = System.currentTimeMillis();
        printGameState(); // Show initial empty board
        
        while (isRunning && !gameState.isGameOver() && moveCount < MAX_MOVES) {
            // Check if current player has any valid moves
            if (!hasValidMoves(currentPlayer)) {
                System.out.println(currentPlayer + " has no valid moves. Switching players...");
                currentPlayer = (currentPlayer == 'B') ? 'R' : 'B';
                if (!hasValidMoves(currentPlayer)) {
                    System.out.println("No valid moves for either player. Game ends.");
                    break;
                }
            }
            
            makeAIMove();
            
            // Check for game over conditions
            if (gameState.isGameOver()) {
                System.out.println("Game over detected!");
                break;
            }
        }
        
        if (moveCount >= MAX_MOVES) {
            System.out.println("Maximum moves reached. Game ended.");
        }
        
        long duration = System.currentTimeMillis() - startTime;
        printFinalResults(duration);
    }
    
    private boolean hasValidMoves(char player) {
        for (int i = 0; i < gameState.getRows(); i++) {
            for (int j = 0; j < gameState.getCols(); j++) {
                if (isValidChainReactionMove(i, j, player)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void printFinalResults(long duration) {
        System.out.println("\n====== Match Complete! ======");
        char winner = gameState.getWinner();
        System.out.println("Winner: " + (winner == 'B' ? "Blue (First AI)" : 
                                       winner == 'R' ? "Red (Second AI)" : "Draw"));
        System.out.println("Total Moves: " + moveCount);
        System.out.println("Duration: " + String.format("%.2f", duration / 1000.0) + " seconds");
        if (moveCount > 0) {
            System.out.println("Average time per move: " + 
                              String.format("%.2f", (duration / 1000.0) / moveCount) + " seconds");
        }
        System.out.println("\nFinal Board State:");
        printGameState();
        
        // Print detailed final statistics
        System.out.println("=== Final Statistics ===");
        System.out.println("Blue - Orbs: " + gameState.getOrbCount('B') + ", Cells: " + gameState.getCellCount('B'));
        System.out.println("Red  - Orbs: " + gameState.getOrbCount('R') + ", Cells: " + gameState.getCellCount('R'));
        System.out.println("========================");
    }
    
    public GameResult getResult() {
        return new GameResult(
            gameState.getWinner(),
            moveCount,
            gameState.getOrbCount('B'),
            gameState.getOrbCount('R')
        );
    }
    
    public static class GameResult {
        public final char winner;
        public final int moves;
        public final int blueOrbs;
        public final int redOrbs;
        
        public GameResult(char winner, int moves, int blueOrbs, int redOrbs) {
            this.winner = winner;
            this.moves = moves;
            this.blueOrbs = blueOrbs;
            this.redOrbs = redOrbs;
        }
    }
}