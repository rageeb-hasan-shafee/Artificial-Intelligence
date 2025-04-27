    public class HammingHeuristic implements Heuristic{
        @Override
        public int calculateCost(PuzzleConfig puzzleConfig){
            return puzzleConfig.getMoves() + puzzleConfig.calculateHammingDistance();
        }
    }