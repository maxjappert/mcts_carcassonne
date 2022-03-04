import java.util.*;

public class GameState {
    List<Tile> deck;
    List<List<Tile>> board;
    Player player1;
    Player player2;

    List<Integer> areaTypes;

    /**
     * Initialises a game object. Thereby the deck is assembled according to the game's instructions.
     */
    public GameState() {
        deck = new ArrayList<>();
        board = new ArrayList<>();
        assembleDeck();
        assert(deck.size() == 72);

        board.add(new ArrayList<>());

        Tile startingTile = new Tile(0, false);

        startingTile.getAreas()[0] = 0;
        startingTile.getAreas()[1] = 0;
        startingTile.getAreas()[2] = 1;
        startingTile.getAreas()[3] = 3;
        startingTile.getAreas()[4] = 2;
        startingTile.getAreas()[5] = 3;
        startingTile.getAreas()[6] = 1;
        startingTile.getAreas()[7] = 0;
        startingTile.getAreas()[8] = 1;

        // The starting tile as defined in the game's manual.
        board.get(0).add(startingTile);

        areaTypes = new ArrayList<>();
        areaTypes.add(0);
        areaTypes.add(2);
        areaTypes.add(1);
    }

    /**
     * Copy constructor.
     * @param state The GameState object for which a deep copy should be created.
     */
    public GameState(GameState state) {
        this.deck = state.deck;
        this.board = new ArrayList<>();
        this.player1 = state.player1;
        this.player2 = state.player2;
        this.areaTypes = new ArrayList<>();

        this.areaTypes.addAll(state.areaTypes);

        for (int i = 0; i < state.board.size(); i++) {
            this.board.add(new ArrayList<>());
            for (int j = 0; j < state.board.get(0).size(); j++) {
                this.board.get(i).add(state.board.get(i).get(j));
            }
        }
    }

    public Tile drawTile() {
        return deck.remove(0);
    }

    public List<List<Tile>> getBoard() {
        return board;
    }

    /**
     * Places the tile which the player drew from the deck onto the board.
     * @param move The coordinates of where the tile should be placed.
     * @param tile The tile which should be placed.
     */
    public void updateBoard(int[] move, Tile tile) {

        Map<Integer, Tile> neighbours = getNeighboursByType(tile, move, -1);

        //int[] areas = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1};

        // Assign the adjacent areas to each side of the tile.
        for (int side : neighbours.keySet()) {
            int[] adjacentSides = getAdjacentSides(side*2);

            int oppositeSide = GameStateSpace.getOppositeSide(side)*2;
            int oppositeArea = neighbours.get(side).getArea(oppositeSide);

            tile.setArea(side*2, oppositeArea);
            tile.setArea(adjacentSides[0], neighbours.get(side).getArea(adjacentSides[0]));
            tile.setArea(adjacentSides[1], neighbours.get(side).getArea(adjacentSides[1]));
        }

        int middleArea = -1;

        // Get the sides whose type matches the type of the middle.
        List<Integer> matchingSides = new ArrayList<>();
        List<Integer> matchingCorners = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (tile.getSides()[i] == tile.getMiddle()) {
                matchingSides.add(i);
            }

            if (tile.getCorners()[i] == tile.getMiddle()) {
                matchingCorners.add(i);
            }
        }

        for (int side : matchingSides) {
            if (tile.getArea(side*2) != -1) {
                middleArea = tile.getArea(side*2);
                // Set the middle area of the tile.
                tile.setArea(8, middleArea);
                break;
            }
        }

        for (int side : matchingSides) {
            tile.setArea(side*2, middleArea);
        }

        for (int corner : matchingCorners) {
            tile.setArea(corner*2 + 1, middleArea);
        }

        // Okay so this implementation is total bullshit and what remains of my rational thinking ability tells me
        // to take a break and go to bed before trying to redo this. But the idea is not that bad: I save all the connected areas
        // in a list in the game state. Each tile has an array which denotes to which area all sides and the middle belong
        // to. When trying to place a meeple on a side, I query all tiles which are involved in a given area if they have
        // a meeple which is placed on a side of the same type as the area. If yes, then I can be 90% sure that placing
        // the meeple is illegal. The only problems are tiles which have multiple sides of the same type without
        // being connected with each other. tl;dr: TODO: Implement a system which can actually assign each side to the area it belongs to.

        // Transfer the area to connected sides on the same tile.
        // TODO: What if tile connects two areas?
//        for (int side = 0; side < 4; side++) {
//            if (tile.getSides()[side] == tile.getMiddle()) {
//                if (tile.getAreas()[side] != -1) {
//                    middleArea = tile.getAreas()[side];
//                    break;
//                }
//            }
//        }
//
//        tile.getAreas()[4] = middleArea;
//
//        for (int side = 0; side < 4; side++) {
//            if (tile.getSides()[side] == tile.getMiddle()) {
//                tile.getAreas()[side] = middleArea;
//            }
//        }
//
//        for (int side = 0; side < 4; side++) {
//            if (areas[side] == -1) {
//                areas[side] = areaTypes.size();
//                areaTypes.add(tile.getSides()[side]);
//            }
//        }
//
//        tile.setAreas(areas);

        // This is the case where the tile generates a new top row.
        if (move[0] == 0) {
            board.add(0, new ArrayList<>());
            for (int i = 0; i < move[1] - 1; i++) {
                board.get(0).add(null);
            }
            board.get(0).add(tile);

            return;
        }

        // This is the case where the tile generates a new first column.
        if (move[1] == 0) {
            for (int i = 0; i < board.size(); i++) {
                List<Tile> row = board.get(i);
                if (i == move[0] - 1) {
                    row.add(0, tile);
                } else {
                    row.add(0, null);
                }
            }

            return;
        }

        // This is the case where the tile generates a new bottom row.
        if (move[0] == board.size() + 1) {
            List<Tile> newRow = new ArrayList<>();

            for (int i = 0; i < getBoardDimensions()[1]; i++) {
                if (move[1] - 1 == i) {
                    newRow.add(tile);
                } else {
                    newRow.add(null);
                }
            }

            board.add(newRow);
            return;
        }

        // This is the case where the tile generates a new last column
        if (move[1] == getBoardDimensions()[1] + 1) {
            for (int i = 0; i < getBoardDimensions()[0]; i++) {

                if (move[0] - 1 == i) {
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
        board.get(move[0] - 1).set(move[1] - 1, tile) ;
    }

    /**
     * Assembles the deck according to the game's instructions and then shuffles it.
     */
    private void assembleDeck() {
        addTilesToDeck(0, 4, false);
        addTilesToDeck(1, 3, false);
        addTilesToDeck(2, 3, false);
        addTilesToDeck(3, 3, false);
        addTilesToDeck(4, 5, false);
        addTilesToDeck(5, 3, false);
        addTilesToDeck(6, 2, false);
        addTilesToDeck(7, 8, false);
        addTilesToDeck(8, 9, false);
        addTilesToDeck(9, 4, false);
        addTilesToDeck(10, 1, false);
        addTilesToDeck(11, 2, false);
        addTilesToDeck(12, 4, false);
        addTilesToDeck(13, 1, false);
        addTilesToDeck(13, 2, true);
        addTilesToDeck(14, 3, false);
        addTilesToDeck(14, 2, true);
        addTilesToDeck(15, 3, false);
        addTilesToDeck(15, 2, true);
        addTilesToDeck(16, 1, false);
        addTilesToDeck(16, 2, true);
        addTilesToDeck(17, 3, false);
        addTilesToDeck(17, 1, true);
        addTilesToDeck(18, 1, true);

        // shuffle the deck
        Collections.shuffle(deck);
    }

    /**
     * Simply adds a given number of a given type's tiles to the deck.
     * @param type The type of tile to be added.
     * @param amount The number of tiles of the given type to be added.
     * @param pennant True if the tile includes a pennant.
     */
    private void addTilesToDeck(int type, int amount, boolean pennant) {
        for (int i = 0; i < amount; i++) {
            this.deck.add(new Tile(type, pennant));
        }
    }

    /**
     * Takes the tiles on the board and prints an ASCII-representation of the board. This is achieved by assembling
     * a 2D-char-array using the individual ASCII-representations of the relevant tiles.
     */
    public void displayBoard() throws Exception {
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

        // Here the labels which allow for naming tiles and therefore moves. 65 in decimal corresponds to 'A' in ASCII.
        char columnName = 49;
        for (int i = 0; i < boardDimensions[1] + 2; i++) {
            boardFormat[boardDimensions[0] * 5 + 10][i * 10 + 4] = columnName;
            columnName += 1;
        }

        // 49 in decimal corresponds to a 1 in ASCII.
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

    private int[] getAdjacentSides(int side) {
        int[] adjacentSides = new int[2];

        if (side == 0) {
            adjacentSides[0] = 1;
            adjacentSides[1] = 7;
        } else if (side == 7) {
            adjacentSides[0] = 0;
            adjacentSides[1] = 6;
        } else if (side == 8) {
            System.out.println("Weird call in getAdjacentSides(...)");
            adjacentSides[0] = 8;
            adjacentSides[1] = 8;
        } else {
            adjacentSides[0] = side + 1;
            adjacentSides[1] = side - 1;
        }

        return adjacentSides;
    }

    /**
     * @return Array of size 2 denoting the dimensions of the board: [height, width].
     */
    public int[] getBoardDimensions() {
        int maxWidth = board.get(0).size();

        for (List<Tile> row : board) {
            assert(row.size() == maxWidth);
        }

        return new int[] {board.size(), maxWidth};
    }

    public Tile getTile(int[] coordinates) {

        Tile tile = null;

        if (coordinates[0] >= 0 && coordinates[0] < getBoardDimensions()[0]
        && coordinates[1] >= 0 && coordinates[1] < getBoardDimensions()[1]) {
            tile = board.get(coordinates[0]).get(coordinates[1]);
        }

        return tile;
    }

    public int[] getCoordinates(Tile tile) {
        for (int i = 0; i < board.size(); i++) {
            for (int j = 0; j < board.get(0).size(); j++) {
                if (board.get(i).get(j).equals(tile)) {
                    return new int[]{i, j};
                }
            }
        }

        System.out.println("Tile not found in getCoordinates(...)");
        return null;
    }

    /**
     * If type == -1, then all neighbours are returned.
     * @param tile
     * @param move
     * @param type
     * @return
     */
    public Map<Integer, Tile> getNeighboursByType(Tile tile, int[] move, int type) {
        Map<Integer, Tile> neighbourMap = new HashMap<>();

        // Create a deep copy of the move array
        int[] tileCoords = Arrays.copyOf(move, move.length);

        tileCoords[0] -= 1;
        tileCoords[1] -= 1;

        if (getTile(new int[]{tileCoords[0] - 1, tileCoords[1]}) != null) {
            if (type == -1 || board.get(tileCoords[0] - 1).get(tileCoords[1]).getSides()[0] == type) {
                neighbourMap.put(2, board.get(tileCoords[0] - 1).get(tileCoords[1]));
            }
        }

        if (getTile(new int[]{tileCoords[0], tileCoords[1] - 1}) != null) {
            if (type == -1 || board.get(tileCoords[0]).get(tileCoords[1] - 1).getSides()[1] == type) {
                neighbourMap.put(3, board.get(tileCoords[0]).get(tileCoords[1] - 1));
            }
        }

        if (getTile(new int[]{tileCoords[0], tileCoords[1] + 1}) != null) {
            if (type == -1 || board.get(tileCoords[0]).get(tileCoords[1] + 1).getSides()[3] == type) {
                neighbourMap.put(1, board.get(tileCoords[0]).get(tileCoords[1] + 1));
            }
        }

        if (getTile(new int[]{tileCoords[0] + 1, tileCoords[1]}) != null) {
            if (type == -1 || board.get(tileCoords[0] + 1).get(tileCoords[1]).getSides()[2] == type) {
                neighbourMap.put(0, board.get(tileCoords[0] + 1).get(tileCoords[1]));

            }
        }

            return neighbourMap;
    }
}
