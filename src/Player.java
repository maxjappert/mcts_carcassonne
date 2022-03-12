import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class Player {

    static final Logger logger = LoggerFactory.getLogger("PlayerLogger");

    /**
     * An integer to uniquely identify each player. Since this implementation only considers the two-player version
     * of Carcassonne, the ID should either be 1 or 2.
     */
    protected int playerID;

    protected int currentPoints;

    protected int numberOfMeeples;

    protected Player(int playerID) {
        if (playerID != 1 && playerID != 2) {
            logger.error("Player initialised with invalid ID");
        }
        this.playerID = playerID;
        this.currentPoints = 0;
        this.numberOfMeeples = 7;
    }

    abstract int[] decideOnNextMove(GameState state, GameStateSpace stateSpace, Tile tile) throws Exception;

    public int getPoints() {
        return currentPoints;
    }
}
