
public class Move {
    int row, col;
    
    Move(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}