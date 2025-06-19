import java.util.*;

class MinimaxAI {

    private static final int MAX_DEPTH = 5; // You can adjust the depth here (3, 4, 5 for experiments)
    
    // Perform the minimax algorithm with alpha-beta pruning
    public int minimax(int[][] board, int depth, boolean isMaximizingPlayer, int alpha, int beta) {
        if (depth == 0 || isGameOver(board)) {
            return evaluateBoard(board); // Evaluate board at leaf node
        }

        if (isMaximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (int[] move : getPossibleMoves(board)) {
                int eval = minimax(board, depth - 1, false, alpha, beta);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break; // Beta cut-off
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int[] move : getPossibleMoves(board)) {
                int eval = minimax(board, depth - 1, true, alpha, beta);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break; // Alpha cut-off
            }
            return minEval;
        }
    }

    // Check if the game is over
    private boolean isGameOver(int[][] board) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 6; j++) {
                if (board[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    // Evaluate the board with multiple heuristics
    private int evaluateBoard(int[][] board) {
        int score = 0;

        // Heuristic 1: Number of Orbs (simple count)
        score += countOrbs(board, 1) - countOrbs(board, 2);

        // Heuristic 2: Control of Center
        score += controlCenter(board);

        // Heuristic 3: Clustering Orbs
        score += clusterOrbs(board);

        // Heuristic 4: Safety of Orbs (proximity to the center)
        score += safetyOfOrbs(board);

        // Heuristic 5: Critical Mass (explosions risk)
        score += criticalMassRisk(board);

        return score;
    }

    private int countOrbs(int[][] board, int color) {
        int count = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 6; j++) {
                if (board[i][j] == color) {
                    count++;
                }
            }
        }
        return count;
    }

    private int controlCenter(int[][] board) {
        int score = 0;
        int centerX = 9 / 2;
        int centerY = 6 / 2;
        if (board[centerX][centerY] == 1) score += 10;  // Red controls center
        else if (board[centerX][centerY] == 2) score -= 10;  // Blue controls center
        return score;
    }

    private int clusterOrbs(int[][] board) {
        int score = 0;
        // Heuristic 3: Clustered orbs increase score
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 6; j++) {
                if (board[i][j] != 0) {
                    // Reward clustering of same-color orbs
                    if ((i + 1 < 9 && board[i + 1][j] == board[i][j]) || (j + 1 < 6 && board[i][j + 1] == board[i][j])) {
                        score += 3;
                    }
                }
            }
        }
        return score;
    }

    private int safetyOfOrbs(int[][] board) {
        int score = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 6; j++) {
                if (board[i][j] != 0) {
                    if (Math.abs(i - 4) < 2 && Math.abs(j - 2) < 2) { // Near center cells
                        score -= 3; // Penalize orbs near the center
                    }
                }
            }
        }
        return score;
    }

    private int criticalMassRisk(int[][] board) {
        int score = 0;
        // Heuristic 5: Penalize for orbs near critical mass (explosion zones)
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 6; j++) {
                if (Math.abs(board[i][j]) >= getCriticalMass(i, j)) {
                    score -= 5; // Penalize for creating potential explosions
                }
            }
        }
        return score;
    }

    private int getCriticalMass(int row, int col) {
        if ((row == 0 || row == 8) && (col == 0 || col == 5)) return 2;  // Corner cells
        else if (row == 0 || row == 8 || col == 0 || col == 5) return 3;  // Edge cells
        else return 4; // Interior cells
    }

    private List<int[]> getPossibleMoves(int[][] board) {
        List<int[]> moves = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 6; j++) {
                if (board[i][j] == 0) {
                    moves.add(new int[]{i, j});
                }
            }
        }
        return moves;
    }

    public int[] getBestMove(int[][] board) {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = new int[2];
        for (int[] move : getPossibleMoves(board)) {
            int x = move[0];
            int y = move[1];
            board[x][y] = 1;  // Assume Red's move
            int score = minimax(board, MAX_DEPTH, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
            board[x][y] = 0;  // Undo move
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return bestMove;
    }
}
