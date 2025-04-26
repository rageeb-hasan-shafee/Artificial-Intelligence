public class Factory{
    public static Strategy getHeuristic(String type){
        if(type == null) 
            return null;

            
        switch (type.toUpperCase()) {
            case "MANHATTAN" : 
                return new ManhattanHeuristic();
            case "HAMMING" : 
                return new HammingHeuristic();
            case "EUCLIDEAN" : 
                return new EuclideanHeuristic();
            case "LINEAR_CONFLICT" : 
                return new LinearConflictHeuristic();
            default:
                return null;
        }
    }
}