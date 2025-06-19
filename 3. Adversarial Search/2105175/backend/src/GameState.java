import java.util.*;

public class GameState {
    private int rows;
    private int cols;
    private int[][] board;
    private char[][] colors;
    private int[] criticalMass;
    int moveCount;
    
    // Directions: up, right, down, left
    private static final int[] dx = {-1, 0, 1, 0};
    private static final int[] dy = {0, 1, 0, -1};
    
    public GameState(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.board = new int[rows][cols];
        this.colors = new char[rows][cols];
        this.criticalMass = new int[rows * cols];
        
        initializeCriticalMass();
        this.moveCount = 0;
        clearBoard();
    }
    
    public GameState(GameState other) {
        this.rows = other.rows;
        this.cols = other.cols;
        this.board = new int[rows][cols];
        this.colors = new char[rows][cols];
        this.criticalMass = other.criticalMass.clone();
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                this.board[i][j] = other.board[i][j];
                this.colors[i][j] = other.colors[i][j];
            }
        }
        this.moveCount = other.moveCount;
         
    }

     public boolean makeMove(int row, int col, char player) {
        if (!isValidMove(row, col, player)) {
            return false;
        }
        
        board[row][col]++;
        colors[row][col] = player;
        moveCount++;
        
        // Handle explosions
        explode();
        
        return true;
    }

    public int getMoveCount() {
        return moveCount;
    }
    
    private void initializeCriticalMass() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int neighbors = 0;
                for (int d = 0; d < 4; d++) {
                    int ni = i + dx[d];
                    int nj = j + dy[d];
                    if (isValidCell(ni, nj)) {
                        neighbors++;
                    }
                }
                criticalMass[i * cols + j] = neighbors;
            }
        }
    }
    
    private void clearBoard() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j] = 0;
                colors[i][j] = ' ';
            }
        }
    }
    
    public void setBoard(int[][] newBoard, char[][] newColors) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                this.board[i][j] = newBoard[i][j];
                this.colors[i][j] = newColors[i][j];
            }
        }
    }
    
    public boolean isValidMove(int row, int col, char player) {
        if (!isValidCell(row, col)) {
            return false;
        }
        
        // Can place on empty cell or cell with same color
        return board[row][col] == 0 || colors[row][col] == player;
    }
    
    
    private void explode() {
        Queue<Point> explosionQueue = new LinkedList<>();
        int count=0;
        // Find all cells that need to explode
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j] >= getCriticalMass(i, j)) {
                    explosionQueue.offer(new Point(i, j));
                }
            }
        }
        
        while (!explosionQueue.isEmpty()) {
            count++;
            if(count>51){
                break;
            }
            Point p = explosionQueue.poll();
            int r = p.row;
            int c = p.col;
            
            if (board[r][c] >= getCriticalMass(r, c)) {
                char explodingColor = colors[r][c];
                int orbsToDistribute = getCriticalMass(r, c);
                
                board[r][c] -= orbsToDistribute;
                if (board[r][c] == 0) {
                    colors[r][c] = ' ';
                }
                
                // Distribute orbs to neighbors
                for (int d = 0; d < 4; d++) {
                    int nr = r + dx[d];
                    int nc = c + dy[d];
                    
                    if (isValidCell(nr, nc)) {
                        board[nr][nc]++;
                        colors[nr][nc] = explodingColor; // Convert color
                        
                        // Check if neighbor now needs to explode
                        if (board[nr][nc] >= getCriticalMass(nr, nc)) {
                            explosionQueue.offer(new Point(nr, nc));
                        }
                    }
                }
            }
        }
    }
    
    public List<Move> getLegalMoves(char player) {
        List<Move> moves = new ArrayList<>();
        
        boolean hasPlayerOrbs = false;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (colors[i][j] == player) {
                    hasPlayerOrbs = true;
                    break;
                }
            }
            if (hasPlayerOrbs) break;
        }
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (isValidMove(i, j, player)) {
                    // If player has no orbs, can place anywhere empty
                    // If player has orbs, can only place on empty or own cells
                    if (!hasPlayerOrbs && board[i][j] == 0) {
                        moves.add(new Move(i, j));
                    } else if (hasPlayerOrbs && (board[i][j] == 0 || colors[i][j] == player)) {
                        moves.add(new Move(i, j));
                    }
                }
            }
        }
        
        return moves;
    }
    
    public boolean isGameOver() {
        // Require at least 2 moves before checking game over
        if (moveCount < 2) {
            return false;
        }

        Set<Character> activeColors = new HashSet<>();
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j] > 0) {
                    activeColors.add(colors[i][j]);
                }
            }
        }
        
        return activeColors.size() <= 1;
    }

    
    public char getWinner() {
        Set<Character> activeColors = new HashSet<>();
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j] > 0) {
                    activeColors.add(colors[i][j]);
                }
            }
        }
        
        if (activeColors.size() == 1) {
            return activeColors.iterator().next();
        }
        
        return ' '; // No winner yet or draw
    }
    
    public int getOrbCount(char player) {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (colors[i][j] == player) {
                    count += board[i][j];
                }
            }
        }
        return count;
    }
    
    public int getCellCount(char player) {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (colors[i][j] == player) {
                    count++;
                }
            }
        }
        return count;
    }
    
    public double getControlScore(char player) {
        double score = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (colors[i][j] == player) {
                    // Higher score for cells closer to critical mass
                    double ratio = (double) board[i][j] / getCriticalMass(i, j);
                    score += ratio * ratio;
                }
            }
        }
        return score;
    }
    
    public int getStabilityScore(char player) {
        int score = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (colors[i][j] == player) {
                    // Penalty for unstable cells (close to explosion)
                    int deficit = getCriticalMass(i, j) - board[i][j];
                    score += deficit * deficit;
                }
            }
        }
        return score;
    }
    
    public int getTerritorialScore(char player) {
        int score = 0;
        boolean[][] visited = new boolean[rows][cols];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (colors[i][j] == player && !visited[i][j]) {
                    // Count connected components
                    int territorySize = dfs(i, j, player, visited);
                    score += territorySize * territorySize; // Bonus for larger territories
                }
            }
        }
        return score;
    }
    
    private int dfs(int row, int col, char player, boolean[][] visited) {
        if (!isValidCell(row, col) || visited[row][col] || colors[row][col] != player) {
            return 0;
        }
        
        visited[row][col] = true;
        int size = 1;
        
        for (int d = 0; d < 4; d++) {
            int nr = row + dx[d];
            int nc = col + dy[d];
            size += dfs(nr, nc, player, visited);
        }
        
        return size;
    }
    
    private boolean isValidCell(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }
    
    public int getCriticalMass(int row, int col) {
        return criticalMass[row * cols + col];
    }
    
    public void printBoard() {
        System.out.println("Current Board:");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j] == 0) {
                    System.out.print("0   ");
                } else {
                    System.out.printf("%d%c  ", board[i][j], colors[i][j]);
                }
            }
            System.out.println();
        }
        System.out.println();
    }
    
    // Getters
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int[][] getBoard() { return board; }
    public char[][] getColors() { return colors; }

    public void resetGameOverState() {
        // If there's only one color but pieces are still moving/exploding,
        // we want to let the chain reactions complete
        boolean hasActiveExplosions = false;
        
        // Check for cells that could explode
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j] >= getCriticalMass(i, j)) {
                    hasActiveExplosions = true;
                    break;
                }
            }
        }
        
        // Only force continue if there are no active explosions
        if (!hasActiveExplosions) {
            // Ensure at least one empty cell exists for continued play
            boolean foundEmpty = false;
            for (int i = 0; i < rows && !foundEmpty; i++) {
                for (int j = 0; j < cols; j++) {
                    if (board[i][j] == 0) {
                        foundEmpty = true;
                        break;
                    }
                }
            }
            
            // If no empty cells, clear a random non-critical cell
            if (!foundEmpty) {
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        if (board[i][j] < getCriticalMass(i, j)) {
                            board[i][j] = 0;
                            colors[i][j] = ' ';
                            return;
                        }
                    }
                }
            }
        }
    }

    public char getCurrentPlayer() {
        // TODO Auto-generated method stub
        return (moveCount % 2 == 0) ? 'B' : 'R';
    }
    
}


