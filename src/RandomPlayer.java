//import org.slf4j.//logger;
//import org.slf4j.//loggerFactory;

import java.util.List;
import java.util.Random;

public class RandomPlayer extends Player {

    //static final //logger //logger = //loggerFactory.get//logger("AIPlayer//logger");
    Random random;

    public RandomPlayer(int playerID) {
        super((byte) playerID);
        random = new Random();
    }

    /**
     * Decides on a random possible next move. Rotation included.
     * @return The coordinates of where the tile will be placed.
     */
    @Override
    public byte[] decideOnNextMove(GameState state, GameStateSpace stateSpace, Tile tile, List<Tile> deck) {

        //GameState state = new GameState(originalState);

        List<ActionRotationStateTriple> ars = stateSpace.succ(state, tile);

        // In this case no move is possible and a new tile has to be drawn.
        if (ars.isEmpty()) {
            return new byte[]{-1, -1};
        }

        ActionRotationStateTriple move = ars.get(random.nextInt(ars.size()));

        for (int i = 0; i < move.getRotation(); i++) {
            tile.rotate();
        }

        byte[] action = move.getAction();

        //logger.info("Player {} placed tile at [{}, {}] with rotation {}.", playerID, move.getAction()[0], move.getAction()[1], tile.getRotation());

        // Here the placement of the meeple is decided

        List<Byte> meeplePlacements = stateSpace.legalMeeples(state, tile, action);

        if (random.nextBoolean() && !meeplePlacements.isEmpty() && state.getNumMeeples(playerID) > 0) {
            //logger.info("Player {} places meeple on point {}, which is of type {} and belongs to area {}. {} meeples remaining.", tile.getMeeple()[1], tile.getMeeple()[0], tile.getPoint(tile.getMeeple()[0]), tile.getArea(tile.getMeeple()[0]), state.getNumMeeples(playerID));
            tile.placeMeeple(meeplePlacements.get(random.nextInt(meeplePlacements.size())), playerID, state);
        }

        return move.getAction();
    }
}
