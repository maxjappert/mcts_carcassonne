import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Game {
    List<Tile> deck;
    List<List<Tile>> board;
    Player player1;
    Player player2;

    /**
     * Initialises a game object. Thereby the deck is assembled according to the game's instructions.
     */
    public Game() throws Exception {
        deck = new ArrayList<>();
        board = new ArrayList<>();
        assembleDeck();
        assert(deck.size() == 72);

        // Initialise the player as either humans or robots.
        player1 = new HumanPlayer();
        player2 = new HumanPlayer();

        board.add(new ArrayList<>());

        // The starting tile as defined in the game's manual.
        board.get(0).add(new Tile(0, false));

        displayBoard();
    }

    public void play() throws Exception {
        while (true) {
            displayBoard();

            player1.draw(deck);

            int[] move = player1.decideOnNextMove(board);

            updateBoard(move, player1);
        }
    }

    /**
     * Places the tile which the player drew from the deck onto the board.
     * @param move The coordinates of where the tile should be placed.
     * @param p The player who executed the move.
     */
    private void updateBoard(int[] move, Player p) {
        // This is the case where the tile generates a new top row.
        if (move[0] == 0) {
            board.add(0, new ArrayList<>());
            for (int i = 0; i < move[1] - 1; i++) {
                board.get(0).add(null);
            }
            board.get(0).add(p.getDrawnTile());

            return;
        }

        // This is the case where the tile generates a new first column.
        if (move[1] == 0) {
            for (int i = 0; i < board.size(); i++) {
                List<Tile> row = board.get(i);
                if (i == move[0] - 1) {
                    row.add(0, p.getDrawnTile());
                } else {
                    row.add(0, null);
                }
            }

            return;
        }

        // This is the case where the tile generates a new bottom row.
        if (move[0] == board.size() + 1) {
            List<Tile> newRow = new ArrayList<>();

            for (int i = 0; i < getBoardDimensions(board)[1]; i++) {
                if (move[1] - 1 == i) {
                    newRow.add(p.getDrawnTile());
                } else {
                    newRow.add(null);
                }
            }

            board.add(newRow);
            return;
        }

        // This is the case where the tile generates a new last column
        if (move[1] == getBoardDimensions(board)[1] + 1) {
            for (int i = 0; i < getBoardDimensions(board)[0]; i++) {

                if (move[0] - 1 == i) {
                    board.get(i).add(p.getDrawnTile());
                } else {
                    board.get(i).add(null);
                }
            }

            return;
        }

        // In the case that the board dimensions remain the same, we simply place the tile at the coordinates given
        // by the move. These need to be subtracted by 1, since the user has to consider the additional potential row
        // and column.
        board.get(move[0] - 1).set(move[1] - 1, p.getDrawnTile()) ;
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
     * a 2D-char-array using the individual ASCII-representations of the relevant tile.
     */
    public void displayBoard() throws Exception {
        int[] boardDimensions = Game.getBoardDimensions(board);

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
                            boardFormat[rowIndex * 5 + charRowIndex + 5][columnIndex * 10 + charColumnIndex * 2 + 10] = tileFormat[charRowIndex][charColumnIndex];
                            boardFormat[rowIndex * 5 + charRowIndex + 5][columnIndex * 10 + charColumnIndex * 2 + 1 + 10] = ' ';
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

    /**
     * @return Array of size 2 denoting the dimensions of the board: [height, width].
     */
    public static int[] getBoardDimensions(List<List<Tile>> board) {
        int maxWidth = board.get(0).size();

        for (List<Tile> row : board) {
            assert(row.size() == maxWidth);
        }

        return new int[] {board.size(), maxWidth};
    }

    /**
     * @param board The board for which the tile should be found.
     * @param coordinates The coordinates of the tile.
     * @return The tile object at the given coordinates on the given board. Returns null if there's no tile at the
     * given coordinates.
     */
    public static Tile getTile(List<List<Tile>> board, int[] coordinates) {
        if (coordinates[0] >= getRowSize(coordinates[0], board) || coordinates[1] >= getColumnSize(coordinates[1], board)
                || coordinates[0] < 0 || coordinates[1] < 0) {
            return null;
        }

        return board.get(coordinates[0]).get(coordinates[1]);
    }

    /**
     * @param columnIndex The index of the column in question.
     * @param board The board in question.
     * @return The size of the column.
     */
    public static int getColumnSize(int columnIndex, List<List<Tile>> board) {
        int rowIndex = 0;

        for (List<Tile> row : board) {
            if (row.size() > columnIndex) {
                rowIndex += 1;
            }
        }

        return rowIndex;
    }

    /**
     * @param rowIndex The index of the row in question.
     * @param board The board in question.
     * @return The size of the row.
     */
    public static int getRowSize(int rowIndex, List<List<Tile>> board) {
        if (board.size() < rowIndex - 1) {
            return 0;
        }

        System.out.println(rowIndex);

        return board.get(rowIndex).size();
    }
}
