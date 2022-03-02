import java.util.Arrays;
import java.util.List;

abstract class Player {

    /**
     * An integer to uniquely identify each player. Since this implementation only considers the two-player version
     * of Carcassonne, the ID should either be 1 or 2.
     */
    protected int playerID;

    public Player(int playerID) {
        assert (playerID == 1 || playerID == 2);
        this.playerID = playerID;
    }

    /**
     * The tile the player has drawn from the deck and contemplates at a given point.
     */
    protected Tile drawnTile;

    /**
     * Draw one tile from the deck and place it into the drawnTile variable.
     * @param deck The deck to be drawn from.
     */
    public void draw(List<Tile> deck) {
        drawnTile = deck.remove(0);
    }

    public Tile getDrawnTile() {
        return drawnTile;
    }

    /**
     * Checks if a move is legal.
     * @param move The place to put the tile.
     * @param tile The tile which should be placed.
     * @param board The board on which the move should be executed.
     * @return True if the move is legal.
     */
    public boolean isLegalMove(int[] move, Tile tile, List<List<Tile>> board) {

        // If the new tile is placed such that a new row and a new column of the board are created, then it necessarily
        // follows that the tile doesn't connect to any tile on the board and therefore the move must be illegal.
        if (move[0] >= Game.getBoardDimensions(board)[0] + 1 && move[1] >= Game.getBoardDimensions(board)[1] + 1) {
            return false;
        }


        boolean[] connected = new boolean[]{true, true, true, true};

        if (move[0] == 0) {
            connected = new boolean[]{true, false, false, false};
        } else if (move[1] == 0) {
            connected = new boolean[]{false, true, false, false};
        } else if (move[0] == Game.getBoardDimensions(board)[0] + 1) {
            connected = new boolean[]{false, false, true, false};
        } else if (move[1] == Game.getBoardDimensions(board)[1] + 1) {
            connected = new boolean[]{false, false, false, true};
        } else {
            if (Game.getTile(new int[]{move[0] + 1, move[1]}, board) == null) {
                connected[0] = false;
            }

            if (Game.getTile(new int[]{move[0], move[1] + 1}, board) == null) {
                connected[1] = false;
            }

            if (Game.getTile(new int[]{move[0] - 1, move[1]}, board) == null) {
                connected[2] = false;
            }

            if (Game.getTile(new int[]{move[0], move[1] - 1}, board) == null) {
                connected[3] = false;
            }
        }

        try {
            if (connected[0] && Game.getTile(new int[]{move[0], move[1] - 1}, board).getSides()[2] != tile.getSides()[0]) {
                return false;
            }

            if (connected[1] && Game.getTile(new int[]{move[0] - 1, move[1]}, board).getSides()[3] != tile.getSides()[1]) {
                return false;
            }

            if (connected[2] && Game.getTile(new int[]{move[0] - 2, move[1] - 1}, board).getSides()[0] != tile.getSides()[2]) {
                return false;
            }

            if (connected[3] && Game.getTile(new int[]{move[0] - 1, move[1] - 2}, board).getSides()[1] != tile.getSides()[3]) {
                return false;
            }
        } catch (NullPointerException e) {
            System.out.println("For move " + Arrays.toString(move) + " and a board of size " + Arrays.toString(Game.getBoardDimensions(board)) + " there was a null pointer exception.");
        }

        // The move needs to be connected on least one side.
        return connected[0] || connected[1] || connected[2] || connected[3];
    }

    public void removeTile() {
        drawnTile = null;
    }

    abstract int[] decideOnNextMove(List<List<Tile>> board) throws Exception;
}
