import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Engine {

    public short[] play() throws Exception {
        //logger.info("play() started.");

        GameStateSpace stateSpace = new GameStateSpace();
        GameState state = stateSpace.init();
        //remainingDeck

        Player player1 = new UCTPlayer((byte) 1, 0.5f, (short) 500);
        Player player2 = new RandomPlayer(2);

        List<Tile> deck = assembleDeck();

        while (!stateSpace.isGoal(state)) {
            state.displayBoard();

            Tile drawnTile = drawTile(deck);
            drawnTile.printTile();

            byte[] move = new byte[]{-1, -1};

            while (move[0] == -1) {
                if (deck.size() % 2 == 0) {
                    move = player1.decideOnNextMove(state, stateSpace, drawnTile, deck);
                } else {
                    move = player2.decideOnNextMove(state, stateSpace, drawnTile, deck);
                }

                // In the rare case that the drawn tile cannot legally be placed, the tile is added back to the deck
                // and a new tile is drawn.
                if (move[0] == -1) {
                    //logger.info("Player {} draws tile with no possible legal moves. The tile is therefore redrawn.", ((state.deckSize() % 2) + 1));
                    deck.add(drawnTile);
                    Collections.shuffle(deck);
                    drawnTile = drawTile(deck);
                    drawnTile.printTile();
                }
            }

            state.updateBoard(move, drawnTile);

            state.checkForScoreAfterRound();
        }

        state.displayBoard();

        state.assignPointsAtEndOfGame();

        System.out.println("Player 1 has " + state.getScore()[0] + " points.");
        System.out.println("Player 2 has " + state.getScore()[1] + " points.");

        //logger.info("play() finished.");

        return state.getScore();
    }

    /**
     * Simply adds a given number of a given type's tiles to the deck.
     * @param type The type of tile to be added.
     * @param amount The number of tiles of the given type to be added.
     * @param pennant True if the tile includes a pennant.
     */
    private void addTilesToDeck(List<Tile> deck, int type, int amount, boolean pennant) {
        for (int i = 0; i < amount; i++) {
            deck.add(new Tile((byte) type, pennant));
        }
    }

    /**
     * Assembles the deck according to the game's instructions and then shuffles it.
     */
    private List<Tile> assembleDeck() {
        List<Tile> deck = new ArrayList<>();

        addTilesToDeck(deck,0, 3, false);
        addTilesToDeck(deck,1, 3, false);
        addTilesToDeck(deck,2, 3, false);
        addTilesToDeck(deck,3, 3, false);
        addTilesToDeck(deck,4, 5, false);
        addTilesToDeck(deck,5, 3, false);
        addTilesToDeck(deck,6, 2, false);
        addTilesToDeck(deck,7, 8, false);
        addTilesToDeck(deck,8, 9, false);
        addTilesToDeck(deck,9, 4, false);
        addTilesToDeck(deck,10, 1, false);
        addTilesToDeck(deck,11, 2, false);
        addTilesToDeck(deck,12, 4, false);
        addTilesToDeck(deck,13, 1, false);
        addTilesToDeck(deck,13, 2, true);
        addTilesToDeck(deck,14, 3, false);
        addTilesToDeck(deck,14, 2, true);
        addTilesToDeck(deck,15, 3, false);
        addTilesToDeck(deck,15, 2, true);
        addTilesToDeck(deck,16, 1, false);
        addTilesToDeck(deck,16, 2, true);
        addTilesToDeck(deck,17, 3, false);
        addTilesToDeck(deck,17, 1, true);
        addTilesToDeck(deck,18, 1, true);

        // shuffle the deck
        Collections.shuffle(deck);

        return deck;
    }

    public static Tile drawTile(List<Tile> deck) {
        return deck.remove(0);
    }

    public static List<Tile> copyDeck(List<Tile> deck) {
        List<Tile> newDeck = new ArrayList<>();

        for (Tile tile : deck) {
            newDeck.add(new Tile(tile));
        }

        return newDeck;
    }
}