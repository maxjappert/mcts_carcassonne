import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

public class RandomPlayer extends Player {

    static final Logger logger = LoggerFactory.getLogger("AIPlayerLogger");
    Random random;

    public RandomPlayer(int playerID) {
        super(playerID);
        random = new Random();
    }

    /**
     * For now this will be a random walk.
     */
    @Override
    public int[] decideOnNextMove(GameState state, GameStateSpace stateSpace, Tile tile) {

        //GameState state = new GameState(originalState);

        List<ActionRotationStateTriple> ars = stateSpace.succ(state, tile);

        // In this case no move is possible and a new tile has to be drawn.
        if (ars.isEmpty()) {
            return new int[]{-1, -1};
        }

        ActionRotationStateTriple move = ars.get(random.nextInt(ars.size()));

        for (int i = 0; i < move.getRotation(); i++) {
            tile.rotate();
        }

        int[] action = move.getAction();

        logger.info("Player {} placed tile at [{}, {}] with rotation {}.", playerID, move.getAction()[0], move.getAction()[1], tile.getRotation());

        // Here the placement of the meeple is decided

        List<Integer> meeplePlacements = stateSpace.legalMeeples(state, tile, action);

        if (random.nextBoolean() && !meeplePlacements.isEmpty() && state.getNumMeeples(playerID) > 0) {
            tile.placeMeeple(meeplePlacements.get(random.nextInt(meeplePlacements.size())), playerID, state);
            logger.info("Player {} places meeple on point {}, which is of type {} and belongs to area {}. {} meeples remaining.", tile.getMeeple()[1], tile.getPoint(tile.getMeeple()[0]), tile.getArea(tile.getMeeple()[0]), tile.getMeeple()[0], state.getNumMeeples(playerID));
        }

        return move.getAction();
    }
}
