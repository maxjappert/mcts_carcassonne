import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HeuristicPlayer extends Player{
    protected HeuristicPlayer(GameStateSpace stateSpace, int playerID) {
        super(stateSpace, playerID);
    }

    @Override
    Pair decideOnNextMove(GameState originalState, Tile originalTile, List<Tile> deck, List<Move> legalMoves) throws Exception {

        GameState state = new GameState(originalState);
        Tile tile = new Tile(originalTile);
        int highestPlacementValue = Integer.MIN_VALUE;
        int indexOfHighestPlacementValue = -1;

        int i = 0;
        for (Move move : legalMoves) {
            int h = stateSpace.moveHeuristic(state, move, tile, playerID);
            //System.out.println("Placement heuristic: " + h);
            if (h > highestPlacementValue) {
                highestPlacementValue = h;
                indexOfHighestPlacementValue = i;
            }
            i++;
        }

        Move move = legalMoves.get(indexOfHighestPlacementValue);
        tile.rotateBy(move.getRotation());

        List<Integer> legalMeeples = stateSpace.meepleSucc(state, tile, move.getCoords(), playerID);
        Collections.shuffle(legalMeeples);
        int highestMeepleValue = Integer.MIN_VALUE;
        int bestMeeplePlacement = -1;

        for (int point : legalMeeples) {
            int h = stateSpace.meepleHeuristic(state, tile, point, playerID);
            if (h > highestMeepleValue) {
                System.out.println("Meeple heuristic: " + h);
                highestMeepleValue = h;
                bestMeeplePlacement = point;
            }
        }

        return new Pair(indexOfHighestPlacementValue, bestMeeplePlacement);
    }

    @Override
    String getTypeAsString() {
        return "heuristic";
    }
}
