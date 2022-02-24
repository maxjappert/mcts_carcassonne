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
    public Game() {
        deck = new ArrayList<>();
        board = new ArrayList<>();
        assembleDeck();
        assert(deck.size() == 72);


        // just for testing from here on
//        board.add(new ArrayList<>());
//        board.get(0).add(new Tile(0, false));
//        board.get(0).add(new Tile(1, false));
//
//        System.out.println(Arrays.deepToString(board.get(0).get(0).getPrintFormatOfTile()));
//        System.out.println(Arrays.deepToString(board.get(0).get(1).getPrintFormatOfTile()));
//
//        displayBoard();
    }

    public void play() throws Exception {
        while (true) {
            displayBoard();

        }
    }

    /**
     * Assembles and shuffles the deck according to the game's instructions.
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
     * Simply adds a given amount of a given type's tiles to the deck.
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
        int[] boardDimensions = getBoardDimensions();

        char[][] boardFormat = new char[boardDimensions[0] * 5][boardDimensions[1] * 10];

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
                            boardFormat[rowIndex * 5 + charRowIndex][columnIndex * 10 + charColumnIndex] = ' ';
                        }
                    } else {
                        char[][] tileFormat = t.getPrintFormatOfTile();
                        // loop over the characters in each row of characters
                        for (int charColumnIndex = 0; charColumnIndex < 5; charColumnIndex++) {
                            boardFormat[rowIndex * 5 + charRowIndex][columnIndex * 10 + charColumnIndex * 2] = tileFormat[charRowIndex][charColumnIndex];
                            boardFormat[rowIndex * 5 + charRowIndex][columnIndex * 10 + charColumnIndex * 2 + 1] = ' ';
                        }
                    }
                }
            }
        }

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
    private int[] getBoardDimensions() {
        int maxWidth = 0;

        for (List<Tile> row : board) {
            if (row.size() > maxWidth) {
                maxWidth = row.size();
            }
        }

        return new int[] {this.board.size(), maxWidth};
    }
}
