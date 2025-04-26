public class EuclideanHeuristic implements Strategy{
    @Override
    public int calculateCost(Node node){
        return node.getMoves() + node.calculateEuclideanDistance();
    }
}