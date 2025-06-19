import java.util.ArrayList;
import java.util.List;

public class PuzzleConfig {
    private int k;
    private int moves;
    private int[][] grid;
    private PuzzleConfig previous;
    private int blankRow, blankCol;

    public PuzzleConfig(int k, int[][] grid) {
        this.k = k;
        this.moves = 0;
        this.previous = null;
        this.grid = new int[k][k];
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                this.grid[i][j] = grid[i][j];
                if (grid[i][j] == 0) {
                    blankRow = i;
                    blankCol = j;
                }
            }
        }
    }

    public PuzzleConfig(int k, int[][] grid, int moves, PuzzleConfig previous) {
        this(k, grid);
        this.moves = moves;
        this.previous = previous;
    }

    public PuzzleConfig(PuzzleConfig other) {
        this.k = other.k;
        this.moves = other.moves;
        this.previous = other.previous;
        this.blankRow = other.blankRow;
        this.blankCol = other.blankCol;
        this.grid = new int[k][k];
        for (int i = 0; i < k; i++) {
            System.arraycopy(other.grid[i], 0, this.grid[i], 0, k);
        }
    }

    public int[][] getGrid() {
        int[][] copy = new int[k][k];
        for (int i = 0; i < k; i++) {
            System.arraycopy(grid[i], 0, copy[i], 0, k);
        }
        return copy;
    }

    public int getMoves() { 
        return moves; 
    }

    public PuzzleConfig getPrevious() { 
        return previous; 
    }

    public int getBlankRow() { 
        return blankRow; 
    }

    public int getBlankCol() { 
        return blankCol; 
    }

    public int getGridSize() { 
        return k; 
    }

    public void printGrid() {
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public boolean isGoal() {
        int count = 1;
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                if (i == k-1 && j == k-1) 
                    return grid[i][j] == 0;
                if (grid[i][j] != count++) 
                    return false;
            }
        }
        return true;
    }

    public List<PuzzleConfig> getChildren() {
        List<PuzzleConfig> children = new ArrayList<>();
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        for (int dir = 0; dir < 4; dir++) {
            int newRow = blankRow + dx[dir];
            int newCol = blankCol + dy[dir];

            if (newRow >= 0 && newRow < k && newCol >= 0 && newCol < k) {
                int[][] newGrid = getGrid();
                newGrid[blankRow][blankCol] = newGrid[newRow][newCol];
                newGrid[newRow][newCol] = 0;
                children.add(new PuzzleConfig(k, newGrid, moves + 1, this));
            }
        }
        return children;
    }

    public int calculateManhattanDistance() {
        int distance = 0;
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                int value = grid[i][j];

                if (value == 0) 
                    continue;

                int targetRow = (value - 1) / k;
                int targetCol = (value - 1) % k;
                distance += Math.abs(i - targetRow) + Math.abs(j - targetCol);
            }
        }
        return distance;
    }

    public int calculateHammingDistance() {
        int distance = 0, count = 1;
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                if (grid[i][j] != 0 && grid[i][j] != count) {
                    distance++;
                }
                count++;
            }
        }
        return distance;
    }

    public int calculateEuclideanDistance() {
        double distance = 0;
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                int value = grid[i][j];

                if (value == 0) 
                    continue;

                int targetRow = (value - 1) / k;
                int targetCol = (value - 1) % k;
                distance += Math.sqrt(Math.pow(i - targetRow, 2) + Math.pow(j - targetCol, 2));
            }
        }
        return (int)Math.round(distance);
    }

    private int calculateRowConflicts() {
        int conflicts = 0;
        for (int i = 0; i < k; i++) {
            for (int col1 = 0; col1 < k - 1; col1++) {
                for (int col2 = col1 + 1; col2 < k; col2++) {
                    int val1 = grid[i][col1];
                    int val2 = grid[i][col2];

                    if (val1 != 0 && val2 != 0 && (val1 - 1) / k == i && (val2 - 1) / k == i && val1 > val2) {
                        conflicts++;
                    }
                }
            }
        }
        return conflicts;
    }

    private int calculateColumnConflicts() {
        int conflicts = 0;
        for (int j = 0; j < k; j++) {
            for (int row1 = 0; row1 < k - 1; row1++) {
                for (int row2 = row1 + 1; row2 < k; row2++) {
                    int val1 = grid[row1][j];
                    int val2 = grid[row2][j];
                    
                    if (val1 != 0 && val2 != 0 && (val1 - 1) % k == j && (val2 - 1) % k == j && val1 > val2) {
                        conflicts++;
                    }
                }
            }
        }
        return conflicts;
    }

    public int calculateLinearConflict() {
        return calculateManhattanDistance() + 2 * (calculateRowConflicts() + calculateColumnConflicts());
    }
}
