// import java.util.Random;

public class HeuristicEvaluator {
    //private static final Random rand = new Random();
    private int selectedHeuristic;
    
    public HeuristicEvaluator(int heuristicChoice) {
        this.selectedHeuristic = heuristicChoice;
    }
    
    public HeuristicEvaluator() {
        this(1); 
    }

    private static double getCornerControlBonus(int[][] board, char[][] colors, char player, int rows, int cols) {
                double bonus = 0;
                
                // Check all four corners
                int[][] corners = {{0,0}, {0,cols-1}, {rows-1,0}, {rows-1,cols-1}};
                
                for (int[] corner : corners) {
                    int r = corner[0];
                    int c = corner[1];
                    
                    if (colors[r][c] == player && board[r][c] > 0) {
                        bonus += 8; // High bonus for controlling corners early
                    }
                }
                
                return bonus;
            }
    
    // private double criticalCellHeuristic(GameState state, char player) {
    //     double redCritical = 0, blueCritical = 0;
    //     int[][] board = state.getBoard();
    //     char[][] colors = state.getColors();
        
    //     for (int i = 0; i < state.getRows(); i++) {
    //         for (int j = 0; j < state.getCols(); j++) {
    //             if (board[i][j] > 0) {
    //                 int criticalMass = state.getCriticalMass(i, j);
    //                 double dangerLevel = (double) board[i][j] / criticalMass;
                    
    //                 if (colors[i][j] == 'R') {
    //                     redCritical += dangerLevel;
    //                 } else if (colors[i][j] == 'B') {
    //                     blueCritical += dangerLevel;
    //                 }
    //             }
    //         }
    //     }
        
    //     return (redCritical - blueCritical) * 15;
    // }

    public double evaluate(GameState state, char player) {
        if (state.isGameOver()) {
            char winner = state.getWinner();
            if (winner == player) return 10000.0;
            if (winner != ' ' && winner != player) return -10000.0;
            return 0.0;
        }
        
        char opponent = (player == 'R') ? 'B' : 'R';
        
        return switch (selectedHeuristic) {
            case 1 -> orbCountHeuristic(state, player, opponent);
            case 2 -> cellControlHeuristic(state, player, opponent);
            case 3 -> positionControlHeuristic(state, player, opponent);
            case 4 -> stabilityHeuristic(state, player, opponent);
            case 5 -> territoryHeuristic(state, player, opponent);
            case 6 -> threatHeuristic(state, player, opponent);
            case 7 -> mobilityHeuristic(state, player, opponent);
            default -> orbCountHeuristic(state, player, opponent);
        };
    }
    

    private double orbCountHeuristic(GameState state, char player, char opponent) {
        int playerOrbs = state.getOrbCount(player);
        int opponentOrbs = state.getOrbCount(opponent);
        
        if (playerOrbs + opponentOrbs == 0) return 0;
        
        double orbDifference = (double)(playerOrbs - opponentOrbs) / (playerOrbs + opponentOrbs);
        
        double orbPositionBonus = 0;
        char[][] colors = state.getColors();
        
        int centerX = state.getRows() / 2;
        int centerY = state.getCols() / 2;
        int proximityFactor = 3; 
        
        for (int i = Math.max(0, centerX - 2); i <= Math.min(state.getRows() - 1, centerX + 2); i++) {
            for (int j = Math.max(0, centerY - 2); j <= Math.min(state.getCols() - 1, centerY + 2); j++) {
                if (colors[i][j] == player) orbPositionBonus += proximityFactor;
                if (colors[i][j] == opponent) orbPositionBonus -= proximityFactor;
            }
        }
        
        double edgeBonus = 0;
        for (int i = 0; i < state.getRows(); i++) {
            if (colors[i][0] == player || colors[i][state.getCols() - 1] == player) edgeBonus += 0.3;
            if (colors[i][0] == opponent || colors[i][state.getCols() - 1] == opponent) edgeBonus -= 0.3;
        }
        for (int j = 0; j < state.getCols(); j++) {
            if (colors[0][j] == player || colors[state.getRows() - 1][j] == player) edgeBonus += 0.3;
            if (colors[0][j] == opponent || colors[state.getRows() - 1][j] == opponent) edgeBonus -= 0.3;
        }
        
        return orbDifference + orbPositionBonus + edgeBonus;
    }
    
    
    private double cellControlHeuristic(GameState state, char player, char opponent) {
        // int playerCells = state.getCellCount(player);
        // int opponentCells = state.getCellCount(opponent);
        
        // if (playerCells + opponentCells == 0) return 0;
        
        // double centralBonus = 0;
        // char[][] colors = state.getColors();
        // int centerX = state.getRows() / 2;
        // int centerY = state.getCols() / 2;
        
        // if (colors[centerX][centerY] == player) centralBonus += 0.4;
        // if (colors[centerX - 1][centerY] == player) centralBonus += 0.3;
        // if (colors[centerX + 1][centerY] == player) centralBonus += 0.3;
        // if (colors[centerX][centerY - 1] == player) centralBonus += 0.3;
        // if (colors[centerX][centerY + 1] == player) centralBonus += 0.3;
        
        // double edgeBonus = 0;
        // for (int i = 0; i < state.getRows(); i++) {
        //     if (colors[i][0] == player) edgeBonus += 0.1;
        //     if (colors[i][state.getCols() - 1] == player) edgeBonus += 0.1;
        // }
        // for (int j = 0; j < state.getCols(); j++) {
        //     if (colors[0][j] == player) edgeBonus += 0.1;
        //     if (colors[state.getRows() - 1][j] == player) edgeBonus += 0.1;
        // }
        
        // return ((double)(playerCells - opponentCells) / (playerCells + opponentCells)) + centralBonus + edgeBonus;
        double redCritical = 0, blueCritical = 0;
        int[][] board = state.getBoard();
        char[][] colors = state.getColors();
        
        for (int i = 0; i < state.getRows(); i++) {
            for (int j = 0; j < state.getCols(); j++) {
                if (board[i][j] > 0) {
                    int criticalMass = state.getCriticalMass(i, j);
                    double dangerLevel = (double) board[i][j] / criticalMass;
                    
                    if (colors[i][j] == 'R') {
                        redCritical += dangerLevel;
                    } else if (colors[i][j] == 'B') {
                        blueCritical += dangerLevel;
                    }
                }
            }
        }
        
        return (redCritical - blueCritical) * 15;
    }
    

    private double positionControlHeuristic(GameState state, char player, char opponent) {
        double playerControl = state.getControlScore(player);
        double opponentControl = state.getControlScore(opponent);
        
        if (playerControl + opponentControl == 0) return 0;
        
        double edgeControlBonus = 0;
        char[][] colors = state.getColors();
        
        for (int i = 0; i < state.getRows(); i++) {
            if (colors[i][0] == player) edgeControlBonus += 0.2;
            if (colors[i][state.getCols() - 1] == player) edgeControlBonus += 0.2;
        }
        for (int j = 0; j < state.getCols(); j++) {
            if (colors[0][j] == player) edgeControlBonus += 0.2;
            if (colors[state.getRows() - 1][j] == player) edgeControlBonus += 0.2;
        }
    
        return (playerControl - opponentControl) / (playerControl + opponentControl) + edgeControlBonus;
    }


     private double stabilityHeuristic(GameState state, char player, char opponent) {
        int playerStability = state.getStabilityScore(player);
        int opponentStability = state.getStabilityScore(opponent);
        
        if (playerStability + opponentStability == 0) return 0;
        
        // Consider positions near the edges to be more stable
        double edgeStabilityBonus = 0;
        char[][] colors = state.getColors();
        
        // If a player has pieces on the edge, it's more stable
        for (int i = 0; i < state.getRows(); i++) {
            if (colors[i][0] == player || colors[i][state.getCols() - 1] == player) edgeStabilityBonus += 0.2;
        }
        for (int j = 0; j < state.getCols(); j++) {
            if (colors[0][j] == player || colors[state.getRows() - 1][j] == player) edgeStabilityBonus += 0.2;
        }
    
        return (double)(playerStability - opponentStability) / (playerStability + opponentStability) + edgeStabilityBonus;
    }
    
    private double territoryHeuristic(GameState state, char player, char opponent) {
        int playerTerritory = state.getTerritorialScore(player);
        int opponentTerritory = state.getTerritorialScore(opponent);
        
        if (playerTerritory + opponentTerritory == 0) return 0;
        
        return (double)(playerTerritory - opponentTerritory) / (playerTerritory + opponentTerritory);
    }

    private double threatHeuristic(GameState state, char player, char opponent) {
        double playerThreats = calculateThreats(state, player);
        double opponentThreats = calculateThreats(state, opponent);
        
        // The higher the threat score, the better
        return playerThreats - opponentThreats;
    }
    
    private double calculateThreats(GameState state, char player) {
        double threats = 0;
        int[][] board = state.getBoard();
        char[][] colors = state.getColors();
        
        for (int i = 0; i < state.getRows(); i++) {
            for (int j = 0; j < state.getCols(); j++) {
                if (colors[i][j] == player && board[i][j] > 0) {
                    int criticalMass = getCriticalMass(i, j, state);
                    int currentOrbs = board[i][j];
                    
                    if (currentOrbs == criticalMass - 1) {
                        threats += 5.0;
                    } else if (currentOrbs == criticalMass - 2) {
                        threats += 2.0;
                    }
                    
                    if (criticalMass == 2) threats += 0.5; // Corner
                    else if (criticalMass == 3) threats += 0.3; // Edge
                }
            }
        }
        
        return threats;
    }
    
    private double mobilityHeuristic(GameState state, char player, char opponent) {
        int playerMoves = state.getLegalMoves(player).size();
        int opponentMoves = state.getLegalMoves(opponent).size();
        
        if (playerMoves + opponentMoves == 0) return 0;
        
        return (double)(playerMoves - opponentMoves) / (playerMoves + opponentMoves);
    }
    
    private int getCriticalMass(int row, int col, GameState state) {
        int neighbors = 0;
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, 1, 0, -1};
        
        for (int i = 0; i < 4; i++) {
            int nr = row + dx[i];
            int nc = col + dy[i];
            if (nr >= 0 && nr < state.getRows() && nc >= 0 && nc < state.getCols()) {
                neighbors++;
            }
        }
        return neighbors;
    }
}