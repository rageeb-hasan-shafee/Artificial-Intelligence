package backend;

// import java.io.*;
// import java.nio.file.*;
// import java.util.*;

// public class GameEngine {
//     private static final String GAME_STATE_FILE = "/media/rageeb-hasan-shafee/New Volume/3-1/CSE 318/3. Adversarial Search/2105175/shared/gamestate.txt";
//     private static final int ROWS = 9;
//     private static final int COLS = 6;
//     private static final int MAX_DEPTH = 4;
//     private static final long MOVE_TIMEOUT = 5000; // 5 seconds
//     private static final long FILE_CHECK_INTERVAL = 100; // 100ms
    
//     private GameState gameState;
//     private MinimaxAgent agent;
//     private boolean isRunning;
//     private String lastFileContent;
//     private boolean aiVsAiMode;
    
//     // public GameEngine() {
//     //     this.gameState = new GameState(ROWS, COLS);
//     //     this.agent = new MinimaxAgent();
//     //     this.isRunning = true;
//     //     this.lastFileContent = "";
        
//     //     // Ensure shared directory exists
//     //     try {
//     //         Files.createDirectories(Paths.get("shared"));
//     //     } catch (IOException e) {
//     //         System.err.println("Failed to create shared directory: " + e.getMessage());
//     //     }
//     // }
//     public GameEngine(boolean aiVsAiMode) {
//         this.gameState = new GameState(ROWS, COLS);
//         this.agent = new MinimaxAgent();
//         this.isRunning = true;
//         this.lastFileContent = "";
//         this.aiVsAiMode = aiVsAiMode;
        
//         try {
//             Files.createDirectories(Paths.get("shared"));
//         } catch (IOException e) {
//             System.err.println("Failed to create shared directory: " + e.getMessage());
//         }
//     }
    
//     public void run() {
//         System.out.println("=== Chain Reaction Game Engine Started ===");
//         System.out.println("Waiting for human moves from UI...");
//         System.out.println("File path: " + new File(GAME_STATE_FILE).getAbsolutePath());
        
//         while (isRunning) {
//             try {
//                 if (waitForHumanMove()) {
//                     System.out.println("\n--- Processing Human Move ---");
//                     processHumanMove();
                    
//                     if (!gameState.isGameOver()) {
//                         System.out.println("\n--- Making AI Move ---");
//                         makeAIMove();
//                         checkGameEnd();
//                     } else {
//                         handleGameEnd();
//                         break;
//                     }
//                 }
//                 Thread.sleep(FILE_CHECK_INTERVAL);
//             } catch (Exception e) {
//                 System.err.println("Error in game loop: " + e.getMessage());
//                 e.printStackTrace();
//                 try {
//                     Thread.sleep(1000); // Wait before retrying
//                 } catch (InterruptedException ie) {
//                     break;
//                 }
//             }
//         }
        
//         System.out.println("Game Engine shutting down...");
//     }
    
//     private boolean waitForHumanMove() {
//         try {
//             Path path = Paths.get(GAME_STATE_FILE);
//             if (!Files.exists(path)) {
//                 return false;
//             }
            
//             String content = Files.readString(path);
            
//             // Check if file content has changed and contains human move
//             if (!content.equals(lastFileContent) && content.startsWith("Human Move:")) {
//                 lastFileContent = content;
//                 return true;
//             }
            
//         } catch (IOException e) {
//             // File might be being written, ignore and try again
//         }
        
//         return false;
//     }
    
//     private void processHumanMove() {
//         try {
//             Path path = Paths.get(GAME_STATE_FILE);
//             String content = Files.readString(path);
            
//             String[] lines = content.split("\n");
//             if (lines.length < ROWS + 1) {
//                 System.err.println("Invalid game state format - expected " + (ROWS + 1) + " lines, got " + lines.length);
//                 return;
//             }
            
//             // Parse board state
//             int[][] board = new int[ROWS][COLS];
//             char[][] colors = new char[ROWS][COLS];
            
//             System.out.println("Parsing game state:");
//             for (int i = 0; i < ROWS; i++) {
//                 if (i + 1 < lines.length) {
//                     String[] cells = lines[i + 1].trim().split("\\s+");
//                     System.out.println("Row " + i + ": " + Arrays.toString(cells));
                    
//                     for (int j = 0; j < COLS; j++) {
//                         if (j < cells.length) {
//                             String cell = cells[j];
//                             if (cell.equals("0")) {
//                                 board[i][j] = 0;
//                                 colors[i][j] = ' ';
//                             } else {
//                                 try {
//                                     board[i][j] = Integer.parseInt(cell.substring(0, cell.length() - 1));
//                                     colors[i][j] = cell.charAt(cell.length() - 1);
//                                 } catch (Exception e) {
//                                     System.err.println("Error parsing cell " + cell + " at (" + i + "," + j + ")");
//                                     board[i][j] = 0;
//                                     colors[i][j] = ' ';
//                                 }
//                             }
//                         } else {
//                             board[i][j] = 0;
//                             colors[i][j] = ' ';
//                         }
//                     }
//                 }
//             }
            
//             gameState.setBoard(board, colors);
//             System.out.println("Human move processed successfully");
//             gameState.printBoard();
//             printGameStats();
            
//         } catch (Exception e) {
//             System.err.println("Error processing human move: " + e.getMessage());
//             e.printStackTrace();
//         }
//     }
    
//     private void makeAIMove() {
//         System.out.println("AI analyzing position...");
//         long startTime = System.currentTimeMillis();
        
//         List<Move> legalMoves = gameState.getLegalMoves('B');
//         System.out.println("AI has " + legalMoves.size() + " legal moves");
        
//         Move bestMove = agent.findBestMove(gameState, MAX_DEPTH);
        
//         long endTime = System.currentTimeMillis();
//         System.out.println("AI decision took " + (endTime - startTime) + "ms");
        
//         if (bestMove != null) {
//             System.out.println("AI chooses move: " + bestMove);
//             boolean success = gameState.makeMove(bestMove.row, bestMove.col, 'B');
            
//             if (success) {
//                 System.out.println("AI move executed successfully");
//                 gameState.printBoard();
//                 printGameStats();
//                 writeGameState();
//             } else {
//                 System.err.println("Failed to execute AI move!");
//             }
//         } else {
//             System.err.println("No valid AI move found!");
//             // This shouldn't happen, but let's handle it gracefully
//             List<Move> moves = gameState.getLegalMoves('B');
//             if (!moves.isEmpty()) {
//                 bestMove = moves.get(0); // Take first available move
//                 gameState.makeMove(bestMove.row, bestMove.col, 'B');
//                 writeGameState();
//             }
//         }
//     }
    
//     private void writeGameState() {
//         try {
//             StringBuilder sb = new StringBuilder();
//             sb.append("AI Move:\n");
            
//             int[][] board = gameState.getBoard();
//             char[][] colors = gameState.getColors();
            
//             for (int i = 0; i < ROWS; i++) {
//                 for (int j = 0; j < COLS; j++) {
//                     if (board[i][j] == 0) {
//                         sb.append("0");
//                     } else {
//                         sb.append(board[i][j]).append(colors[i][j]);
//                     }
//                     if (j < COLS - 1) sb.append(" ");
//                 }
//                 sb.append("\n");
//             }
            
//             // Write atomically using temp file to prevent race conditions
//             Path tempPath = Paths.get(GAME_STATE_FILE + ".tmp");
//             Files.writeString(tempPath, sb.toString());
//             Files.move(tempPath, Paths.get(GAME_STATE_FILE), StandardCopyOption.REPLACE_EXISTING);
            
//             // Update our last content tracker
//             lastFileContent = sb.toString();
            
//             System.out.println("Game state written to file successfully");
            
//         } catch (IOException e) {
//             System.err.println("Error writing game state: " + e.getMessage());
//             e.printStackTrace();
//         }
//     }
    
//     private void checkGameEnd() {
//         if (gameState.isGameOver()) {
//             handleGameEnd();
//             isRunning = false;
//         }
//     }
    
//     private void handleGameEnd() {
//         char winner = gameState.getWinner();
//         System.out.println("\n=== GAME OVER ===");
        
//         if (winner == 'B') {
//             System.out.println("🤖 AI (Blue) WINS! 🤖");
//         } else if (winner == 'R') {
//             System.out.println("🏆 HUMAN (Red) WINS! 🏆");
//         } else {
//             System.out.println("🤝 GAME ENDED IN A DRAW! 🤝");
//         }
        
//         gameState.printBoard();
//         printFinalStats();
//         isRunning = false;
//     }
    
//     private void printGameStats() {
//         int redOrbs = gameState.getOrbCount('R');
//         int blueOrbs = gameState.getOrbCount('B');
//         int redCells = gameState.getCellCount('R');
//         int blueCells = gameState.getCellCount('B');
        
//         System.out.println("--- Game Statistics ---");
//         System.out.println("Red (Human):  " + redOrbs + " orbs, " + redCells + " cells");
//         System.out.println("Blue (AI):    " + blueOrbs + " orbs, " + blueCells + " cells");
//         System.out.println("----------------------");
//     }
    
//     private void printFinalStats() {
//         int redOrbs = gameState.getOrbCount('R');
//         int blueOrbs = gameState.getOrbCount('B');
//         int redCells = gameState.getCellCount('R');
//         int blueCells = gameState.getCellCount('B');
        
//         System.out.println("\n=== Final Statistics ===");
//         System.out.println("Red (Human):  " + redOrbs + " orbs in " + redCells + " cells");
//         System.out.println("Blue (AI):    " + blueOrbs + " orbs in " + blueCells + " cells");
        
//         // Calculate some interesting stats
//         double redControlScore = gameState.getControlScore('R');
//         double blueControlScore = gameState.getControlScore('B');
//         int redTerritoryScore = gameState.getTerritorialScore('R');
//         int blueTerritoryScore = gameState.getTerritorialScore('B');
        
//         System.out.println("Control Score - Red: " + String.format("%.2f", redControlScore) + 
//                           ", Blue: " + String.format("%.2f", blueControlScore));
//         System.out.println("Territory Score - Red: " + redTerritoryScore + ", Blue: " + blueTerritoryScore);
//         System.out.println("========================");
//     }
    
//     // public static void main(String[] args) {
//     //     System.out.println("Chain Reaction Game Engine");
//     //     System.out.println("Backend AI with Minimax Algorithm");
//     //     System.out.println("==================================");
        
//     //     GameEngine engine = new GameEngine();
        
//     //     // Handle shutdown gracefully
//     //     Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//     //         System.out.println("\nReceived shutdown signal...");
//     //         engine.isRunning = false;
//     //     }));
        
//     //     engine.run();
//     // }
//     public static void main(String[] args) {
//         System.out.println("Chain Reaction Game Engine");
//         System.out.println("1. Human vs AI");
//         System.out.println("2. AI vs AI Experiments");
        
//         Scanner scanner = new Scanner(System.in);
//         int choice = scanner.nextInt();
        
//         if (choice == 1) {
//             GameEngine engine = new GameEngine(false);
//             engine.run();
//         } else {
//             runAIExperiments();
//         }
//     }

//     private static void runAIExperiments() {
//         System.out.println("Running AI Experiments...");
        
//         // Example experiment configurations
//         HeuristicFactory.HeuristicWeights[] heuristics = {
//             HeuristicFactory.getDefaultWeights(),
//             HeuristicFactory.getAggressiveWeights(),
//             HeuristicFactory.getDefensiveWeights(),
//             HeuristicFactory.getRandomWeights()
//         };
        
//         int[] depths = {2, 3, 4};
//         int matchesPerConfig = 5;

//         for (HeuristicFactory.HeuristicWeights h1 : heuristics) {
//             for (HeuristicFactory.HeuristicWeights h2 : heuristics) {
//                 for (int d1 : depths) {
//                     for (int d2 : depths) {
//                         if (h1 == h2 && d1 == d2) continue;
                        
//                         System.out.printf("\nTesting AI1(depth=%d) vs AI2(depth=%d)\n", d1, d2);
//                         int wins1 = 0, wins2 = 0;
//                         long totalDuration = 0;
//                         int totalMoves = 0;
                        
//                         for (int i = 0; i < matchesPerConfig; i++) {
//                             AIMatch match = new AIMatch(h1, d1, h2, d2);
//                             AIMatch.MatchResult result = match.runMatch();
                            
//                             if (result.winner == 'B') wins1++;
//                             else if (result.winner == 'R') wins2++;
                            
//                             totalDuration += result.duration;
//                             totalMoves += result.moves;
//                         }

//                          // Print results
//                         System.out.printf("Results after %d matches:\n", matchesPerConfig);
//                         System.out.printf("AI1 wins: %d (%.1f%%)\n", wins1, (wins1 * 100.0 / matchesPerConfig));
//                         System.out.printf("AI2 wins: %d (%.1f%%)\n", wins2, (wins2 * 100.0 / matchesPerConfig));
//                         System.out.printf("Draws: %d\n", matchesPerConfig - wins1 - wins2);
//                         System.out.printf("Avg. moves per game: %.1f\n", totalMoves * 1.0 / matchesPerConfig);
//                         System.out.printf("Avg. game duration: %.1fs\n", totalDuration / 1000.0 / matchesPerConfig);
//                     }
//                 }
//             }
//         }
//     }
// }
 {
    
}
