public class Main {

    static String instructions = """
            The following arguments must be passed to the program:
            0:  Either "human", "random" or "uct" for player 1.
            1:  Player 1 random seed (either for random player or for the random playout for the UCT player).
            2:  Either "human", "random" or "uct" for player 2.
            3:  Player 2 random seed.
            4:  Deck random seed for assembling and shuffling the deck.
            5:  Player 1 exploration term (if applicable).
            6:  Player 1 meeple placement probability (if applicable).
            7:  Player 1 training iterations (if applicable).
            8:  Player 1 exploration term delta (if applicable).
            9:  Player 2 exploration term (if applicable).
            10: Player 2 meeple placement probability (if applicable).
            11: Player 2 training iterations (if applicable)
            12: Player 2 exploration term delta (if applicable).
            """;

    public static void main(String[] args) throws Exception {
        GameStateSpace stateSpace = new GameStateSpace();
        Player player1;
        Player player2;
        long deckRandomSeed;

        if (args.length == 0) {
            player2 = new UCTPlayer(stateSpace, 2, 4f, 50, 3, 0.5f, 0);
            //player1 = new MinimaxPlayer(stateSpace, 1);
            player1 = new RandomPlayer(stateSpace, 1, 7);
            //player1 = new UCTPlayer(stateSpace, 1, 4f, 100, 3, 0.5f, 0);
            deckRandomSeed = 8;
        } else {
            if (args[0].equalsIgnoreCase("-h") || args[0].equalsIgnoreCase("-help")) {
                System.out.println(instructions);
                return;
            }
            try {
                player1 = assignPlayer(args[0].toLowerCase(), 1, stateSpace, Long.parseLong(args[1]), Float.parseFloat(args[5]), Float.parseFloat(args[6]), Integer.parseInt(args[7]), Float.parseFloat(args[8]));
                player2 = assignPlayer(args[2].toLowerCase(), 2, stateSpace, Long.parseLong(args[3]), Float.parseFloat(args[9]), Float.parseFloat(args[10]), Integer.parseInt(args[11]), Float.parseFloat(args[12]));
                deckRandomSeed = Long.parseLong(args[4]);
            } catch (NumberFormatException nfe) {
                System.out.println("Invalid numbers.");
                return;
            }
        }

        Engine engine = new Engine(player1, player2, deckRandomSeed);

        engine.play();
    }

    private static Player assignPlayer(String type, int playerID, GameStateSpace stateSpace, long randomSeed, float explorationTerm,
                                       float randomPlayoutMeeplePlacementProbability, int trainingIterations, float explorationTermDelta) {
        return switch (type) {
            case "human" -> new HumanPlayer(stateSpace, playerID);
            case "uct" -> new UCTPlayer(stateSpace, playerID, explorationTerm, trainingIterations, randomSeed, randomPlayoutMeeplePlacementProbability, explorationTermDelta);
            case "random" -> new RandomPlayer(stateSpace, playerID, randomSeed);
            default -> null;
        };

    }

//    public static void createReport() throws Exception {
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH:mm:ss");
//        LocalDateTime now = LocalDateTime.now();
//
//        FileWriter fileWriter = new FileWriter("report_" + dtf.format(now));
//        PrintWriter printWriter = new PrintWriter(fileWriter);
//
//        int[] ti = new int[]{50, 100, 500, 1000, 5000};
//
//        printWriter.println("Playing against a random opponent:");
//        printWriter.flush();
//
//        GameStateSpace stateSpace = new GameStateSpace();
//
//        for (float c = 0f; c < 15; c += 0.5f) {
//            for (int trainingIterations : ti) {
//                Engine engine = new Engine(new UCTPlayer(stateSpace, 1, c, trainingIterations), new RandomPlayer(stateSpace, 2));
//                int[] score = new int[]{0, 0};
//                for (int i = 0; i < 10; i++) {
//                    System.out.println("Round " + (i+1));
//                    int[] roundScore = engine.play();
//                    score[0] += roundScore[0];
//                    score[1] += roundScore[1];
//                }
//
//                score[0] = score[0] / 10;
//                score[1] = score[1] / 10;
//
//                String line = trainingIterations + " training iterations, c = " + c + ": " + Arrays.toString(score) + "\n";
//                printWriter.println(line);
//                printWriter.flush();
//            }
//        }
//
//        printWriter.close();
//        }

}


