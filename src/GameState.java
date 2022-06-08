//import org.slf4j.//logger;
//import org.slf4j.//loggerFactory;

import java.util.*;

public class GameState {
    private final List<List<Tile>> board;

    private final Map<Integer, Integer> areaTypes;
    private final List<Integer> completedCities;
    private int areaCounter;

    private final int[] scores;
    private final int[] numMeeples;

    private int deckSize;

    /**
     * Initialises a game object. Thereby the deck is assembled according to the game's instructions.
     */
    public GameState() {
        board = new ArrayList<>();
        numMeeples = new int[]{7, 7};

        deckSize = 71;

        board.add(new ArrayList<>());

        scores = new int[]{0, 0};

        Tile startingTile = new Tile(0, false);

        startingTile.setArea(0, 0);
        startingTile.setArea(1, 0);
        startingTile.setArea(2, 0);
        startingTile.setArea(3, 0);
        startingTile.setArea(4, 1);
        startingTile.setArea(5, 2);
        startingTile.setArea(6, 3);
        startingTile.setArea(7, 3);
        startingTile.setArea(8, 3);
        startingTile.setArea(9, 2);
        startingTile.setArea(10, 1);
        startingTile.setArea(11, 0);

        // The starting tile as defined in the game's manual.
        board.get(0).add(startingTile);

        areaTypes = new HashMap<>();
        areaTypes.put(0, 0);
        areaTypes.put(1, 2);
        areaTypes.put(2, 1);
        areaTypes.put(3, 0);
        areaCounter = 4;

        completedCities = new ArrayList<>();
    }

    public void placeMeeple(int side, int playerID, Tile tile) {
        if (getNumMeeples(playerID) > 0) {
            removeMeeple(playerID);
            tile.placeMeeple(side, playerID);
        }
    }

    /**
     * Copy constructor.
     * @param state The GameState object for which a deep copy should be created.
     */
    public GameState(GameState state) {
        this.board = new ArrayList<>();

        this.deckSize = state.deckSize;
        this.areaCounter = state.areaCounter;

        this.areaTypes = new HashMap<>(Map.copyOf(state.areaTypes));

        this.completedCities = new ArrayList<>(List.copyOf(state.completedCities));

        this.scores = Arrays.copyOf(state.scores, state.scores.length);
        this.numMeeples = Arrays.copyOf(state.numMeeples, 2);

        for (int i = 0; i < state.board.size(); i++) {
            this.board.add(new ArrayList<>());

            for (int j = 0; j < state.board.get(0).size(); j++) {
                if (state.board.get(i).get(j) == null) {
                    this.board.get(i).add(null);
                } else {
                    this.board.get(i).add(new Tile(state.board.get(i).get(j)));
                }
            }
        }
    }

    /**
     * Places the tile which the player drew from the deck onto the board. This method is a collection of bodges, but
     * it works.
     * @param coords The coordinates of where the tile should be placed.
     * @param tile The tile which should be placed.
     */
    public void updateBoard(Coordinates coords, Tile tile) {

        deckSize--;

        tile.resetAreas();

        if (tile.getMiddle() == 4) {
            tile.setArea(12, assignNewArea(4));
        }

        Map<Integer, Tile> neighbours = getNeighboursByType(coords, false);

        for (int side : neighbours.keySet()) {
            int point1 = side * 3;
            int point2 = side * 3 + 1;
            int point3 = side * 3 + 2;

            Tile neighbour = neighbours.get(side);

            tile.setArea(point1, getOppositeArea(point1, neighbour));
            tile.setArea(point2, getOppositeArea(point2, neighbour));
            tile.setArea(point3, getOppositeArea(point3, neighbour));
        }

        List<TreeSet<Integer>> adjacentPointsOfSameType = new ArrayList<>();
        adjacentPointsOfSameType.add(new TreeSet<>());
        adjacentPointsOfSameType.get(0).add(0);

        List<Integer> indices = new ArrayList<>();

        int runner = 0;

        if (tile.getPoint(0) == tile.getMiddle()) {
            indices.add(runner);
        }

        for (int point = 0; point < 11; point++) {
            // Assign areas to all points which don't have an area yet.

            if (tile.getPoint(point) == tile.getPoint(point+1)) {
                adjacentPointsOfSameType.get(runner).add(point);
                adjacentPointsOfSameType.get(runner).add(point+1);
            } else {
                runner++;
                adjacentPointsOfSameType.add(new TreeSet<>());
                adjacentPointsOfSameType.get(runner).add(point+1);

                if (tile.getPoint(point+1) == tile.getMiddle()) {
                    indices.add(runner);
                }
            }
        }

        if (tile.getPoint(11) == tile.getPoint(0) && runner > 0) {
            adjacentPointsOfSameType.get(0).addAll(adjacentPointsOfSameType.get(runner));
            adjacentPointsOfSameType.remove(runner);

            if (indices.contains(adjacentPointsOfSameType.size())) {
                indices.remove(indices.size()-1);
            }
        }

        adjacentPointsOfSameType.removeIf(TreeSet::isEmpty);

        // Merge all sets of adjacent points of the same type for the types corresponding to the middle.

        for (int i = 1; i < indices.size(); i++) {
            adjacentPointsOfSameType.get(indices.get(0)).addAll(adjacentPointsOfSameType.get(indices.get(i)));
            int index = indices.get(i);
            adjacentPointsOfSameType.remove(index);
        }

        List<Integer> areas = new ArrayList<>();

        for (Set<Integer> set : adjacentPointsOfSameType) {
            int area = Integer.MAX_VALUE;
            int type = -1;
            Set<Integer> inheritedAreas = new TreeSet<>();

            for (int point : set) {
                type = tile.getPoint(point);

                if (/*tile.getArea(point) < area && */tile.getArea(point) != -1) {
                    inheritedAreas.add(tile.getArea(point));

                    // If adjacent points of the same type inherit from different areas, then this tile connects
                    // those two areas and consequently we must replace the one of the areas. In our case this is
                    // the area with the higher area code with the area with the lower area code.
                    if (inheritedAreas.size() > 1) {
                        int replacedArea = Collections.max(inheritedAreas);
                        int newArea = Collections.min(inheritedAreas);
                        replaceArea(replacedArea, newArea);

                        for (int i = 0; i <= 12; i++) {
                            if (tile.getArea(i) == replacedArea) {
                                tile.setArea(i, newArea);
                            }
                        }

                        //inheritedAreas.remove(replacedArea);
                    }

                    area = tile.getArea(point);
                }
            }

            if (area == Integer.MAX_VALUE) {
                area = assignNewArea(type);
            }

            areas.add(area);
        }

        for (int i = 0; i < adjacentPointsOfSameType.size(); i++) {
            Set<Integer> set = adjacentPointsOfSameType.get(i);

            for (int point : set) {
                tile.setArea(point, areas.get(i));
            }
        }

        for (int i = 0; i < 12; i++) {
            if (tile.getPoint(i) == tile.getMiddle()) {
                tile.setMiddleArea(tile.getArea(i));
                break;
            }
        }

        // Yeah, here's where the bodges start again... This is handling the case of tile 15, where there's a road
        // without the middle of the tile being a road. This is a special case and differs from all other road tiles.
        if (tile.getType() == 15) {
            int roadArea = Integer.MAX_VALUE;

            for (int i = 0; i < 4; i++) {
                if (tile.getPoint(i*3 + 1) == 2 && tile.getArea(i*3 + 1) < roadArea) {
                    roadArea = tile.getArea(i*3 + 1);
                }
            }

            for (int i = 0; i < 4; i++) {
                if (tile.getPoint(i*3 + 1) == 2) {
                    tile.setArea(i*3 + 1, roadArea);
                }
            }
        }

        //--------------------------

        // This is the case where the tile generates a new top row.
        if (coords.x == 0) {
            board.add(0, new ArrayList<>());
            for (int i = 0; i < coords.y - 1; i++) {
                board.get(0).add(null);
            }
            board.get(0).add(tile);

            for (int i = coords.y; i < getBoardDimensions()[1]; i++) {
                board.get(0).add(null);
            }

            return;
        }

        // This is the case where the tile generates a new first column.
        if (coords.y == 0) {
            for (int i = 0; i < board.size(); i++) {
                List<Tile> row = board.get(i);
                if (i == coords.x - 1) {
                    row.add(0, tile);
                } else {
                    row.add(0, null);
                }
            }

            return;
        }

        // This is the case where the tile generates a new bottom row.
        if (coords.x == board.size() + 1) {
            List<Tile> newRow = new ArrayList<>();

            for (int i = 0; i < getBoardDimensions()[1]; i++) {
                if (coords.y - 1 == i) {
                    newRow.add(tile);
                } else {
                    newRow.add(null);
                }
            }

            board.add(newRow);
            return;
        }

        // This is the case where the tile generates a new last column
        if (coords.y == getBoardDimensions()[1] + 1) {
            for (int i = 0; i < getBoardDimensions()[0]; i++) {

                if (coords.x - 1 == i) {
                    board.get(i).add(tile);
                } else {
                    board.get(i).add(null);
                }
            }

            return;
        }

        // In the case that the board dimensions remain the same, we simply place the tile at the coordinates given
        // by the move. These need to be subtracted by 1, since the user has to consider the additional potential row
        // and column.
        board.get(coords.x - 1).set(coords.y - 1, tile);
    }

    /**
     * Takes the tiles on the board and prints an ASCII-representation of the board. This is achieved by assembling
     * a 2D-char-array using the individual ASCII-representations of the relevant tiles.
     */
    public void displayBoard() {

        int[] boardDimensions = getBoardDimensions();

        char[][] boardFormat = new char[boardDimensions[0] * 5 + 1 + 10][boardDimensions[1] * 10 + 1 + 20];

        // loop over the rows
        for (int rowIndex = 0; rowIndex < boardDimensions[0]; rowIndex++) {
            List<Tile> row = board.get(rowIndex);
            //loop over the rows within each row
            for (int charRowIndex = 0; charRowIndex < 5; charRowIndex++) {
                // loop over the tiles in each row
                for (int columnIndex = 0; columnIndex < boardDimensions[1]; columnIndex++) {
                    Tile t = row.get(columnIndex);
                    if (t == null) {
                        // loop over the characters in each row of characters
                        for (int charColumnIndex = 0; charColumnIndex < 10; charColumnIndex++) {
                            boardFormat[rowIndex * 5 + charRowIndex + 5][columnIndex * 10 + charColumnIndex + 10] = ' ';
                        }
                    } else {
                        char[][] tileFormat = t.getPrintFormatOfTile();
                        // loop over the characters in each row of characters
                        for (int charColumnIndex = 0; charColumnIndex < 5; charColumnIndex++) {

                            // These if-conditions are just to make the horizontal roads more dense.

                            if (tileFormat[charRowIndex][charColumnIndex] == '-' && charColumnIndex != 4) {
                                boardFormat[rowIndex * 5 + charRowIndex + 5][columnIndex * 10 + charColumnIndex * 2 + 10] = '-';
                                boardFormat[rowIndex * 5 + charRowIndex + 5][columnIndex * 10 + charColumnIndex * 2 + 1 + 10] = '-';
                            } else {
                                boardFormat[rowIndex * 5 + charRowIndex + 5][columnIndex * 10 + charColumnIndex * 2 + 10] = tileFormat[charRowIndex][charColumnIndex];
                                boardFormat[rowIndex * 5 + charRowIndex + 5][columnIndex * 10 + charColumnIndex * 2 + 1 + 10] = ' ';
                            }

                            if (charColumnIndex == 3 && boardFormat[rowIndex * 5 + charRowIndex + 5][columnIndex * 10 + charColumnIndex * 2 + 10] == '-') {
                                boardFormat[rowIndex * 5 + charRowIndex + 5][columnIndex * 10 + charColumnIndex * 2 + 10 - 1] = '-';
                            }
                        }
                    }
                }
            }
        }

        // This is a horribly inefficient O(n^2), yet luckily the board size doesn't really grow all that much and the method
        // is only called once per round.
        for (int i = 0; i < boardFormat.length; i++) {
            for (int j = 0; j < boardFormat[0].length; j++) {
                if (boardFormat[i][j] == 0) {
                    boardFormat[i][j] = ' ';
                }
            }
        }

        // 49 in decimal corresponds to a 1 in ASCII.
        char columnName = 49;
        for (int i = 0; i < boardDimensions[1] + 2; i++) {
            if (columnName <= 57) {
                boardFormat[boardDimensions[0] * 5 + 10][i * 10 + 4] = columnName;
            } else {
                boardFormat[boardDimensions[0] * 5 + 10][i * 10 + 4] = 49;
                boardFormat[boardDimensions[0] * 5 + 10][i * 10 + 5] = (char) (columnName - 10);
            }
            columnName += 1;
        }

        // Here the labels which allow for naming tiles and therefore moves. 65 in decimal corresponds to 'A' in ASCII.
        char rowName = 65;
        for (int i = 0; i < boardDimensions[0] + 2; i++) {
            boardFormat[i * 5 + 2][boardDimensions[1] * 10 + 20] = rowName;
            rowName += 1;
        }

        // Here the 2D-char-array is converted into a string which can be printed.
        String boardString = "";

        for (char[] line : boardFormat) {
            boardString = boardString.concat(new String(line));
            boardString = boardString.concat("\n");
        }

        System.out.println(boardString);
    }

    /**
     * @return Array of size 2 denoting the dimensions of the board: [height, width].
     */
    public int[] getBoardDimensions() {
        int maxWidth = board.get(0).size();

        for (List<Tile> row : board) {
            if (row.size() > maxWidth) {
                maxWidth = row.size();
            }
        }

        return new int[] {board.size(), maxWidth};
    }

    /**
     * @param coordinates The coordinates of the tile.
     * @return The tile at the given coordinates on the board.
     */
    public Tile getTile(Coordinates coordinates) {

        Tile tile = null;

        if (coordinates.x >= 0 && coordinates.x < getBoardDimensions()[0]
        && coordinates.y >= 0 && coordinates.y < getBoardDimensions()[1]) {
            tile = board.get(coordinates.x).get(coordinates.y);
        }

        return tile;
    }

    /**
     * Finds the coordinates of a given tile on the board.
     * @param tile The tile at the given coordinates.
     * @return The coordinates of the given tile as an int array of length 2.
     */
    public Coordinates getCoordinates(Tile tile) {
        for (int i = 0; i < board.size(); i++) {
            for (int j = 0; j < board.get(0).size(); j++) {
                if (board.get(i).get(j) != null && board.get(i).get(j).equals(tile)) {
                    return new Coordinates(i, j);
                }
            }
        }

        return null;
    }

    /**
     * @param move The coordinates of which the neighbours are to be determined.
     * @return All neighbouring tiles which are connected along the given type. If type == -1, then all neighbours are returned.
     */
    public Map<Integer, Tile> getNeighboursByType(Coordinates move, boolean monasteryNeighbours) {
        Map<Integer, Tile> neighbourMap = new HashMap<>();

        Coordinates tileCoords = new Coordinates(move.x, move.y);

        // We only need to subtract 1 if the coordinates reference the placement space which considers a first row
        // which doesn't exist in the board space. If we want to check for the monastery neighbours, we check in the
        // board space.
        if (!monasteryNeighbours) {
            tileCoords.x -= 1;
            tileCoords.y -= 1;
        }

        if (getTile(new Coordinates(tileCoords.x - 1, tileCoords.y)) != null) {
                neighbourMap.put(2, board.get(tileCoords.x - 1).get(tileCoords.y));
        }

        if (getTile(new Coordinates(tileCoords.x, tileCoords.y - 1)) != null) {
                neighbourMap.put(3, board.get(tileCoords.x).get(tileCoords.y - 1));
        }

        if (getTile(new Coordinates(tileCoords.x, tileCoords.y + 1)) != null) {
                neighbourMap.put(1, board.get(tileCoords.x).get(tileCoords.y + 1));
        }

        if (getTile(new Coordinates(tileCoords.x + 1, tileCoords.y)) != null) {
                neighbourMap.put(0, board.get(tileCoords.x + 1).get(tileCoords.y));
        }

        //-----------------------------------------------------

        if (monasteryNeighbours && getTile(new Coordinates(tileCoords.x - 1, tileCoords.y - 1)) != null) {
            neighbourMap.put(4, board.get(tileCoords.x - 1).get(tileCoords.y - 1));
        }

        if (monasteryNeighbours && getTile(new Coordinates(tileCoords.x + 1, tileCoords.y - 1)) != null) {
            neighbourMap.put(5, board.get(tileCoords.x + 1).get(tileCoords.y - 1));
        }

        if (monasteryNeighbours && getTile(new Coordinates(tileCoords.x + 1, tileCoords.y + 1)) != null) {
            neighbourMap.put(6, board.get(tileCoords.x + 1).get(tileCoords.y + 1));
        }

        if (monasteryNeighbours && getTile(new Coordinates(tileCoords.x - 1, tileCoords.y + 1)) != null) {
            neighbourMap.put(7, board.get(tileCoords.x - 1).get(tileCoords.y + 1));
        }

        return neighbourMap;
    }

    /**
     * @param point The point index whose opposite should be returned.
     * @return The point index opposite the given point index.
     */
    private int getOppositePoint(int point) {
        int oppositePoint;

        if (point == 0) {
            oppositePoint = 8;
        } else if (point == 1) {
            oppositePoint = 7;
        } else if (point == 2) {
            oppositePoint = 6;
        } else if (point == 3) {
            oppositePoint = 11;
        } else if (point == 4) {
            oppositePoint = 10;
        } else if (point == 5) {
            oppositePoint = 9;
        } else if (point == 6) {
            oppositePoint = 2;
        } else if (point == 7) {
            oppositePoint = 1;
        } else if (point == 8) {
            oppositePoint = 0;
        } else if (point == 9) {
            oppositePoint = 5;
        } else if (point == 10) {
            oppositePoint = 4;
        } else if (point == 11) {
            oppositePoint = 3;
        } else {
            oppositePoint = -1;
        }

        return oppositePoint;
    }

    private int getOppositeArea(int point, Tile neighbour) {
        return neighbour.getArea(getOppositePoint(point));
    }

    private int assignNewArea(int type) {
        areaTypes.put(areaCounter, type);
        return areaCounter++;
    }

    /**
     * Replaces an area with another area.
     * @param replacedArea The area which will be replaced.
     * @param newArea The area which will replace the other area.
     */
    private void replaceArea(int replacedArea, int newArea) {
        for (List<Tile> row : board) {
            for (Tile tile : row) {
                for (int i = 0; i <= 12; i++) {
                    if (tile != null && tile.getArea(i) == replacedArea) {
                        tile.setArea(i, newArea);
                    }
                }
            }
        }

        areaTypes.remove(replacedArea);
    }

    public List<Tile> getTilesOfArea(int area) {
        List<Tile> tiles = new ArrayList<>();

        for (List<Tile> row : board) {
            for (Tile tile : row) {
                for (int i = 0; i < 12; i++) {
                    if (tile != null && tile.getArea(i) == area) {
                        tiles.add(tile);
                        break;
                    }
                }
            }
        }

        return tiles;
    }

    public void checkForScoreAfterRound(boolean verbose) {
        for (List<Tile> row : board) {
            for (Tile tile : row) {
                if (tile != null && tile.hasMeeple()) {

                    // If the tile has a monastery
                    if (tile.getMiddle() == 4 && tile.getMeeple()[0] == 12) {
                        Map<Integer, Tile> neighbours = getNeighboursByType(getCoordinates(tile), true);
                        if (neighbours.size() >= 8) {
                            //getPlayer(tile.getMeeple()[1], player1, player2).currentPoints += 9;
                            scores[tile.getMeeple()[1] - 1] += 9;
                            //getPlayer(tile.getMeeple()[1], player1, player2).numberOfMeeples += 1;
                            numMeeples[tile.getMeeple()[1] - 1] += 1;
                            if (verbose && Engine.verbose) System.out.println("Monastery completed! Player " + tile.getMeeple()[1] + " has gained 9 points.");
                            tile.removeMeeple();
                            return;
                        }
                    }

                    if (tile.getPoint(tile.getMeeple()[0]) == 1) {
                        int points = checkForCityCompletion(tile.getArea(tile.getMeeple()[0]));
                        if (points != 0) {
                            //getPlayer(tile.getMeeple()[1], player1, player2).currentPoints += points;
                            scores[tile.getMeeple()[1] - 1] += points;
                            //getPlayer(tile.getMeeple()[1], player1, player2).numberOfMeeples += 1;
                            numMeeples[tile.getMeeple()[1] - 1] += 1;
                            if (verbose && Engine.verbose) System.out.println("City completed! Player " + tile.getMeeple()[1] + " has gained " + points + " points.");
                            completedCities.add(tile.getArea(tile.getMeeple()[0]));
                            tile.removeMeeple();
                            return;
                        }
                    }

                    if (tile.getPoint(tile.getMeeple()[0]) == 2) {
                        int points = checkForRoadCompletion(tile.getArea(tile.getMeeple()[0]));
                        if (points != 0) {
                            //getPlayer(tile.getMeeple()[1], player1, player2).currentPoints += points;
                            scores[tile.getMeeple()[1] - 1] += points;
                            //getPlayer(tile.getMeeple()[1], player1, player2).numberOfMeeples += 1;
                            numMeeples[tile.getMeeple()[1] - 1] += 1;
                            if (verbose && Engine.verbose) System.out.println("Road completed! Player " + tile.getMeeple()[1] + " has gained " + points + " points.");
                            tile.removeMeeple();
                            return;
                        }
                    }

                }
            }
        }
    }

    public void removeMeeple(int player) {
        numMeeples[player-1]--;
    }

    public int getNumMeeples(int player) {
        return numMeeples[player-1];
    }

    private Set<Integer> getTilesWithOneSidedCity() {
        Set<Integer> tilesWithOneSidedCity = new TreeSet<>();

        tilesWithOneSidedCity.add(0);
        tilesWithOneSidedCity.add(1);
        tilesWithOneSidedCity.add(2);
        tilesWithOneSidedCity.add(3);
        tilesWithOneSidedCity.add(4);

        return tilesWithOneSidedCity;
    }

    /**
     *
     * @param area The area in question.
     * @return 0 if the city hasn't been completed, otherwise the number of points which the city has generated.
     */
    private int checkForCityCompletion(int area) {
        List<Tile> tiles = getTilesOfArea(area);

        Set<Integer> tilesWithOneSidedCity = getTilesWithOneSidedCity();

        int nrOfPennants = 0;
        int numberOfFinishersNeeded = 2;

        for (Tile tile : tiles) {
            int tileCode = tile.getType();
            if (tilesWithOneSidedCity.contains(tileCode)) {
                numberOfFinishersNeeded -= 1;
            } else if (tileCode == 18) {
                numberOfFinishersNeeded += 2;
            } else if (tileCode == 16 || tileCode == 17) {
                numberOfFinishersNeeded += 1;
            }

            if (tile.hasPennant()) {
                nrOfPennants++;
            }
        }

        if (numberOfFinishersNeeded != 0) {
            return 0;
        } else {
            return (tiles.size() * 2) + (nrOfPennants * 2);
        }
    }

    private int checkForRoadCompletion(int area) {
        List<Tile> tiles = getTilesOfArea(area);

        int endPoints = 0;

        for (Tile tile : tiles) {
            if (tile.getType() == 16 || tile.getType() == 11) {
                endPoints += 1;
            } else if (tile.getType() == 3 || tile.getType() == 9 || tile.getType() == 10) {
                int numRoadsOfGivenType = 0;

                endPoints++;

                if (tile.getArea(1) == area) {
                    numRoadsOfGivenType += 1;
                }

                if (tile.getArea(4) == area) {
                    numRoadsOfGivenType += 1;
                }

                if (tile.getArea(7) == area) {
                    numRoadsOfGivenType += 1;
                }

                if (tile.getArea(10) == area) {
                    numRoadsOfGivenType += 1;
                }

                if (numRoadsOfGivenType == 2) {
                    endPoints = 2;
                }
            }
        }

        if (endPoints == 2) {
            return tiles.size();
        } else if (endPoints < 2) {
            return 0;
        } else {
            ////logger.error("Weird return value in checkForRoadCompletion(...)");
            return -1;
        }
    }

    public void assignPointsAtEndOfGame() {
        for (int i : areaTypes.keySet()) {
            // If the area is a field, then we need to evaluate it.
            if (areaTypes.get(i) == 0) {
                List<Tile> tilesOfArea = getTilesOfArea(i);
                List<Integer> consideredCities = new ArrayList<>();
                int numAdjacentCities = 0;

                for (Tile tile : tilesOfArea) {
                    for (int point : tile.getPoints()) {
                        if (completedCities.contains(tile.getArea(point)) && !consideredCities.contains(tile.getArea(point))) {
                            numAdjacentCities++;
                            consideredCities.add(tile.getArea(point));
                        }
                    }
                }

                for (int playerNr : getAreaOwners(i)) {
                    scores[playerNr - 1] += numAdjacentCities * 3;
                }
            }

            if (areaTypes.get(i) == 1 || areaTypes.get(i) == 2) {
                //if (getAreaOwners(i).length != 0) System.out.println(Arrays.toString(getAreaOwners(i)));
                for (int playerNr : getAreaOwners(i)) {

                    List<Tile> tilesOfArea = getTilesOfArea(i);

                    scores[playerNr - 1] += tilesOfArea.size();
                }
            }

            if (areaTypes.get(i) == 4) {
                for (int playerNr : getAreaOwners(i)) {
                    for (List<Tile> row : board) {
                        for (Tile tile : row) {
                            if (tile.getArea(12) == 4) {
                                Map<Integer, Tile> monasteryTiles = getNeighboursByType(getCoordinates(tile), true);
                                scores[playerNr - 1] += monasteryTiles.size();
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private int[] getAreaOwners(int area) {
        int numMeeplesP1 = 0;
        int numMeeplesP2 = 0;

        List<Tile> tilesInArea = getTilesOfArea(area);

        for (Tile tile : tilesInArea) {
            if (tile.getMeeple()[1] == 1 && area == tile.getArea(tile.getMeeple()[0])) {
                numMeeplesP1++;
            } else if (tile.getMeeple()[1] == 2 && area == tile.getArea(tile.getMeeple()[0])) {
                numMeeplesP2++;
            }
        }

        if (numMeeplesP1 > numMeeplesP2) {
            return new int[]{1};
        } else if (numMeeplesP2 > numMeeplesP1) {
            return new int[]{2};
        } else if (numMeeplesP1 == 0 && numMeeplesP2 == 0) {
            return new int[0];
        } else {
            return new int[]{1, 2};
        }
    }

    public int[] getScore() {
        return scores;
    }

    public int getPlayer() {
        if (deckSize % 2 == 1) {
            return 1;
        } else {
            return 2;
        }
    }

    public int getDeckSize() {
        return deckSize;
    }
}
