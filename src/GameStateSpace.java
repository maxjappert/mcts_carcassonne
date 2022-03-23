//import org.slf4j.//logger;
//import org.slf4j.//loggerFactory;

import java.util.*;

public class GameStateSpace {
//    final static //logger //logger = //loggerFactory.get//logger("GameStateSpace//logger");

    public GameState init() {
        return new GameState();
    }

    public boolean isGoal(GameState state) {
        return state.getDeckSize() == 0;
    }

    public List<ActionRotationStateTriple> succ(GameState state, Tile tile) {
        List<ActionRotationStateTriple> successors = new ArrayList<>();

        Tile drawnTile = new Tile(tile);

        int[] boardDimensions = state.getBoardDimensions();
        boardDimensions[0] += 2;
        boardDimensions[1] += 2;

        for (byte i = 0; i < boardDimensions[0]; i++) {
            for (byte j = 0; j < boardDimensions[1]; j++) {
                byte[] move = new byte[]{i, j};
                for (int rotation = 0; rotation < 4; rotation++) {
                    if (isLegalMove(move, drawnTile, state)) {
                        GameState updatedState = new GameState(state);
                        updatedState.updateBoard(move, drawnTile);
                        ActionRotationStateTriple arst = new ActionRotationStateTriple(move, rotation, updatedState);
                        successors.add(arst);

                        ////logger.info("[{}, {}] with rotation {} is a legal move.", move[0], move[1], rotation);
                    }
                    drawnTile.rotate();
                }
            }
        }

        return successors;
    }

    /**
     *
     * @param state The state in question.
     * @param tile The tile in question.
     * @return Subset of {0, ..., 12}, denoting the sides on which meeples can be placed.
     */
    public List<Byte> legalMeeples(GameState state, Tile tile, byte[] move) {
        List<Byte> placements = new ArrayList<>();

        if (state.getNumMeeples(state.getPlayer()) < 1) {
            return placements;
        }

        for (int point = 0; point < 13; point++) {
            if (checkIfLegalMeeplePlacement(tile, point, move, state)) {
                placements.add((byte) point);
            }
        }
        
        return placements;
    }

    /**
     * Checks if a move is legal.
     * @param move The place to put the tile.
     * @param tile The tile which should be placed.
     * @param state The current game state.
     * @return True if the move is legal.
     */
    private boolean isLegalMove(byte[] move, Tile tile, GameState state) {

        if (move[0] > 0 && move[0] <= state.getBoardDimensions()[0] && move[1] > 0 && move[1] <= state.getBoardDimensions()[1]) {
            if (state.getTile(new int[]{move[0]-1, move[1]-1}) != null) {
                return false;
            }
        }

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
            if (state.getTile(new int[]{move[0], move[1] - 1}) == null) {
                connected[0] = false;
            }

            if (state.getTile(new int[]{move[0] - 1, move[1]}) == null) {
                connected[1] = false;
            }

            if (state.getTile(new int[]{move[0] - 2, move[1] - 1}) == null) {
                connected[2] = false;
            }

            if (state.getTile(new int[]{move[0] - 1, move[1] - 2}) == null) {
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
            // This is a bodge.
            return false;
        }

        // The move needs to be connected on least one side.
        return connected[0] || connected[1] || connected[2] || connected[3];
    }

    private boolean checkIfLegalMeeplePlacement(Tile tile, int point, byte[] move, GameState state) {

        // Can't place meeples on intersections.
        if (point == 12 && tile.getMiddle() == 3) {
            return false;
        }

        // Use the copy constructors to create deep copies of the state and the tile in order to operate on them
        // without changing the actual state and tile.
        GameState potentialState = new GameState(state);
        Tile potentialTile = new Tile(tile);

        potentialState.updateBoard(move, potentialTile);

        List<Tile> tilesInArea;

        if (point == 12) {
            tilesInArea = potentialState.getTilesOfArea(potentialTile.getMiddleArea());
        } else {
            tilesInArea = potentialState.getTilesOfArea(potentialTile.getArea(point));
        }

        for (Tile tileInArea : tilesInArea) {
            if (tileInArea.getMeeple()[0] != -1 && tileInArea.getArea(tileInArea.getMeeple()[0]) == potentialTile.getArea(point)) {
                return false;
            }
        }

        return true;
    }
}
