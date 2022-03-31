import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Engine {

    Player player1;
    Player player2;

    public Engine(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public int[] play() throws Exception {
        GameStateSpace stateSpace = new GameStateSpace();

        GameState state = stateSpace.init();

        List<Tile> deck = assembleDeck();

        if (player1 instanceof UCTPlayer) {
            System.out.println("Player 1: c = " + ((UCTPlayer) player1).getExplorationTerm() + ", " + ((UCTPlayer) player1).getTrainingIterations() + " training iterations.");
        }

        if (player2 instanceof UCTPlayer) {
            System.out.println("Player 2: c = " + ((UCTPlayer) player2).getExplorationTerm() + ", " + ((UCTPlayer) player2).getTrainingIterations() + " training iterations.");
        }

        while (!stateSpace.isGoal(state)) {
            System.out.println("Current score: " + Arrays.toString(state.getScore()));

            state.displayBoard();

            System.out.println(deck.size() + " tiles remaining.");

            Tile drawnTile = drawTile(deck);
            drawnTile.printTile();

            // Players should be given a list of possible moves and should pick one of those moves.

            int player = deck.size() % 2 == 0 ? 1 : 2;
            List<Move> moves = new ArrayList<>();

            while (moves.isEmpty()) {
                moves = stateSpace.placementSucc(state, drawnTile);

                for (Move move : moves) {
                    Tile tileCopy = new Tile(drawnTile);
                    tileCopy.rotateBy(move.getRotation());
                }

                // In the rare case that the drawn tile cannot legally be placed, the tile is added back to the deck
                // and a new tile is drawn.
                if (moves.isEmpty()) {
                    System.out.printf("Player %d draws tile with no possible legal moves. The tile is therefore redrawn.\n\n", ((deck.size() % 2) + 1));
                    deck.add(drawnTile);
                    Collections.shuffle(deck);
                    drawnTile = drawTile(deck);
                    drawnTile.printTile();
                }
            }

            Pair<Integer, Integer> choice;

            if (player == 1) {
                choice = player1.decideOnNextMove(state, drawnTile, deck, moves);
            } else {
                choice = player2.decideOnNextMove(state, drawnTile, deck, moves);
            }

            Move move = moves.get(choice.getFirst());
            int meeplePlacement = choice.getSecond();

            drawnTile.rotateBy(move.getRotation());

            List<Integer> meepleSuccessors = stateSpace.meepleSucc(state, drawnTile, move.getCoords(), player);

            System.out.println(state.getNeighboursByType(move.getCoords(), false).size() + " neighbours.");

            System.out.println(meepleSuccessors.toString());

            if (meepleSuccessors.contains(meeplePlacement)) {
                drawnTile.placeMeeple(choice.getSecond(), player);
            } else {
                System.out.printf("Meeple placement at point %d not allowed.\n\n", meeplePlacement);
            }

            state.updateBoard(move.getCoords(), drawnTile);

            state.checkForScoreAfterRound();
        }

        state.displayBoard();

        state.assignPointsAtEndOfGame();

        System.out.println("Player 1 has " + state.getScore()[0] + " points.");
        System.out.println("Player 2 has " + state.getScore()[1] + " points.");

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
            deck.add(new Tile(type, pennant));
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
        List<Tile> newDeck = new ArrayList<>(deck.size());

        for (Tile tile : deck) {
            newDeck.add(new Tile(tile));
        }

        return newDeck;
    }
}
