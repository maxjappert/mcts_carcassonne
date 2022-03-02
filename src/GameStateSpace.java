import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameStateSpace {
    public GameState init() {
        return new GameState();
    }

    public boolean isGoal(GameState state) {
        return state.deck.isEmpty();
    }

    public List<ActionRotationStateTriple> succ(GameState state, Tile drawnTile) {
        List<List<Tile>> board =  state.getBoard();

        List<ActionRotationStateTriple> successors = new ArrayList<>();

        int[] boardDimensions = state.getBoardDimensions();
        boardDimensions[0] += 2;
        boardDimensions[1] += 2;

        for (int i = 0; i < boardDimensions[0]; i++) {
            for (int j = 0; j < boardDimensions[1]; j++) {
                int[] move = new int[]{i, j};
                for (int rotation = 0; rotation < 4; rotation++) {
                    if (isLegalMove(move, drawnTile, state)) {
                        GameState updatedState = new GameState(state);
                        updatedState.updateBoard(move, drawnTile);
                        successors.add(new ActionRotationStateTriple(move, rotation, updatedState));
                    }
                    drawnTile.rotate();
                }
            }
        }

        return successors;
    }

    /**
     * Checks if a move is legal.
     * @param move The place to put the tile.
     * @param tile The tile which should be placed.
     * @param state The current game state.
     * @return True if the move is legal.
     */
    private boolean isLegalMove(int[] move, Tile tile, GameState state) {

        List<List<Tile>> board = state.getBoard();

        // If the new tile is placed such that a new row and a new column of the board are created, then it necessarily
        // follows that the tile doesn't connect to any tile on the board and therefore the move must be illegal.
        if (move[0] >= state.getBoardDimensions()[0] + 1 && move[1] >= state.getBoardDimensions()[1] + 1) {
            return false;
        }


        boolean[] connected = new boolean[]{true, true, true, true};

        if (move[0] == 0) {
            connected = new boolean[]{true, false, false, false};
        } else if (move[1] == 0) {
            connected = new boolean[]{false, true, false, false};
        } else if (move[0] == state.getBoardDimensions()[0] + 1) {
            connected = new boolean[]{false, false, true, false};
        } else if (move[1] == state.getBoardDimensions()[1] + 1) {
            connected = new boolean[]{false, false, false, true};
        } else {
            if (state.getTile(new int[]{move[0] + 1, move[1]}) == null) {
                connected[0] = false;
            }

            if (state.getTile(new int[]{move[0], move[1] + 1}) == null) {
                connected[1] = false;
            }

            if (state.getTile(new int[]{move[0] - 1, move[1]}) == null) {
                connected[2] = false;
            }

            if (state.getTile(new int[]{move[0], move[1] - 1}) == null) {
                connected[3] = false;
            }
        }

        try {
            if (connected[0] && state.getTile(new int[]{move[0], move[1] - 1}).getSides()[2] != tile.getSides()[0]) {
                return false;
            }

            if (connected[1] && state.getTile(new int[]{move[0] - 1, move[1]}).getSides()[3] != tile.getSides()[1]) {
                return false;
            }

            if (connected[2] && state.getTile(new int[]{move[0] - 2, move[1] - 1}).getSides()[0] != tile.getSides()[2]) {
                return false;
            }

            if (connected[3] && state.getTile(new int[]{move[0] - 1, move[1] - 2}).getSides()[1] != tile.getSides()[3]) {
                return false;
            }
        } catch (NullPointerException e) {
            // TODO: This is a bodge.
            return false;
            //System.out.println("For move " + Arrays.toString(move) + " and a board of size " + Arrays.toString(state.getBoardDimensions()) + " there was a null pointer exception.");
        }

        // The move needs to be connected on least one side.
        return connected[0] || connected[1] || connected[2] || connected[3];
    }
}