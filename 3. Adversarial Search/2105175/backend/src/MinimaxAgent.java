import java.util.Collections;
import java.util.List;

public class MinimaxAgent {
    private final HeuristicEvaluator evaluator;
    private static final int EARLY_GAME_MOVES = 6;
    private static final double RANDOMIZATION_FACTOR = 0.1;
    private static final double EARLY_GAME_EVAL_FACTOR = 0.8;
    
    public MinimaxAgent() {
        this.evaluator = new HeuristicEvaluator();
    }
    
    public MinimaxAgent(HeuristicEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    // public Move findBestMove(GameState state, int maxDepth) {
    //     Move bestMove = null;
    //     double bestValue = Double.NEGATIVE_INFINITY;
    //     char player = 'B';  
        
    //     for (Move move : state.getLegalMoves(player)) {
    //         GameState nextState = new GameState(state);
    //         nextState.makeMove(move.row, move.col, player);
            
    //         double value = minimax(nextState, maxDepth - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);
            
    //         if (value > bestValue) {
    //             bestValue = value;
    //             bestMove = move;
    //         }
    //     }
        
    //     return bestMove;
    // }
    public Move findBestMove(GameState state, int maxDepth) {
        Move bestMove = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        char player = state.getCurrentPlayer();  // Get current player instead of hardcoding 'B'
        
        List<Move> legalMoves = state.getLegalMoves(player);
        
        // Early game strategy with limited depth
        if (state.getMoveCount() < EARLY_GAME_MOVES) {
            maxDepth = Math.min(maxDepth, 2); // Limit depth in early game
            // Prioritize strategic positions in early game
            for (Move move : legalMoves) {
                if (isStrategicPosition(move, state.getRows(), state.getCols())) {
                    GameState nextState = new GameState(state);
                    if (nextState.makeMove(move.row, move.col, player)) {
                        return move;
                    }
                }
            }
        }
    
        // Add some randomization for move variety
        if (Math.random() < 0.2) {
            Collections.shuffle(legalMoves);
        }
    
        for (Move move : legalMoves) {
            GameState nextState = new GameState(state);
            if (nextState.makeMove(move.row, move.col, player)) {
                double value = minimax(nextState, maxDepth - 1, 
                                    Double.NEGATIVE_INFINITY, 
                                    Double.POSITIVE_INFINITY, 
                                    false);
                
                if (isStrategicPosition(move, state.getRows(), state.getCols())) {
                    value *= 1.2; 
                }
                
                if (value > bestValue) {
                    bestValue = value;
                    bestMove = move;
                }
            }
        }
        
        return bestMove != null ? bestMove : 
               !legalMoves.isEmpty() ? legalMoves.get((int)(Math.random() * legalMoves.size())) : 
               null;
    }
    
    // public Move findBestMove(GameState state, int maxDepth) {
    //     Move bestMove = null;
    //     double bestValue = Double.NEGATIVE_INFINITY;
    //     char player = 'B';  // AI plays as Blue
        
    //     List<Move> legalMoves = state.getLegalMoves(player);
        
    //     // Early game strategy with limited depth
    //     if (state.getMoveCount() < EARLY_GAME_MOVES) {
    //         maxDepth = Math.min(maxDepth, 2); // Limit depth in early game
    //         for (Move move : legalMoves) {
    //             if (isStrategicPosition(move, state.getRows(), state.getCols())) {
    //                 GameState nextState = new GameState(state);
    //                 if (nextState.makeMove(move.row, move.col, player)) {
    //                     return move;
    //                 }
    //             }
    //         }
    //     }

    //     // Randomize move order for variety while maintaining strategic evaluation
    //     if (Math.random() < 0.3) {
    //         Collections.shuffle(legalMoves);
    //     }

    //     for (Move move : legalMoves) {
    //         GameState nextState = new GameState(state);
    //         if (nextState.makeMove(move.row, move.col, player)) {
    //             double value = minimax(nextState, maxDepth - 1, 
    //                                 Double.NEGATIVE_INFINITY, 
    //                                 Double.POSITIVE_INFINITY, 
    //                                 false);
                
    //             // Add strategic position bonus
    //             if (isStrategicPosition(move, state.getRows(), state.getCols())) {
    //                 value *= 1.2; // 20% bonus for strategic positions
    //             }
                
    //             // Small random factor for similar-valued moves
    //             if (Math.abs(value - bestValue) < 0.1) {
    //                 value += Math.random() * RANDOMIZATION_FACTOR;
    //             }
                
    //             if (value > bestValue) {
    //                 bestValue = value;
    //                 bestMove = move;
    //             }
    //         }
    //     }

    //     return bestMove != null ? bestMove : 
    //            !legalMoves.isEmpty() ? legalMoves.get((int)(Math.random() * legalMoves.size())) : 
    //            null;
    // }
    private double minimax(GameState state, int depth, double alpha, double beta, boolean isMax) {
        // Terminal conditions
        if (depth == 0 || state.isGameOver()) {
            char player = state.getCurrentPlayer();
            return evaluator.evaluate(state, player);  
        }    
    
        char player = state.getCurrentPlayer();
        List<Move> moves = state.getLegalMoves(player);
        
        // Add randomization for move ordering
        if (Math.random() < 0.2) {
            Collections.shuffle(moves);
        }
        
        if (isMax) {
            double maxValue = Double.NEGATIVE_INFINITY;
            for (Move move : moves) {
                GameState nextState = new GameState(state);
                if (nextState.makeMove(move.row, move.col, player)) {
                    double value = minimax(nextState, depth - 1, alpha, beta, false);
                    maxValue = Math.max(maxValue, value);
                    alpha = Math.max(alpha, value);
                    if (beta <= alpha) break;  // Beta cut-off
                }
            }
            return maxValue;
        } else {
            double minValue = Double.POSITIVE_INFINITY;
            for (Move move : moves) {
                GameState nextState = new GameState(state);
                if (nextState.makeMove(move.row, move.col, player)) {
                    double value = minimax(nextState, depth - 1, alpha, beta, true);
                    minValue = Math.min(minValue, value);
                    beta = Math.min(beta, value);
                    if (beta <= alpha) break;  // Alpha cut-off
                }
            }
            return minValue;
        }
    }
    // private double minimax(GameState state, int depth, double alpha, double beta, boolean isMax) {
    //     //Terminal conditions
    //     // if (depth == 0 || state.isGameOver()) {
    //     //     double evaluation = evaluator.evaluate(state, 'B');
            
    //     //     // Apply early game modifier
    //     //     if (state.getMoveCount() < EARLY_GAME_MOVES) {
    //     //         evaluation *= EARLY_GAME_EVAL_FACTOR;
                
    //     //         // Add positional bonuses in early game
    //     //         for (int i = 0; i < state.getRows(); i++) {
    //     //             for (int j = 0; j < state.getCols(); j++) {
    //     //                 if (state.getColors()[i][j] == 'B' && 
    //     //                     isStrategicPosition(new Move(i, j), state.getRows(), state.getCols())) {
    //     //                     evaluation += 0.2; // Small bonus for strategic positions
    //     //                 }
    //     //             }
    //     //         }
    //     //     }
            
    //     //     return evaluation;
    //     // }
    //     //Terminal conditions
    //     if (depth == 0 || state.isGameOver()) {
    //         return evaluator.evaluate(state, 'B');  
    //     }    

    //     char player = isMax ? 'B' : 'R';
    //     List<Move> moves = state.getLegalMoves(player);
        
    //     if (Math.random() < 0.2) {
    //         Collections.shuffle(moves);
    //     }
        
    //     if (isMax) {
    //         double maxValue = Double.NEGATIVE_INFINITY;
    //         for (Move move : moves) {
    //             GameState nextState = new GameState(state);
    //             if (nextState.makeMove(move.row, move.col, player)) {
    //                 double value = minimax(nextState, depth - 1, alpha, beta, false);
    //                 maxValue = Math.max(maxValue, value);
    //                 alpha = Math.max(alpha, value);
    //                 if (beta <= alpha) break;  // Beta cut-off
    //             }
    //         }
    //         return maxValue;
    //     } else {
    //         double minValue = Double.POSITIVE_INFINITY;
    //         for (Move move : moves) {
    //             GameState nextState = new GameState(state);
    //             if (nextState.makeMove(move.row, move.col, player)) {
    //                 double value = minimax(nextState, depth - 1, alpha, beta, true);
    //                 minValue = Math.min(minValue, value);
    //                 beta = Math.min(beta, value);
    //                 if (beta <= alpha) break;  // Alpha cut-off
    //             }
    //         }
    //         return minValue;
    //     }
    // }
    
    private boolean isStrategicPosition(Move move, int rows, int cols) {
        if ((move.row == 0 || move.row == rows-1) && 
            (move.col == 0 || move.col == cols-1)) {
            return true;
        }
        
        if ((move.row == rows/2 || move.row == (rows-1)/2) && 
            (move.col == cols/2 || move.col == (cols-1)/2)) {
            return true;
        }
        
        return false;
    }
}

// import java.util.Collections;
// import java.util.List;

// public class MinimaxAgent {
//     private final HeuristicEvaluator evaluator;
//     private static final int EARLY_GAME_MOVES = 6;
//     private static final double RANDOMIZATION_FACTOR = 0.1;
    
//     public MinimaxAgent() {
//         this.evaluator = new HeuristicEvaluator();
//     }
    
//     public MinimaxAgent(HeuristicEvaluator evaluator) {
//         this.evaluator = evaluator;
//     }

//     public Move findBestMove(GameState state, int maxDepth, char player) {
//         Move bestMove = null;
//         double bestValue = Double.NEGATIVE_INFINITY;
        
//         List<Move> legalMoves = getLegalMovesForPlayer(state, player);
        
//         if (legalMoves.isEmpty()) {
//             System.out.println("No legal moves available for player " + player);
//             return null;
//         }
        
//         System.out.println("Player " + player + " has " + legalMoves.size() + " legal moves");
        
//         // Early game strategy with limited depth
//         if (state.getMoveCount() < EARLY_GAME_MOVES) {
//             maxDepth = Math.min(maxDepth, 2); // Limit depth in early game
//             for (Move move : legalMoves) {
//                 if (isStrategicPosition(move, state.getRows(), state.getCols())) {
//                     GameState nextState = new GameState(state);
//                     if (nextState.makeMove(move.row, move.col, player)) {
//                         System.out.println("Early game strategic move selected: (" + move.row + "," + move.col + ")");
//                         return move;
//                     }
//                 }
//             }
//         }
    
//         if (Math.random() < 0.2) {
//             Collections.shuffle(legalMoves);
//         }
    
//         for (Move move : legalMoves) {
//             GameState nextState = new GameState(state);
//             if (nextState.makeMove(move.row, move.col, player)) {
//                 char opponent = (player == 'B') ? 'R' : 'B';
//                 double value = minimax(nextState, maxDepth - 1, 
//                                     Double.NEGATIVE_INFINITY, 
//                                     Double.POSITIVE_INFINITY, 
//                                     false, player, opponent);
                
//                 if (isStrategicPosition(move, state.getRows(), state.getCols())) {
//                     value *= 1.1; 
//                 }
                
//                 if (value > bestValue) {
//                     bestValue = value;
//                     bestMove = move;
//                 }
//             }
//         }
        
//         if (bestMove == null && !legalMoves.isEmpty()) {
//             bestMove = legalMoves.get((int)(Math.random() * legalMoves.size()));
//             System.out.println("Using fallback random move: (" + bestMove.row + "," + bestMove.col + ")");
//         }
        
//         return bestMove;
//     }
    
//     public Move findBestMove(GameState state, int maxDepth) {
//         return findBestMove(state, maxDepth, 'B'); 
//     }
    
//     private List<Move> getLegalMovesForPlayer(GameState state, char player) {
//         List<Move> moves = new java.util.ArrayList<>();
//         int[][] board = state.getBoard();
//         char[][] colors = state.getColors();
        
//         for (int i = 0; i < state.getRows(); i++) {
//             for (int j = 0; j < state.getCols(); j++) {
//                 if (board[i][j] == 0 || colors[i][j] == player) {
//                     moves.add(new Move(i, j));
//                 }
//             }
//         }
        
//         return moves;
//     }
    
//     private double minimax(GameState state, int depth, double alpha, double beta, 
//                           boolean isMaximizing, char maxPlayer, char minPlayer) {
//         if (depth == 0 || state.isGameOver()) {
//             return evaluator.evaluate(state, maxPlayer);  
//         }    
    
//         char currentPlayer = isMaximizing ? maxPlayer : minPlayer;
//         List<Move> moves = getLegalMovesForPlayer(state, currentPlayer);
        
//         if (moves.isEmpty()) {
//             return evaluator.evaluate(state, maxPlayer);
//         }
        
//         if (Math.random() < 0.15) {
//             Collections.shuffle(moves);
//         }
        
//         if (isMaximizing) {
//             double maxValue = Double.NEGATIVE_INFINITY;
//             for (Move move : moves) {
//                 GameState nextState = new GameState(state);
//                 if (nextState.makeMove(move.row, move.col, currentPlayer)) {
//                     double value = minimax(nextState, depth - 1, alpha, beta, false, maxPlayer, minPlayer);
//                     maxValue = Math.max(maxValue, value);
//                     alpha = Math.max(alpha, value);
//                     if (beta <= alpha) break;  // Beta cut-off
//                 }
//             }
//             return maxValue;
//         } else {
//             double minValue = Double.POSITIVE_INFINITY;
//             for (Move move : moves) {
//                 GameState nextState = new GameState(state);
//                 if (nextState.makeMove(move.row, move.col, currentPlayer)) {
//                     double value = minimax(nextState, depth - 1, alpha, beta, true, maxPlayer, minPlayer);
//                     minValue = Math.min(minValue, value);
//                     beta = Math.min(beta, value);
//                     if (beta <= alpha) break;  // Alpha cut-off
//                 }
//             }
//             return minValue;
//         }
//     }
    
//     private boolean isStrategicPosition(Move move, int rows, int cols) {
//         if ((move.row == 0 || move.row == rows-1) && 
//             (move.col == 0 || move.col == cols-1)) {
//             return true;
//         }
        
//         if ((move.row == rows/2 || move.row == (rows-1)/2) && 
//             (move.col == cols/2 || move.col == (cols-1)/2)) {
//             return true;
//         }
        
//         if ((move.row == 0 || move.row == rows-1) && 
//             (move.col == cols/2 || move.col == (cols-1)/2)) {
//             return true;
//         }
        
//         if ((move.col == 0 || move.col == cols-1) && 
//             (move.row == rows/2 || move.row == (rows-1)/2)) {
//             return true;
//         }
        
//         return false;
//     }
// }