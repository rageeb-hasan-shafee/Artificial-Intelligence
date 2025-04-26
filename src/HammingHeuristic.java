public class HammingHeuristic implements Strategy{
    @Override
    public int calculateCost(Node node){
        return node.getMoves() + node.calculateHammingDistance();
    }
}