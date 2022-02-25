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

            player1.decideOnNextMove(board);
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
        int maxWidth = 0;

        for (List<Tile> row : board) {
            if (row.size() > maxWidth) {
                maxWidth = row.size();
            }
        }

        return new int[] {board.size(), maxWidth};
    }

    public static int getColumnSize(int columnIndex, List<List<Tile>> board) {
        int rowIndex = 0;

        for (List<Tile> row : board) {
            if (row.size() > columnIndex) {
                rowIndex += 1;
            }
        }

        return rowIndex;
    }

    public static int getRowSize(int rowIndex, List<List<Tile>> board) {
        return board.get(rowIndex).size();
    }
}
