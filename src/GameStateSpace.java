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

    public List<Move> placementSucc(GameState state, Tile tile) {
        List<Move> successors = new ArrayList<>();

        Tile drawnTile = new Tile(tile);

        int[] boardDimensions = state.getBoardDimensions();
        boardDimensions[0] += 2;
        boardDimensions[1] += 2;

        for (int i = 0; i < boardDimensions[0]; i++) {
            for (int j = 0; j < boardDimensions[1]; j++) {
                Coordinates move = new Coordinates(i, j);
                for (int rotation = 0; rotation < 4; rotation++) {
                    if (isLegalMove(move, drawnTile, state)) {
                        Move arst = new Move(move, rotation);
                        successors.add(arst);

                        ////logger.info("[{}, {}] with rotation {} is a legal move.", coords.x, coords.y, rotation);
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
    public List<Integer> meepleSucc(GameState state, Tile tile, Coordinates move, int player) {
        List<Integer> placements = new ArrayList<>();
        placements.add(-1);

        if (state.getNumMeeples(player) == 0) {
            return placements;
        }

        for (int point = 0; point <= 12; point++) {
            if (checkIfLegalMeeplePlacement(tile, point, move, state)) {
                placements.add(point);
            }
        }

        return placements;
    }

    /**
     * Checks if a move is legal.
     * @param coords The place to put the tile.
     * @param tile The tile which should be placed.
     * @param state The current game state.
     * @return True if the move is legal.
     */
    private boolean isLegalMove(Coordinates coords, Tile tile, GameState state) {

        if (coords.x > 0 && coords.x <= state.getBoardDimensions()[0] && coords.y > 0 && coords.y <= state.getBoardDimensions()[1]) {
            if (state.getTile(new Coordinates(coords.x-1, coords.y-1)) != null) {
                return false;
            }
        }

        // If the new tile is placed such that a new row and a new column of the board are created, then it necessarily
        // follows that the tile doesn't connect to any tile on the board and therefore the move must be illegal.
        if (coords.x >= state.getBoardDimensions()[0] + 1 && coords.y >= state.getBoardDimensions()[1] + 1) {
            return false;
        }

        boolean[] connected = new boolean[]{true, true, true, true};

        if (coords.x == 0) {
            connected = new boolean[]{true, false, false, false};
        } else if (coords.y == 0) {
            connected = new boolean[]{false, true, false, false};
        } else if (coords.x == state.getBoardDimensions()[0] + 1) {
            connected = new boolean[]{false, false, true, false};
        } else if (coords.y == state.getBoardDimensions()[1] + 1) {
            connected = new boolean[]{false, false, false, true};
        } else {
            if (state.getTile(new Coordinates(coords.x, coords.y - 1)) == null) {
                connected[0] = false;
            }

            if (state.getTile(new Coordinates(coords.x - 1, coords.y)) == null) {
                connected[1] = false;
            }

            if (state.getTile(new Coordinates(coords.x - 2, coords.y - 1)) == null) {
                connected[2] = false;
            }

            if (state.getTile(new Coordinates(coords.x - 1, coords.y - 2)) == null) {
                connected[3] = false;
            }
        }

        try {
            if (connected[0] && state.getTile(new Coordinates(coords.x, coords.y - 1)).getPoint(7) != tile.getPoint(1)) {
                return false;
            }

            if (connected[1] && state.getTile(new Coordinates(coords.x - 1, coords.y)).getPoint(10) != tile.getPoint(4)) {
                return false;
            }

            if (connected[2] && state.getTile(new Coordinates(coords.x - 2, coords.y - 1)).getPoint(1) != tile.getPoint(7)) {
                return false;
            }

            if (connected[3] && state.getTile(new Coordinates(coords.x - 1, coords.y - 2)).getPoint(4) != tile.getPoint(10)) {
                return false;
            }
        } catch (NullPointerException e) {
            // This is a bodge.
            return false;
        }

        // The move needs to be connected on least one side.
        return connected[0] || connected[1] || connected[2] || connected[3];
    }

    private boolean checkIfLegalMeeplePlacement(Tile tile, int point, Coordinates coords, GameState state) {

        // Can't place meeples on intersections.
        if (point == 12 && tile.getMiddle() == 3) {
            return false;
        }

        // Use the copy constructors to create deep copies of the state and the tile in order to operate on them
        // without changing the actual state and tile.
        GameState potentialState = new GameState(state);
        Tile potentialTile = new Tile(tile);

        potentialState.updateBoard(coords, potentialTile);

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

    /**
     * Rates the quality of a move (without meeple placement) given a board and a tile.
     * @param originalState The state on which the placement is to take place.
     * @param move The move to be rated.
     * @param originalTile The tile to be rotated and placed.
     * @param player The ID of the player in question
     * @return A rating of the move. The higher, the better.
     */
    public int moveHeuristic(GameState originalState, Move move, Tile originalTile, int player) {
        int output;

        GameState state = new GameState(originalState);
        Tile tile = new Tile(originalTile);
        tile.rotateBy(move.getRotation());

        int prevScore = state.getScore()[player-1];
        int prevScoreOp = player == 1 ? state.getScore()[1] : state.getScore()[0];

        state.updateBoard(move.getCoords(), tile);
        state.checkForScoreAfterRound(false);
        state.assignPointsAtEndOfGame();
        int updatedScore = state.getScore()[player-1];
        int updatedScoreOp = player == 1 ? state.getScore()[1] : state.getScore()[0];

        // The difference between the updated and the previous score minus the difference between the updated and the
        // previous score of the opponent.
        output = updatedScore - prevScore - (updatedScoreOp - prevScoreOp);

        return output;
    }

    /**
     * Rates the quality of a meeple placement given a board, a tile and a move.
     * @param originalState The state on which the placement is to take place.
     * @param originalTile The tile to be rotated and placed.
     * @param player The ID of the player in question
     * @return A rating of the move. The higher, the better.
     */
    public int meepleHeuristic(GameState originalState, Tile originalTile, int placement, int player) {
        int output;

        GameState state = new GameState(originalState);
        Tile tile = new Tile(originalTile);

        int prevScore = state.getScore()[player-1];
        int prevScoreOp = player == 1 ? state.getScore()[1] : state.getScore()[0];

        state.placeMeeple(placement, player, tile);

        state.checkForScoreAfterRound(false);
        state.assignPointsAtEndOfGame();
        int updatedScore = state.getScore()[player-1];
        int updatedScoreOp = player == 1 ? state.getScore()[1] : state.getScore()[0];

        // The difference between the updated and the previous score minus the difference between the updated and the
        // previous score of the opponent.
        output = updatedScore - prevScore - (updatedScoreOp - prevScoreOp);

        return output;
    }

    public int getIndexOfBestMeeplePlacement(GameState state, Tile tile, List<Integer> placements, int player, Random random) {
        int index = -1;
        int maxValue = Integer.MIN_VALUE;

        for (int i = 0; i < placements.size(); i++) {
            int h = meepleHeuristic(state, tile, placements.get(i), player);
            if (h > maxValue) {
                maxValue = h;
                index = i;
            }
        }

        return index;
    }
}
