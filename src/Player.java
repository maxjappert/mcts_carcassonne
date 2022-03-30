//import org.slf4j.//logger;
//import org.slf4j.//loggerFactory;

import java.util.List;

abstract class Player {

    //static final //logger //logger = //loggerFactory.get//logger("Player//logger");

    /**
     * An integer to uniquely identify each player. Since this implementation only considers the two-player version
     * of Carcassonne, the ID should either be 1 or 2.
     */
    protected int playerID;
    protected GameStateSpace stateSpace;
    protected Player(GameStateSpace stateSpace, int playerID) {
        this.playerID = playerID;
        this.stateSpace = stateSpace;
    }

    abstract Coordinates decideOnNextMove(GameState state, Tile tile, List<Tile> deck) throws Exception;
}
