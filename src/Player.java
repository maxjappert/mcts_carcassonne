import java.util.Arrays;
import java.util.List;

abstract class Player {

    /**
     * An integer to uniquely identify each player. Since this implementation only considers the two-player version
     * of Carcassonne, the ID should either be 1 or 2.
     */
    protected int playerID;

    protected int currentPoints;

    protected int numberOfMeeples;

    public Player(int playerID) {
        assert (playerID == 1 || playerID == 2);
        this.playerID = playerID;
        this.currentPoints = 0;
        this.numberOfMeeples = 7;
    }

    abstract int[] decideOnNextMove(GameState state, GameStateSpace stateSpace, Tile tile) throws Exception;
}
