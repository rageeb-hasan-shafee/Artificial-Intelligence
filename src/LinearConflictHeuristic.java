public class LinearConflictHeuristic implements Strategy {
    @Override
    public int calculateCost(Node node) {
        return node.getMoves() + node.calculateLinearConflict();
    }
}
