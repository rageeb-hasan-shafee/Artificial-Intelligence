public class ManhattanHeuristic implements Strategy{
    @Override
    public int calculateCost(){
                return node.getMoves() + node.calculateManhattanDistance();
    }
}