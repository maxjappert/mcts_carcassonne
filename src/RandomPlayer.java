import java.util.List;
import java.util.Random;

public class RandomPlayer extends Player {

    private final Random random;
    private final long seed;

    public RandomPlayer(GameStateSpace stateSpace, int playerID, long randomSeed) {
        super(stateSpace, playerID);
        if (randomSeed == -1) {
            random = new Random();
        } else {
            random = new Random(randomSeed);
        }

        this.seed = randomSeed;
    }

    /**
     * Decides on a random possible next move. Rotation included.
     * @return The coordinates of where the tile will be placed.
     */
    @Override
    public Pair decideOnNextMove(GameState state, Tile tile, List<Tile> deck, List<Move> legalMoves) {
        return new Pair(random.nextInt(legalMoves.size()), random.nextInt(13));
    }

    public String getTypeAsString() {
        return "Random Player";
    }

    public long getSeed() {
        return seed;
    }
}
