public class EuclideanHeuristic implements Heuristic{
    @Override
    public int calculateCost(PuzzleConfig puzzleConfig){
        return puzzleConfig.getMoves() + puzzleConfig.calculateEuclideanDistance();
    }
}