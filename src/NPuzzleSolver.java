import java.util.*;

public class NPuzzleSolver {
    private Heuristic heuristic;

    public NPuzzleSolver(Heuristic heuristic) {
        this.heuristic = heuristic;
    }

    public void solve(PuzzleConfig initialConfig) {
        PriorityQueue<PuzzleConfig> openList = new PriorityQueue<>(Comparator.comparingInt(heuristic::calculateCost));
        Set<String> closedList = new HashSet<>();
        Set<String> openListSet = new HashSet<>();

        openList.add(initialConfig);
        int totalExplored = 1;
        int totalExpanded = 0;

        while (!openList.isEmpty()) {
            PuzzleConfig current = openList.poll();
            openListSet.remove(serialize(current)); // remove from open set
            totalExpanded++;

            if (current.isGoal()) {
                System.out.println("Minimum number of moves = " + current.getMoves());
                printSolution(current);
                System.out.println("Total explored puzzleConfigs = " + totalExplored);
                System.out.println("Total expanded puzzleConfigs = " + totalExpanded);
                return;
            }

            closedList.add(serialize(current));

            for (PuzzleConfig child : current.getChildren()) {
                String serializedChild = serialize(child);

                if (!closedList.contains(serializedChild) && !openListSet.contains(serializedChild)) {
                    openList.add(child);
                    openListSet.add(serializedChild);
                    totalExplored++;
                }
            }
        }
        System.out.println("Unsolvable puzzle");
    }

    private void printSolution(PuzzleConfig puzzleConfig) {
        if (puzzleConfig.getPrevious() != null) {
            printSolution(puzzleConfig.getPrevious());
        }
        puzzleConfig.printGrid();
    }

    private String serialize(PuzzleConfig puzzleConfig) {
        StringBuilder sb = new StringBuilder();
        int[][] grid = puzzleConfig.getGrid();
        for (int[] row : grid) {
            for (int val : row) {
                sb.append(val).append(",");
            }
        }
        return sb.toString();
    }
}
