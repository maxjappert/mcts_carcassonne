import java.util.*;

public class Engine {

    private final Player player1;
    private final Player player2;
    private long randomSeed;
    public static boolean verbose;
    public static int numNodes = 0;

    public Engine(Player player1, Player player2, long randomSeed) {
        this.player1 = player1;
        this.player2 = player2;
        this.randomSeed = randomSeed;
    }

    public void play() throws Exception {
        GameStateSpace stateSpace = new GameStateSpace();
        GameState state = stateSpace.init();
        Random random;

        if (randomSeed == -1) {
            this.randomSeed = new Random().nextInt(Integer.MAX_VALUE);
        }

        random = new Random(randomSeed);

        List<Tile> deck = assembleDeck(random);

        StringBuilder info = new StringBuilder(String.format("The game is played with the following parameters:\n\n" +
                "Deck Seed:            %d\n" +
                "Player 1:             %s\n", randomSeed, player1.getTypeAsString()));

        if (player1 instanceof MCTSPlayer) {
            info.append("P1: c = ").append(((MCTSPlayer) player1).getExplorationConst()).append(", ").append(((MCTSPlayer) player1)
                    .getTrainingIterations()).append(" training iterations, with ").append(((MCTSPlayer) player1).getPlayoutSeed()).append(" as a playout seed and a meeple placement probability of ").append(((MCTSPlayer) player1)
                    .getPlayoutMeeplePlacementProbability() * 100).append("%.\n");
        }

        if (player1 instanceof RandomPlayer) {
            info.append("P1 random seed:       ").append(((RandomPlayer) player1).getSeed()).append("\n");
        } else if (player1 instanceof HeuristicPlayer) {
            info.append("P1 random seed:       ").append(((HeuristicPlayer) player1).getSeed()).append("\n");
        }

        info.append("Player 2:             ").append(player2.getTypeAsString()).append("\n");

        if (player2 instanceof MCTSPlayer) {
            info.append("P2: c = ").append(((MCTSPlayer) player2).getExplorationConst()).append(", ").append(((MCTSPlayer) player2)
                    .getTrainingIterations()).append(" training iterations, with ").append(((MCTSPlayer) player2).getPlayoutSeed()).append(" as a playout seed and a meeple placement probability of ").append(((MCTSPlayer) player2)
                    .getPlayoutMeeplePlacementProbability() * 100).append("%.\n");
        }

        if (player2 instanceof RandomPlayer) {
            info.append("P2 random seed:       ").append(((RandomPlayer) player2).getSeed());
        } else if (player2 instanceof HeuristicPlayer) {
            info.append("P2 random seed:       ").append(((HeuristicPlayer) player2).getSeed());
        }

        System.out.println(info);

        double player1ContemplationTime = 0;
        double player2ContemplationTime = 0;


        while (!stateSpace.isGoal(state)) {
            if (verbose) System.out.println("Current score: " + Arrays.toString(state.getScore()));

            if (verbose) state.displayBoard();

            if (verbose) System.out.println(deck.size() + " tiles remaining.");
            if (verbose) System.out.println("Player " + state.getPlayer() + "'s turn:");

            Tile drawnTile = drawTile(deck);
            if (verbose) drawnTile.printTile();

            // Players should be given a list of possible moves and should pick one of those moves.

            int player = deck.size() % 2 == 0 ? 1 : 2;
            List<Move> moves = new ArrayList<>();

            while (moves.isEmpty()) {
                moves = stateSpace.placementSucc(state, drawnTile);

                // In the rare case that the drawn tile cannot legally be placed, the tile is added back to the deck
                // and a new tile is drawn.
                if (moves.isEmpty()) {
                    if (verbose) System.out.printf("Player %d draws tile with no possible legal moves. The tile is therefore redrawn.\n\n", ((deck.size() % 2) + 1));
                    deck.add(drawnTile);
                    Collections.shuffle(deck, random);
                    drawnTile = drawTile(deck);
                    drawnTile.printTile();
                }
            }

            Pair choice;

            List<Tile> shuffledDeck = copyDeck(deck);
            Collections.shuffle(shuffledDeck);

            if (player == 1) {
                if ((player1 instanceof MCTSPlayer || player1 instanceof MinimaxPlayer) && verbose)
                    System.out.println("Player 1 is calculating a move...");
                long time1 = System.nanoTime();
                choice = player1.decideOnNextMove(state, drawnTile, shuffledDeck, moves);
                long time2 = System.nanoTime();
                player1ContemplationTime += (time2 - time1) / Math.pow(10, 9);
            } else {
                if ((player2 instanceof MCTSPlayer || player2 instanceof MinimaxPlayer) && verbose)
                    System.out.println("Player 2 is calculating a move...");
                long time1 = System.nanoTime();
                choice = player2.decideOnNextMove(state, drawnTile, shuffledDeck, moves);
                long time2 = System.nanoTime();
                player2ContemplationTime += (time2 - time1) / Math.pow(10, 9);
            }

            Move move = moves.get(choice.getFirst());
            int meeplePlacement = choice.getSecond();

            drawnTile.rotateBy(move.getRotation());

            List<Integer> meepleSuccessors = stateSpace.meepleSucc(state, drawnTile, move.getCoords(), player);

            if (meepleSuccessors.contains(meeplePlacement)) {
                //drawnTile.placeMeeple(choice.getSecond(), player);
                if (choice.getSecond() != -1) {
                    if (verbose) System.out.println("Player " + player + " has " + state.getNumMeeples(player) + " meeples remaining.");
                    if (verbose) System.out.println("Player " + player + " places meeple.");
                    state.placeMeeple(choice.getSecond(), player, drawnTile);
                }
            } else {
                if (verbose) System.out.printf("Meeple placement at point %d not allowed.\n\n", meeplePlacement);
            }

            state.updateBoard(move.getCoords(), drawnTile);

            System.out.println("Player " + player + " has " + state.getNumMeeples(player) + " meeples remaining.");

            state.checkForScoreAfterRound(true);
        }

        state.displayBoard();

        state.assignPointsAtEndOfGame();

        System.out.println("P1 points: " + state.getScore()[0]);
        System.out.println("P2 points: " + state.getScore()[1]);

        System.out.println("P1 contemplation time in seconds: " + player1ContemplationTime);
        System.out.println("P2 contemplation time in seconds: " + player2ContemplationTime);

        System.out.println("Point difference: " + (state.getScore()[0]-state.getScore()[1]));
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
    private List<Tile> assembleDeck(Random random) {
        List<Tile> deck = new ArrayList<>();

        addTilesToDeck(deck,0, 4, false);
        addTilesToDeck(deck,1, 4, false);
        addTilesToDeck(deck,2, 5, false);
        addTilesToDeck(deck,3, 5, false);
        addTilesToDeck(deck,4, 5, false);
        addTilesToDeck(deck,7, 8, false);
        addTilesToDeck(deck,8, 9, false);
        addTilesToDeck(deck,9, 4, false);
        addTilesToDeck(deck,10, 1, false);
        addTilesToDeck(deck,11, 2, false);
        addTilesToDeck(deck,12, 4, false);
        addTilesToDeck(deck,13, 1, false);
        addTilesToDeck(deck,13, 2, true);
        addTilesToDeck(deck,14, 6, false);
        addTilesToDeck(deck,14, 3, true);
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

    public static void printError(String message) {
        if (verbose) System.out.println("\u001B[31m **" + message + "\u001B[0m");
    }
}
