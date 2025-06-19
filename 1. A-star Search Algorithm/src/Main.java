import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            PuzzleConfig initialNode = null;
            int k = 0;

            // Input a NEW puzzle
            while (true) {
                try {
                    System.out.println("Enter size of puzzle (k):");
                    k = Integer.parseInt(sc.nextLine().trim());
                    if (k <= 1) {
                        System.out.println("Size must be at least 2. Try again.");
                        continue;
                    }

                    System.out.println("Enter the puzzle board (use 0 for blank):");
                    int[][] initialBoard = new int[k][k];
                    for (int i = 0; i < k; i++) {
                        String[] row = sc.nextLine().trim().split("\\s+");
                        if (row.length != k) {
                            System.out.println("Invalid row length. Try again.");
                            i--; // Retry this row
                            continue;
                        }
                        for (int j = 0; j < k; j++) {
                            // if (row[j].equals("*")) {
                            //     initialBoard[i][j] = 0;
                            // } else {
                                initialBoard[i][j] = Integer.parseInt(row[j]);
                            }
                        // }
                    }

                    initialNode = new PuzzleConfig(k, initialBoard);

                    if (!isSolvable(initialNode)) {
                        System.out.println("Unsolvable puzzle! Please input a solvable puzzle.");
                        continue;
                    }

                    break; // Valid puzzle input
                } catch (Exception e) {
                    System.out.println("Invalid input! Please try again.");
                }
            }

            boolean continueWithSamePuzzle = true;

            while (continueWithSamePuzzle) {
                // Ask heuristic
                Heuristic heuristic = null;
                while (heuristic == null) {
                    System.out.println("Enter heuristic (MANHATTAN / HAMMING / EUCLIDEAN / LINEAR_CONFLICT):");
                    String heuristicType = sc.nextLine().trim();
                    heuristic = Factory.getHeuristic(heuristicType);
                    if (heuristic == null) {
                        System.out.println("Invalid heuristic selected. Try again.");
                    }
                }

                // Solve
                NPuzzleSolver solver = new NPuzzleSolver(heuristic);
                solver.solve(initialNode);

                // Ask what to do next
                System.out.println("\nWhat do you want to do next?");
                System.out.println("1. Try another heuristic on the same puzzle");
                System.out.println("2. Solve a completely new puzzle");
                System.out.println("3. Exit");

                String choice = sc.nextLine().trim();
                if (choice.equals("1")) {
                    continueWithSamePuzzle = true; // Same puzzle, different heuristic
                } else if (choice.equals("2")) {
                    continueWithSamePuzzle = false; // Break inner loop to input new puzzle
                } else if (choice.equals("3")) {
                    System.out.println("Exiting program. Thank you!");
                    return; // Exit the entire program
                } else {
                    System.out.println("Invalid option. Assuming exit.");
                    return;
                }
            }
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
