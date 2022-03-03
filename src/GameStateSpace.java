import java.util.*;

import static java.lang.Math.abs;

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
     *
     * @param state The state in question.
     * @param tile The tile in question.
     * @return Subset of {0, 1, 2, 3, 4}, denoting the sides on which meeples can be placed.
     */
    public List<Integer> legalMeeples(GameState state, int[] move, Tile tile) {
        List<Integer> placements = new ArrayList<>();

        for (int side = 0; side < 5; side++) {
            if (checkIfLegalMeeplePlacement(tile, move, side, state)) {
                placements.add(side);
                System.out.println(side);
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

    private boolean checkIfLegalMeeplePlacement(Tile tile, int[] move, int side, GameState state) {
        assert (side >= 0 && side < 5);

        int areaID;

        if (side == 4 && tile.getMiddle() == 3) {
            // One cannot place a meeple on an intersection.
            return false;
        } else if (side == 4 && tile.getMiddle() == 4) {
            // Placing a meeple of a monastery is always allowed.
            return true;
        } if (side == 4) {
            areaID = tile.getMiddle();
        } else {
            areaID = tile.getSides()[side];
        }

        for (Tile otherTile : getTilesOfArea(areaID, state)) {
            int meepleLocation = otherTile.getMeeple()[0];

            if (meepleLocation != -1) {
                return false;
            }
        }

        return true;
    }

//        } else if (side == 4) {
//            areaID = tile.getMiddle();
//        } else {
//            areaID = tile.getAreas()[side];
//        }

//        List<Tile> tilesInSameArea = getTilesOfArea(areaID, state);
//
//        // TODO: this really doesn't always work.
//
//        for (Tile otherTile : tilesInSameArea) {
//            if (otherTile.getMeeple()[0] != -1) {
//                int meepleSide = otherTile.getMeeple()[0];
//
//                if (otherTile.getAreas()[meepleSide] == areaID) {
//                    return false;
//                }
//            }
//        }
//
//        return true;

    private List<Tile> getTilesOfArea(int areaID, GameState state) {
        List<Tile> tiles = new ArrayList<>();

        for (List<Tile> row : state.getBoard()) {
            for (Tile tile : row) {
                if (tile != null) {
                    for (int area : tile.getAreas()) {
                        if (area == areaID) {
                            tiles.add(tile);
                            break;
                        }
                    }
                }
            }
        }

        return tiles;
    }

//    private boolean checkIfLegalMeeplePlacement(Tile tile, int[] move, int side, GameState state) {
//
//        assert (side >= 0 && side < 5);
//
//        if (side == 4 && tile.getMiddle() == 3) {
//            // One cannot place a meeple on an intersection.
//            return false;
//        } else if (side == 4 && tile.getMiddle() == 4) {
//            // Placing a meeple of a monastery is always allowed.
//            return true;
//        }
//
//        int type;
//
//        if (side < 4) {
//            type = tile.getSides()[side];
//        } else {
//            type = tile.getMiddle();
//        }
//
//        List<Tile> visited = new ArrayList<>();
//        visited.add(tile);
//
//        int[] moveCopy = Arrays.copyOf(move, move.length);
//
//        //moveCopy[0] -= 1;
//        //moveCopy[1] -= 1;
//
//        return checkForMeeples(tile, moveCopy, type, state, visited);
//    }
//
//    private boolean checkForMeeples(Tile tile, int[] move, int type, GameState state, List<Tile> explored) {
//        Map<Integer, Tile> connectedTiles = state.getNeighboursByType(tile, move, type);
//
//        // Iterate over all the connected tiles.
//        for (int side : connectedTiles.keySet()) {
//
//            Tile connectedTile = connectedTiles.get(side);
//
//            // If we've already explored the tile, we skip it. If we haven't, we add it to the list of explored tiles.
//            if(explored.contains(connectedTile)) {
//                continue;
//            } else {
//                explored.add(connectedTile);
//            }
//
//            // If the connected tile has a meeple...
//            if (connectedTile.getMeeple()[0] != -1) {
//                int meepleType = connectedTile.getMeeple()[0];
//                // ...check if either the meeple is placed right opposite...
//                if (connectedTile.getMeeple()[0] == getOppositeSide(side)) {
//                    return false;
//                    // ...or if it's placed on a side which is connected.
//                } else if (connectedTile.getSides()[connectedTile.getMeeple()[0]] == type) {
//                    // I.e., either the meeple is connected through the middle or through adjacent sides.
//                    // TODO: Tiles of type 6 aren't recognised correctly.
//                    if (connectedTile.getMiddle() == type) {
//                        return false;
//                    } else if (connectedTile.getMeeple()[0] == getAdjacentSides(getOppositeSide(side))[0]
//                            || connectedTile.getMeeple()[0] == getAdjacentSides(getOppositeSide(side))[1]) {
//                        return false;
//                    }
//                }
//            }
//
//            int[] newMove = Arrays.copyOf(move, move.length);
//
//            if (side == 0) {
//                newMove[0] += 1;
//            } else if (side == 1) {
//                newMove[1] += 1;
//            } else if (side == 2) {
//                newMove[0] -= 1;
//            } else if (side == 3) {
//                newMove[1] -= 1;
//            }
//
//            // We should only reach here if the next tile doesn't have a meeple on a connected area. In that case, we
//            // recursively check all connected tiles in a similar way.
//            checkForMeeples(connectedTile, newMove, type, state, explored);
//        }
//
//        return true;
//    }

    public static int getOppositeSide(int side) {
        if (side == 4) {
            return 4;
        } else if (side == 1) {
            return 3;
        } else {
            return abs(side - 2);
        }
    }

    private int[] getAdjacentSides(int side) {
        int[] adjacentSides = new int[2];

        if (side == 4) {
            adjacentSides[0] = side;
            adjacentSides[1] = side;
            return adjacentSides;
        } else if (side == 3) {
            adjacentSides[0] = 0;
            adjacentSides[1] = 2;
        } else if (side == 2) {
            adjacentSides[0] = 3;
            adjacentSides[1] = 1;
        } else if (side == 1) {
            adjacentSides[0] = 2;
            adjacentSides[1] = 0;
        } else if (side == 0) {
            adjacentSides[0] = 1;
            adjacentSides[1] = 3;
        } else {
            System.out.println("Error in getAdjacentSides(...)");
        }

        return adjacentSides;
    }
}
