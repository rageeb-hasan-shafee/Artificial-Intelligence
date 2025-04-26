
import java.util.ArrayList;
import java.util.List;

public class Node{
    private int k;
    private int moves;
    private int[][] grid;
    private Node previous;
    private int blankRow;
    private int blankCol;

    public Node(int k, int[][] grid){
        this.k = k;
        this.moves = 0;
        this.previous = null;
        this.grid = new int[k][k];

        for(int i = 0; i < k; i++){
            for(int j = 0; j < k; j++){
                this.grid[i][j] = grid[i][j];

                if(grid[i][j] == 0){
                    blankRow = i;
                    blankCol = j;
                }
            }
        }
    }

    public Node(int k, int[][] grid, int moves, Node previous){
        this(k, grid);
        this.moves = moves;
        this.previous = previous;
    }

    public Node(Node other){
        this.k = other.k;
        this.moves = other.moves;
        this.previous = other.previous;
        this.grid = new int[k][k];

        for (int i = 0; i < k; i++) {
            System.arraycopy(other.grid[i], 0, this.grid[i], 0, k);
        }

        this.blankRow = other.blankRow;
        this.blankCol = other.blankCol;
        
    }

    public int[][] getGrid(){
        int[][] newGrid = new int[k][k];

        for(int i = 0; i < k; i++){
            System.arraycopy(grid[i], 0, newGrid[i], 0, k);
        }

        return newGrid;
    }

    public int getGridSize(){
        return k;
    }

    public int getMoves(){
        return moves;
    }

    public int getBlankRow(){
        return blankRow;
    }

    public int getBlankCol(){
        return blankCol;
    }

    public Node getPrevious(){
        return previous;
    }

    public void printGrid(){
        for(int i = 0; i < k; i++){
            for(int j = 0; j < k; j++){
                System.out.print(grid[i][j]+" ");
            }
        }
        System.out.println();
    }

    public int calculateManhattanDistance(){
        int distance = 0;

        for(int i = 0; i < k; i++){
            for(int j = 0; j < k; j++){
                int value = grid[i][j];
                if(value == 0) 
                    continue;
                int targetRow = (value - 1) / k;
                int targetCol = (value - 1) % k;
                distance += Math.abs(i - targetRow) + Math.abs(i - targetCol);    

            }
        }

        return distance;
    }

    public int calculateHammingDistance(){
        int distance = 0;
        int count = 1;

        for(int i = 0; i < k; i++){
            for(int j = 0; j < k; j++){
                if(grid[i][j] != 0 && grid[i][j] != count)
                    distance++;

                count++;    
            }
        }
        return distance;
    }

    public int calculateEuclideanDistance(){
        double distance = 0;

        for(int i = 0; i < k; i++){
            for(int j = 0; j < k; j++){
                int value=grid[i][j];
                if(value == 0) 
                    continue;

                int targetX = (value - 1) / k;
                int targetY = (value - 1) % k;
                distance += Math.sqrt(Math.pow(i - targetX, 2)) + Math.pow(j - targetY, 2); 
            }
        }

        return (int)Math.round(distance);
    }

    private int calculateRowConflicts(){
        int conflicts = 0;

        for(int i = 0; i < k; i++){
            for(int col1 = 0; col1 < (k - 1); col1++){
                for(int col2 = col1 + 1; col2 < k; col2++){
                    int val1 = grid[i][col1];
                    int val2 = grid[i][col2];

                    if(val1 != 0 && val2 !=0 && ((val1 - 1) / k == i) && ((val2 - 1) / k == i) && val1 > val2){
                        conflicts++;
                    }
                }

            }
        }

        return conflicts;
    }

    private int calculateColumnConflicts(){
        int conflicts = 0;

        for(int j = 0; j < k; j++){
            for(int row1 = 0; row1 < (k -1); row1++){
                for(int row2 = row1 + 1; row2 < k; row2++){
                    int val1 = grid[row1][j];
                    int val2 = grid[row2][j];

                    if(val1 != 0 && val2 !=0 && ((val1 - 1) / k == j) && ((val2 - 1) / k == j) && val1 > val2){
                        conflicts++;
                    }
                }
            }
        }

        return conflicts;
    }

    public int calculateLinearConflict(){
        int conflict = 0;
        conflict += calculateRowConflicts();
        conflict += calculateColumnConflicts();
        return calculateManhattanDistance() + 2 * conflict;
    }

    public boolean isEqual(Node other){
        if(other == null)
            return false;

        for(int i = 0; i < k; i++){
            for(int j = 0; j < k; j++){
                if(this.grid[i][j] != other.grid[i][j])
                    return false;
            }
        }  

        return true;  
    }

    public boolean isGoal(){
        int count = 1;

        for(int i = 0; i < k; i++){
            for(int j = 0; j < k; j++){
                if(grid[i][j] != 0 && grid[i][j] != count)
                    return false;
                count++;
            }
        }

        return true;
    }

    private Node createChild(int newRow, int newCol){
        int[][] newGrid = getGrid();

        newGrid[blankRow][blankCol] = newGrid[newRow][newCol];
        newGrid[newRow][newCol] = 0;
        return new Node(k, newGrid, moves + 1, this);
    }

    public List<Node> getChildren(){
        List<Node> children = new ArrayList<>();

        //Up
        if (blankRow > 0){
            children.add(createChild(blankRow - 1, blankCol));
        }
        //Down
        if(blankRow < k - 1){
            children.add(createChild(blankRow + 1, blankCol));
        }
        //Left
        if(blankCol > 0){
            children.add(createChild(blankRow, blankCol - 1));
        }
        //Right
        if(blankCol < k - 1){
            children.add(createChild(blankRow, blankCol + 1));
        }

        return children;
    }

}