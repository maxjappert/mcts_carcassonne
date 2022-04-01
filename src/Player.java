//import org.slf4j.//logger;
//import org.slf4j.//loggerFactory;

import org.apache.commons.math3.util.Pair;

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

    /**Decides on a next move given all possible next moves.
     * @param state The state for which the move should be executed.
     * @param tile The drawn tile.
     * @param deck The remaining deck.
     * @param legalMoves A list of all possible moves.
     * @return A pair consisting of an index to the legalMoves list and an index of a possible meeple on the tile.
     */
    abstract Pair<Integer, Integer> decideOnNextMove(GameState state, Tile tile, List<Tile> deck, List<Move> legalMoves) throws Exception;

    abstract String getTypeAsString();
}
