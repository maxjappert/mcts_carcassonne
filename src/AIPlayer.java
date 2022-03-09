import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIPlayer extends Player {

    public AIPlayer(int playerID) {
        super(playerID);
    }

    /**
     * For now this will be a random walk.
     */
    @Override
    public int[] decideOnNextMove(GameState state, GameStateSpace stateSpace, Tile tile) throws Exception {

        List<ActionRotationStateTriple> ars = stateSpace.succ(state, tile);

        // In this case no move is possible and a new tile has to be drawn.
        if (ars.size() == 0) {
            return new int[]{-1, -1};
        }

        ActionRotationStateTriple move = ars.get(new Random().nextInt(ars.size()));

        for (int i = 0; i < move.getRotation(); i++) {
            tile.rotate();
        }

        int[] action = move.getAction();

        List<Integer> meeplePlacements = stateSpace.legalMeeples(state, tile, action);

        if (new Random().nextInt(4) == 0 && meeplePlacements.size() > 0) {
            tile.placeMeeple(meeplePlacements.get(new Random().nextInt(meeplePlacements.size())), playerID);
        }

        return move.getAction();
    }
}
