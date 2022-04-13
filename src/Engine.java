import java.util.*;

public class Engine {

    Player player1;
    Player player2;
    long randomSeed;

    public Engine(Player player1, Player player2, long randomSeed) {
        this.player1 = player1;
        this.player2 = player2;
        this.randomSeed = randomSeed;
    }

    public void play() throws Exception {
        GameStateSpace stateSpace = new GameStateSpace();
        GameState state = stateSpace.init();
        Random random;

        random = randomSeed == -1 ? new Random() : new Random(randomSeed);

        List<Tile> deck = assembleDeck(random);

        StringBuilder info = new StringBuilder(String.format("""
                The game is played with the following parameters:
                
                Deck Seed : %d
                Player 1:   %s
                """, randomSeed, player1.getTypeAsString()));

        if (player1 instanceof UCTPlayer) {
            info.append("Player 1:   c = " + ((UCTPlayer) player1).getExplorationTerm() + ", " + ((UCTPlayer) player1)
                    .getTrainingIterations() + " training iterations, with " + ((UCTPlayer) player1).getPlayoutSeed()
                    + " as a playout seed and a meeple placement probability of " + ((UCTPlayer) player1)
                    .getPlayoutMeeplePlacementProbability() * 100 + "%.\n");
        }

        if (player1 instanceof RandomPlayer) {
            info.append("Player 1 random seed: " + ((RandomPlayer) player1).getSeed() + "\n");
        }

        info.append("Player 2:   " + player2.getTypeAsString() + "\n");

        if (player2 instanceof UCTPlayer) {
            info.append("Player 2:   c = " + ((UCTPlayer) player2).getExplorationTerm() + ", " + ((UCTPlayer) player2)
                    .getTrainingIterations() + " training iterations, with " + ((UCTPlayer) player2).getPlayoutSeed()
                    + " as a playout seed and a meeple placement probability of " + ((UCTPlayer) player2)
                    .getPlayoutMeeplePlacementProbability() * 100 + "%.\n");
        }

        if (player2 instanceof RandomPlayer) {
            info.append("Player 2 random seed: " + ((RandomPlayer) player2).getSeed());
        }

        System.out.println(info);

        while (!stateSpace.isGoal(state)) {
            System.out.println("Current score: " + Arrays.toString(state.getScore()));

            state.displayBoard();

            System.out.println(deck.size() + " tiles remaining.");
            System.out.println("Player " + state.getPlayer() + "'s turn:");

            Tile drawnTile = drawTile(deck);
            drawnTile.printTile();

            // Players should be given a list of possible moves and should pick one of those moves.

            int player = deck.size() % 2 == 0 ? 1 : 2;
            List<Move> moves = new ArrayList<>();

            while (moves.isEmpty()) {
                moves = stateSpace.placementSucc(state, drawnTile);

                // In the rare case that the drawn tile cannot legally be placed, the tile is added back to the deck
                // and a new tile is drawn.
                if (moves.isEmpty()) {
                    System.out.printf("Player %d draws tile with no possible legal moves. The tile is therefore redrawn.\n\n", ((deck.size() % 2) + 1));
                    deck.add(drawnTile);
                    Collections.shuffle(deck, random);
                    drawnTile = drawTile(deck);
                    drawnTile.printTile();
                }
            }

            Pair choice;

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

            state.checkForScoreAfterRound(true);
        }

        state.displayBoard();

        state.assignPointsAtEndOfGame();

        System.out.println("Player 1 has " + state.getScore()[0] + " points.");
        System.out.println("Player 2 has " + state.getScore()[1] + " points.");

        state.getScore();
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

    private void checkForAreasWithMultipleMeeples(GameState state) {
        List<Integer> areasWithMeeples = new ArrayList<>();

        for (Tile tile : state.getAllTilesOnBoard()) {
            for (int point = 0; point <= 12; point++) {
                if (tile.getMeeple()[0] == point && areasWithMeeples.contains(tile.getArea(point))) {
                    System.out.println("Error in following tile:");
                    tile.printTile();
                    System.out.println("Conflict with the following tiles:");
                    List<Tile> tilesOfArea = state.getTilesOfArea(tile.getArea(point));
                    for (Tile tileOfArea : tilesOfArea) {
                        if (tileOfArea.hasMeeple() && tileOfArea.getArea(tileOfArea.getMeeple()[0]) == tile.getArea(point)) {
                            tileOfArea.printTile();
                        }
                    }
                    System.exit(-1);
                } else if (tile.getMeeple()[0] == point && !areasWithMeeples.contains(tile.getArea(point)))  {
                    areasWithMeeples.add(tile.getArea(point));
                }
            }
        }
    }

    /**
     * Assembles the deck according to the game's instructions and then shuffles it.
     */
    private List<Tile> assembleDeck(Random random) {
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
        Collections.shuffle(deck, random);

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
