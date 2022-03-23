//import org.slf4j.//logger;
//import org.slf4j.//loggerFactory;

import java.util.List;

abstract class Player {

    //static final //logger //logger = //loggerFactory.get//logger("Player//logger");

    /**
     * An integer to uniquely identify each player. Since this implementation only considers the two-player version
     * of Carcassonne, the ID should either be 1 or 2.
     */
    protected byte playerID;
    protected Player(byte playerID) {
        if (playerID != 1 && playerID != 2) {
            //logger.error("Player initialised with invalid ID");
        }
        this.playerID = playerID;
    }

    abstract byte[] decideOnNextMove(GameState state, GameStateSpace stateSpace, Tile tile, List<Tile> deck) throws Exception;
}
