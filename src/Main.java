public class Main {

    public static void main(String[] args) throws Exception {
        GameStateSpace stateSpace = new GameStateSpace();
        Player player1;
        Player player2;
        long deckRandomSeed;

        ArgParser argParser = new ArgParser();

        if (args.length == 0) {
            player2 = new HeuristicPlayer(stateSpace, 2);
            //player1 = new MinimaxPlayer(stateSpace, 1);
            //player1 = new RandomPlayer(stateSpace, 1, -1);
            player1 = new UCTPlayer(stateSpace, 1, 2f, 100, 3, 0.5f, 0, "uct");
            deckRandomSeed = 2;
        } else {
            Player[] players = argParser.assignPlayers(args);
            player1 = players[0];
            player2 = players[1];
            deckRandomSeed = argParser.getDeckRandomSeed();
        }

        Engine engine = new Engine(player1, player2, deckRandomSeed);

        engine.play();
    }

    private static Player assignPlayer(String type, int playerID, GameStateSpace stateSpace, long randomSeed, float explorationTerm,
                                       float randomPlayoutMeeplePlacementProbability, int trainingIterations, float explorationTermDelta) {
        return switch (type) {
            case "human" -> new HumanPlayer(stateSpace, playerID);
            case "uct" -> new UCTPlayer(stateSpace, playerID, explorationTerm, trainingIterations, randomSeed, randomPlayoutMeeplePlacementProbability, explorationTermDelta, "uct");
            case "epsilon-greedy-mcts" -> new UCTPlayer(stateSpace, playerID, explorationTerm, trainingIterations, randomSeed, randomPlayoutMeeplePlacementProbability, explorationTermDelta, "epsilon-greedy-mcts");
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


