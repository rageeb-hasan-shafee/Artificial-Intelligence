import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int k = sc.nextInt();
        int[][] initialBoard = new int[k][k];
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                String s = sc.next();
                // if (s.equals("*")) {
                //     initialBoard[i][j] = 0;
                // } else {
                    initialBoard[i][j] = Integer.parseInt(s);
                //}
            }
        }

        System.out.println("Enter heuristic (MANHATTAN / HAMMING / EUCLIDEAN / LINEAR_CONFLICT):");
        String heuristicType = sc.next();

        Heuristic heuristic = Factory.getHeuristic(heuristicType);
        if (heuristic == null) {
            System.out.println("Invalid heuristic selected.");
            return;
        }

        PuzzleConfig initialNode = new PuzzleConfig(k, initialBoard);

        if (!isSolvable(initialNode)) {
            System.out.println("Unsolvable puzzle");
        } else {
            NPuzzleSolver solver = new NPuzzleSolver(heuristic);
            solver.solve(initialNode);
        }
    }

    private static boolean isSolvable(PuzzleConfig puzzleConfig) {
        int[] array = flatten(puzzleConfig.getGrid());
        int inversions = countInversions(array);
        int k = puzzleConfig.getGrid().length;
        int blankRowFromBottom = k - puzzleConfig.getBlankRow();

        if (k % 2 == 1) {
            return inversions % 2 == 0;
        } else {
            return (blankRowFromBottom % 2 == 0) != (inversions % 2 == 0);
        }
    }

    private static int[] flatten(int[][] board) {
        int n = board.length;
        int[] array = new int[n * n];    
        int idx = 0;
        for (int[] row : board) {
            for (int val : row) {
                array[idx++] = val;
            }
        }
        return array;
    }

    private static int countInversions(int[] array) {
        int inv = 0;
        for (int i = 0; i < array.length; i++) {
            for (int j = i + 1; j < array.length; j++) {
                if (array[i] != 0 && array[j] != 0 && array[i] > array[j]) {
                    inv++;
                }
            }
        }
        return inv;
    }
}
