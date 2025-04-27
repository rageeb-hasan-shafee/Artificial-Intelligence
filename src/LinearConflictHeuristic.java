public class LinearConflictHeuristic implements Heuristic {
    @Override
    public int calculateCost(PuzzleConfig puzzleConfig) {
        return puzzleConfig.getMoves() + puzzleConfig.calculateLinearConflict();
    }
}
