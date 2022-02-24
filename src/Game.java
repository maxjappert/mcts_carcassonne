import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Game {
    List<Tile> deck;
    List<List<Tile>> board;
    Player player1;
    Player player2;

    public Game() throws Exception {
        deck = new ArrayList<>();
        board = new ArrayList<>();
        assembleDeck();
        assert(deck.size() == 72);

        // shuffle the deck
        Collections.shuffle(deck);

        board.add(new ArrayList<>());
        board.get(0).add(deck.remove(0));
        board.get(0).add(deck.remove(0));
        board.get(0).add(deck.remove(0));
        board.get(0).add(deck.remove(0));

        displayBoard();
    }

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
    }

    private void addTilesToDeck(int type, int amount, boolean pennant) {
        for (int i = 0; i < amount; i++) {
            this.deck.add(new Tile(type, pennant));
        }
    }

    public void displayBoard() throws Exception {
        String emptyTile =
                "          \n" +
                "          \n" +
                "          \n" +
                "          \n" +
                "          \n";

        for (List<Tile> row : board) {
            for (Tile t : row) {
                if (t == null) {
                    System.out.println(emptyTile);
                } else {
                    t.printTile();
                }
            }
            System.out.println();
        }
    }
}
