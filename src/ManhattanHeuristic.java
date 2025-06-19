public class ManhattanHeuristic implements Heuristic{
    @Override
    public int calculateCost(PuzzleConfig puzzleConfig){
                return puzzleConfig.getMoves() + puzzleConfig.calculateManhattanDistance();
    }
}