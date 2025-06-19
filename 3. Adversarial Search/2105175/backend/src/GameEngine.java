import java.io.*;
import java.nio.file.*;
import java.util.*;

public class GameEngine {
    public static final String GAME_STATE_FILE = "/media/rageeb-hasan-shafee/New Volume/3-1/CSE 318/3. Adversarial Search/2105175/shared/gamestate.txt";
    private static final int ROWS = 9;
    private static final int COLS = 6;
    private static final int MAX_DEPTH = 4;
    // private static final long MOVE_TIMEOUT = 5000; // 5 seconds
    private static final long FILE_CHECK_INTERVAL = 100; // 100ms
    
    private GameState gameState;
    private MinimaxAgent agent;
    private boolean isRunning;
    private String lastFileContent;
    private boolean aiVsAiMode;
    
    public GameEngine(boolean aiVsAiMode) {
        this.gameState = new GameState(ROWS, COLS);
        this.agent = new MinimaxAgent();
        this.isRunning = true;
        this.lastFileContent = "";
        this.aiVsAiMode = aiVsAiMode;
        
        // try {
        //     Files.createDirectories(Paths.get("shared"));
        // } catch (IOException e) {
        //     System.err.println("Failed to create shared directory: " + e.getMessage());
        // }
    }
    
    public void run() {
        System.out.println("=== Chain Reaction Game Engine Started ===");
        System.out.println("Waiting for human moves from UI...");
        System.out.println("File path: " + new File(GAME_STATE_FILE).getAbsolutePath());
        
        while (isRunning) {
            try {
                if (waitForHumanMove()) {
                    System.out.println("\n--- Processing Human Move ---");
                    processHumanMove();
                    
                    if (!gameState.isGameOver()) {
                        System.out.println("\n--- Making AI Move ---");
                        makeAIMove();
                        checkGameEnd();
                    } else {
                        handleGameEnd();
                        break;
                    }
                }
                Thread.sleep(FILE_CHECK_INTERVAL);
            } catch (Exception e) {
                System.err.println("Error in game loop: " + e.getMessage());
                e.printStackTrace();
                try {
                    Thread.sleep(1000); // Wait before retrying
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }
        
        System.out.println("Game Engine shutting down...");
    }
    
    private boolean waitForHumanMove() {
        try {
            Path path = Paths.get(GAME_STATE_FILE);
            if (!Files.exists(path)) {
                return false;
            }
            
            String content = Files.readString(path);
            
            if (!content.equals(lastFileContent) && content.startsWith("Human Move:")) {
                lastFileContent = content;
                return true;
            }
            
        } catch (IOException e) {

        }
        
        return false;
    }
    
    private void processHumanMove() {
        try {
            Path path = Paths.get(GAME_STATE_FILE);
            String content = Files.readString(path);
            
            String[] lines = content.split("\n");
            if (lines.length < ROWS + 1) {
                System.err.println("Invalid game state format - expected " + (ROWS + 1) + " lines, got " + lines.length);
                return;
            }

            int[][] board = new int[ROWS][COLS];
            char[][] colors = new char[ROWS][COLS];
            
            System.out.println("Parsing game state:");
            for (int i = 0; i < ROWS; i++) {
                if (i + 1 < lines.length) {
                    String[] cells = lines[i + 1].trim().split("\\s+");
                    System.out.println("Row " + i + ": " + Arrays.toString(cells));
                    
                    for (int j = 0; j < COLS; j++) {
                        if (j < cells.length) {
                            String cell = cells[j];
                            if (cell.equals("0")) {
                                board[i][j] = 0;
                                colors[i][j] = ' ';
                            } else {
                                try {
                                    board[i][j] = Integer.parseInt(cell.substring(0, cell.length() - 1));
                                    colors[i][j] = cell.charAt(cell.length() - 1);
                                } catch (Exception e) {
                                    System.err.println("Error parsing cell " + cell + " at (" + i + "," + j + ")");
                                    board[i][j] = 0;
                                    colors[i][j] = ' ';
                                }
                            }
                        } else {
                            board[i][j] = 0;
                            colors[i][j] = ' ';
                        }
                    }
                }
            }
            
            gameState.setBoard(board, colors);
            System.out.println("Human move processed successfully");
            gameState.printBoard();
            printGameStats();
            
        } catch (Exception e) {
            System.err.println("Error processing human move: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /*private void makeAIMove() {
        System.out.println("AI analyzing position...");
        long startTime = System.currentTimeMillis();
        
        List<Move> legalMoves = gameState.getLegalMoves('B');
        System.out.println("AI has " + legalMoves.size() + " legal moves");
        
        if (legalMoves.isEmpty()) {
            System.err.println("No legal moves available for AI!");
            return;
        }
        
        Move bestMove = agent.findBestMove(gameState, MAX_DEPTH);
        
        long endTime = System.currentTimeMillis();
        System.out.println("AI decision took " + (endTime - startTime) + "ms");
        
        if (bestMove != null) {
            System.out.println("AI chooses move: " + bestMove);
            boolean success = gameState.makeMove(bestMove.row, bestMove.col, 'B');
            
            if (success) {
                System.out.println("AI move executed successfully");
                gameState.printBoard();
                printGameStats();
                writeGameState();
            } else {
                
                System.err.println("Failed to execute AI move!");
            }
        } else {
            System.err.println("No valid AI move found!");
            bestMove = legalMoves.get(0); 
            gameState.makeMove(bestMove.row, bestMove.col, 'B');
            writeGameState();
        }
    }*/
    private void makeAIMove() {
        try {
            System.out.println("AI analyzing position...");
            long startTime = System.currentTimeMillis();
            
            // Get current AI player (Blue)
            char aiPlayer = 'B';
            List<Move> legalMoves = gameState.getLegalMoves(aiPlayer);
            System.out.println("AI has " + legalMoves.size() + " legal moves");
            
            // Early exit if no moves available
            if (legalMoves.isEmpty()) {
                System.err.println("No legal moves available for AI!");
                return;
            }
            
            // Get best move with timeout protection
            Move bestMove = null;
            try {
                bestMove = agent.findBestMove(gameState, MAX_DEPTH);
                System.out.println("AI decision took " + (System.currentTimeMillis() - startTime) + "ms");
            } catch (Exception e) {
                System.err.println("Error finding best move: " + e.getMessage());
            }
            
            // Execute move with validation
            if (bestMove != null && gameState.isValidMove(bestMove.row, bestMove.col, aiPlayer)) {
                System.out.println("AI chooses move: (" + bestMove.row + "," + bestMove.col + ")");
                
                // Try to make the move
                boolean success = false;
                try {
                    success = gameState.makeMove(bestMove.row, bestMove.col, aiPlayer);
                } catch (Exception e) {
                    System.err.println("Error executing move: " + e.getMessage());
                }
                
                if (success) {
                    System.out.println("AI move executed successfully");
                    gameState.printBoard();
                    printGameStats();
                    writeGameState();
                    
                    // Add delay for UI update
                    Thread.sleep(500);
                } else {
                    System.err.println("Failed to execute AI move - trying fallback");
                    // Try fallback to first legal move
                    if (!legalMoves.isEmpty()) {
                        Move fallback = legalMoves.get(0);
                        if (gameState.makeMove(fallback.row, fallback.col, aiPlayer)) {
                            System.out.println("Fallback move executed");
                            gameState.printBoard();
                            printGameStats();
                            writeGameState();
                        }
                    }
                }
            } else {
                System.err.println("No valid move found - using random legal move");
                // Use random legal move as last resort
                if (!legalMoves.isEmpty()) {
                    int randomIndex = (int)(Math.random() * legalMoves.size());
                    Move randomMove = legalMoves.get(randomIndex);
                    if (gameState.makeMove(randomMove.row, randomMove.col, aiPlayer)) {
                        System.out.println("Random move executed");
                        gameState.printBoard();
                        printGameStats();
                        writeGameState();
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Critical error in makeAIMove: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void writeGameState() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("AI Move:\n"); 
            
            int[][] board = gameState.getBoard();
            char[][] colors = gameState.getColors();
            
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    if (board[i][j] == 0) {
                        sb.append("0");
                    } else {
                        sb.append(board[i][j]).append(colors[i][j]);
                    }
                    if (j < COLS - 1) sb.append(" ");
                }
                sb.append("\n");
            }
            
            Path tempPath = Paths.get(GAME_STATE_FILE + ".tmp");
            Files.writeString(tempPath, sb.toString());
            Files.move(tempPath, Paths.get(GAME_STATE_FILE), StandardCopyOption.REPLACE_EXISTING);
            
            lastFileContent = sb.toString();
            
        } catch (IOException e) {
            System.err.println("Error writing game state: " + e.getMessage());
        }
    }

    private void writeAIMove(char player) {
        try {
            Thread.sleep(500);
            
            StringBuilder sb = new StringBuilder();
            sb.append("AI ").append(player).append(" Move:\n");
            
            int[][] board = gameState.getBoard();
            char[][] colors = gameState.getColors();
            
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    if (board[i][j] == 0) {
                        sb.append("0");
                    } else {
                        sb.append(board[i][j]).append(colors[i][j]);
                    }
                    if (j < COLS - 1) sb.append(" ");
                }
                sb.append("\n");
            }
            
            // Write atomically using temp file
            Path tempPath = Paths.get(GAME_STATE_FILE + ".tmp");
            Files.writeString(tempPath, sb.toString());
            Files.move(tempPath, Paths.get(GAME_STATE_FILE), StandardCopyOption.REPLACE_EXISTING);
            
            lastFileContent = sb.toString();
            
            // Ensure UI has time to process
            Thread.sleep(500);
            
        } catch (Exception e) {
            System.err.println("Error writing game state: " + e.getMessage());
        }
    }
    
    private void checkGameEnd() {
        if (gameState.isGameOver()) {
            handleGameEnd();
            isRunning = false;
        }
    }
    
    private void handleGameEnd() {
        char winner = gameState.getWinner();
        System.out.println("\n=== GAME OVER ===");
        
        if (winner == 'B') {
            System.out.println("🤖 AI (Blue) WINS! 🤖");
        } else if (winner == 'R') {
            System.out.println("🏆 HUMAN (Red) WINS! 🏆");
        } else {
            System.out.println("🤝 GAME ENDED IN A DRAW! 🤝");
        }
        
        gameState.printBoard();
        printFinalStats();
        isRunning = false;
    }
    
    private void printGameStats() {
        int redOrbs = gameState.getOrbCount('R');
        int blueOrbs = gameState.getOrbCount('B');
        int redCells = gameState.getCellCount('R');
        int blueCells = gameState.getCellCount('B');
        
        System.out.println("--- Game Statistics ---");
        System.out.println("Red (Human):  " + redOrbs + " orbs, " + redCells + " cells");
        System.out.println("Blue (AI):    " + blueOrbs + " orbs, " + blueCells + " cells");
        System.out.println("----------------------");
    }
    
    private void printFinalStats() {
        int redOrbs = gameState.getOrbCount('R');
        int blueOrbs = gameState.getOrbCount('B');
        int redCells = gameState.getCellCount('R');
        int blueCells = gameState.getCellCount('B');
        
        System.out.println("\n=== Final Statistics ===");
        System.out.println("Red (Human):  " + redOrbs + " orbs in " + redCells + " cells");
        System.out.println("Blue (AI):    " + blueOrbs + " orbs in " + blueCells + " cells");
        
        double redControlScore = gameState.getControlScore('R');
        double blueControlScore = gameState.getControlScore('B');
        int redTerritoryScore = gameState.getTerritorialScore('R');
        int blueTerritoryScore = gameState.getTerritorialScore('B');
        
        System.out.println("Control Score - Red: " + String.format("%.2f", redControlScore) + 
                          ", Blue: " + String.format("%.2f", blueControlScore));
        System.out.println("Territory Score - Red: " + redTerritoryScore + ", Blue: " + blueTerritoryScore);
        System.out.println("========================");
    }
        
    // public static void main(String[] args) {
    //     System.out.println("Chain Reaction Game Engine");
    //     System.out.println("1. Human vs AI");
    //     System.out.println("2. AI vs AI (Single Match)");
        
    //     Scanner scanner = new Scanner(System.in);
    //     System.out.print("Enter choice (1-2): ");
    //     int choice = scanner.nextInt();
        
    //     switch (choice) {
    //         case 1:
    //             GameEngine engine = new GameEngine(false);
    //             engine.run();
    //             break;
                
    //         case 2:
    //             runSingleAIMatch();
    //             break;
                
    //         default:
    //             System.out.println("Invalid choice. Exiting...");
    //     }
        
    //     scanner.close();
    // }

    public static void main(String[] args) {
        System.out.println("Chain Reaction Game Engine");
        System.out.println("1. Human vs AI");
        System.out.println("2. AI vs AI (Single Match)");
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter choice (1-2): ");
        int choice = scanner.nextInt();
        
        switch (choice) {
            case 1:
                System.out.println("\nSelect AI heuristic strategy:");
                printHeuristicMenu();
                int heuristic = scanner.nextInt();
                System.out.print("Enter AI depth (2-4): ");
                int depth = scanner.nextInt();
                
                System.out.println("\nStarting Human vs AI match...");
                System.out.println("AI Strategy: " + getHeuristicName(heuristic) + " (Depth " + depth + ")");
                
                GameEngine engine = new GameEngine(false);
                engine.agent = new MinimaxAgent(new HeuristicEvaluator(heuristic));
                engine.run();
                break;
                
            case 2:
                runSingleAIMatch();
                break;
                
            default:
                System.out.println("Invalid choice. Exiting...");
        }
        
        scanner.close();
    }

    private static String getHeuristicName(int choice) {
        return switch (choice) {
            case 1 -> "Orb Count";
            case 2 -> "Cell Control";
            case 3 -> "Position Control";
            case 4 -> "Stability";
            case 5 -> "Territory";
            case 6 -> "Threat";
            case 7 -> "Mobility";
            default -> "Default";
        };
    }

    private static void runSingleAIMatch() {
        Scanner scanner = new Scanner(System.in);
    
        System.out.println("\nSelect first AI heuristic:");
        printHeuristicMenu();
        int heuristic1 = scanner.nextInt();
        System.out.print("Enter depth for first AI (2-4): ");
        int depth1 = scanner.nextInt();
        
        System.out.println("\nSelect second AI heuristic:");
        printHeuristicMenu();
        int heuristic2 = scanner.nextInt();
        System.out.print("Enter depth for second AI (2-4): ");
        int depth2 = scanner.nextInt();
    
        AIMatch match = new AIMatch(
            new MinimaxAgent(new HeuristicEvaluator(heuristic1)),
            depth1,
            new MinimaxAgent(new HeuristicEvaluator(heuristic2)),
            depth2
        );
        
        System.out.println("\nStarting AI vs AI match...");
        System.out.println("Blue AI: " + getHeuristicName(heuristic1) + " (Depth " + depth1 + ")");
        System.out.println("Red AI: " + getHeuristicName(heuristic2) + " (Depth " + depth2 + ")");
    
        match.runMatch();
    }
    
    private static void printHeuristicMenu() {
        System.out.println("1. Orb Count - Simple count of orbs");
        System.out.println("2. Cell Control - Number of cells occupied");
        System.out.println("3. Position Control - Control of strategic positions");
        System.out.println("4. Stability - Defensive positioning");
        System.out.println("5. Territory - Connected regions control");
        System.out.println("6. Threat - Immediate explosion potential");
        System.out.println("7. Mobility - Available moves count");
    }
    
}