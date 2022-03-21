import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class Player {

    static final Logger logger = LoggerFactory.getLogger("PlayerLogger");

    /**
     * An integer to uniquely identify each player. Since this implementation only considers the two-player version
     * of Carcassonne, the ID should either be 1 or 2.
     */
    protected byte playerID;
    protected Player(byte playerID) {
        if (playerID != 1 && playerID != 2) {
            logger.error("Player initialised with invalid ID");
        }
        this.playerID = playerID;
    }

    abstract byte[] decideOnNextMove(GameState state, GameStateSpace stateSpace, Tile tile) throws Exception;
}
