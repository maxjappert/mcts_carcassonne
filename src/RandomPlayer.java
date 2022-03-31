//import org.slf4j.//logger;
//import org.slf4j.//loggerFactory;

import org.apache.commons.math3.util.Pair;

import java.util.List;
import java.util.Random;

public class RandomPlayer extends Player {

    //static final //logger //logger = //loggerFactory.get//logger("AIPlayer//logger");
    Random random;

    public RandomPlayer(GameStateSpace stateSpace, int playerID) {
        super(stateSpace, playerID);
        random = new Random();
    }

    /**
     * Decides on a random possible next move. Rotation included.
     * @return The coordinates of where the tile will be placed.
     */
    @Override
    public Pair<Integer, Integer> decideOnNextMove(GameState state, Tile tile, List<Tile> deck, List<Move> legalMoves) {
        Random random = new Random();

        return new Pair<>(random.nextInt(legalMoves.size()), random.nextInt(13));
    }
}
