import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.Scanner;

public class Node {
    private int k;
    private int moves;
    private int[][] grid;
    private Node previous;
    private int blankRow, blankCol;

    public Node(int k, int[][] grid) {
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

    public Node(int k, int[][] grid, int moves, Node previous) {
        this(k, grid);
        this.moves = moves;
        this.previous = previous;
    }

    public Node(Node other) {
        this.k = other.k;
        this.moves = other.moves;
        this.previous = other.previous;
        this.grid = new int[k][k];
        for (int i = 0; i < k; i++) {
            System.arraycopy(other.grid[i], 0, this.grid[i], 0, k);
        }
    }

    public int[][] getGrid() {
        int[][] newGrid = new int[k][k];
        for (int i = 0; i < k; i++) {
            System.arraycopy(grid[i], 0, newGrid[i], 0, k);
        }
        return newGrid;
    }

    public void printGrid() {
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                if (grid[i][j] == 0) {
                    System.out.print("* ");
                } else {
                    System.out.print(grid[i][j] + " ");
                }
            }
            System.out.println();
        }
    }

    public int calculateManhattanCost() {
        return moves + calculateManhattanDistance(k, grid);
    }

    public int calculateHammingCost() {
        return moves + calculateHammingDistance(k, grid);
    }

    public boolean isEqual(Node other) {
        if (other == null) return false;
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                if (this.grid[i][j] != other.grid[i][j]) return false;
            }
        }
        return true;
    }

    public boolean isGoal() {
        int count = 1;
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                if (grid[i][j] != 0 && grid[i][j] != count) return false;
                count++;
            }
        }
        return true;
    }

    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>();

        // Up
        if (blankRow > 0) {
            children.add(createChild(blankRow - 1, blankCol));
        }
        // Down
        if (blankRow < k - 1) {
            children.add(createChild(blankRow + 1, blankCol));
        }
        // Left
        if (blankCol > 0) {
            children.add(createChild(blankRow, blankCol - 1));
        }
        // Right
        if (blankCol < k - 1) {
            children.add(createChild(blankRow, blankCol + 1));
        }
        return children;
    }

    private Node createChild(int newRow, int newCol) {
        int[][] newGrid = getGrid();
        newGrid[blankRow][blankCol] = newGrid[newRow][newCol];
        newGrid[newRow][newCol] = 0;
        return new Node(k, newGrid, moves + 1, this);
    }

    public int getGridSize() {
        return k;
    }

    public int getMoves() {
        return moves;
    }

    public int getBlankRow() {
        return blankRow;
    }

    public int getBlankCol() {
        return blankCol;
    }

    public Node getPrevious() {
        return previous;
    }

    // Static methods

    public static int calculateManhattanDistance(int k, int[][] grid) {
        int distance = 0;
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                int value = grid[i][j];
                if (value == 0) continue;
                int targetRow = (value - 1) / k;
                int targetCol = (value - 1) % k;
                distance += Math.abs(i - targetRow) + Math.abs(j - targetCol);
            }
        }
        return distance;
    }

    public static int calculateHammingDistance(int k, int[][] grid) {
        int distance = 0;
        int count = 1;
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
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int value = board[i][j];
                if (value == 0) continue;
                int targetX = (value - 1) / size;
                int targetY = (value - 1) % size;
                distance += Math.sqrt(Math.pow(i - targetX, 2) + Math.pow(j - targetY, 2));
            }
        }
        return (int)Math.round(distance);
    }
}

// import java.util.*;

public class NPuzzle {
    private int k;

    public NPuzzle(int k) {
        this.k = k;
        System.out.println("ofajfojadfokash");
    }

    public void solveWithManhattanDistance(int[][] initialGrid) {
        solve(initialGrid, true);
    }

    public void solveWithHammingDistance(int[][] initialGrid) {
        solve(initialGrid, false);
    }

    private void solve(int[][] initialGrid, boolean useManhattan) {
        Node initialNode = new Node(k, initialGrid);

        if (!isSolvable(initialNode)) {
            System.out.println("The puzzle instance is not solvable");
            return;
        }

        System.out.println("The puzzle instance is solvable");

        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingInt(useManhattan ? Node::calculateManhattanCost : Node::calculateHammingCost));
        Set<String> closedList = new HashSet<>();

        openList.add(initialNode);

        int totalExpandedNodes = 0;
        int totalExploredNodes = 0;

        while (!openList.isEmpty()) {
            Node current = openList.poll();
            totalExploredNodes++;

            if (current.isGoal()) {
                System.out.println("Steps:");
                printSteps(current);
                System.out.println("Total steps: " + current.getMoves());
                System.out.println("Total expanded nodes: " + totalExpandedNodes);
                System.out.println("Total explored nodes: " + totalExploredNodes);
                return;
            }

            for (Node child : current.getChildren()) {
                if (!closedList.contains(serialize(child))) {
                    openList.add(child);
                    totalExpandedNodes++;
                }
            }

            closedList.add(serialize(current));
        }
    }

    //  public void solve(Node initialNode) {
    //     PriorityQueue<Node> openList = new PriorityQueue<>(new Comparator<Node>() {
    //         @Override
    //         public int compare(Node o1, Node o2) {
    //             return Integer.compare(heuristicStrategy.calculateCost(o1), heuristicStrategy.calculateCost(o2));
    //         }
    //     });

    //     Set<Node> closedList = new HashSet<>();
    //     openList.add(initialNode);

    //     while (!openList.isEmpty()) {
    //         Node current = openList.poll();

    //         if (current.isGoal()) {
    //             printSolution(current);
    //             return;
    //         }

    //         closedList.add(current);
    //         for (Node child : generateChildren(current)) {
    //             if (!closedList.contains(child)) {
    //                 openList.add(child);
    //             }
    //         }
    //     }
    // }

    private void printSteps(Node node) {
        if (node.getPrevious() != null) {
            printSteps(node.getPrevious());
        }
        node.printGrid();
        System.out.println();
    }

    private boolean isSolvable(Node node) {
        int inversions = countInversions(node);
        if (k % 2 != 0) {
            return inversions % 2 == 0;
        } else {
            int blankRowFromBottom = k - node.getBlankRow();
            if (blankRowFromBottom % 2 == 0) {
                return inversions % 2 != 0;
            } else {
                return inversions % 2 == 0;
            }
        }
    }

    private int countInversions(Node node) {
        int[] array = new int[k * k];
        int idx = 0;
        int[][] grid = node.getGrid();
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                array[idx++] = grid[i][j];
            }
        }

        int inversions = 0;
        for (int i = 0; i < array.length; i++) {
            for (int j = i + 1; j < array.length; j++) {
                if (array[i] != 0 && array[j] != 0 && array[i] > array[j]) {
                    inversions++;
                }
            }
        }
        return inversions;
    }

    private String serialize(Node node) {
        StringBuilder sb = new StringBuilder();
        int[][] grid = node.getGrid();
        for (int[] row : grid) {
            for (int value : row) {
                sb.append(value).append(",");
            }
        }
        return sb.toString();
    }
}
// import java.util.Scanner;

public class demo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int k = scanner.nextInt();
        int[][] initialBoard = new int[k][k];

        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                String s = scanner.next();
                if (s.equals("*")) {
                    initialBoard[i][j] = 0;
                } else {
                    initialBoard[i][j] = Integer.parseInt(s);
                }
            }
        }

        NPuzzle p = new NPuzzle(k);

        System.out.println("\nUsing Manhattan heuristic:");
        p.solveWithManhattanDistance(initialBoard);

        System.out.println("\nUsing Hamming heuristic:");
        p.solveWithHammingDistance(initialBoard);
    }
}

// public int calculateLinearConflict() {
//         int conflict = 0;
//         conflict += calculateRowConflicts();
//         conflict += calculateColumnConflicts();
//         return calculateManhattanDistance() + 2 * conflict;
//     }

//     private int calculateRowConflicts() {
//         int conflicts = 0;
//         for (int row = 0; row < size; row++) {
//             for (int col1 = 0; col1 < size - 1; col1++) {
//                 for (int col2 = col1 + 1; col2 < size; col2++) {
//                     int val1 = board[row][col1];
//                     int val2 = board[row][col2];
//                     if (val1 != 0 && val2 != 0 && (val1 - 1) / size == row && (val2 - 1) / size == row && val1 > val2) {
//                         conflicts++;
//                     }
//                 }
//             }
//         }
//         return conflicts;
//     }

//     private int calculateColumnConflicts() {
//         int conflicts = 0;
//         for (int col = 0; col < size; col++) {
//             for (int row1 = 0; row1 < size - 1; row1++) {
//                 for (int row2 = row1 + 1; row2 < size; row2++) {
//                     int val1 = board[row1][col];
//                     int val2 = board[row2][col];
//                     if (val1 != 0 && val2 != 0 && (val1 - 1) % size == col && (val2 - 1) % size == col && val1 > val2) {
//                         conflicts++;
//                     }
//                 }
//             }
//         }
//         return conflicts;
//     }



