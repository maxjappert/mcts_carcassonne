public class ArgParser {
    private long deckRandomSeed;

    public Player[] assignPlayers(String[] args) {
        String p1Type = "human";
        String p2Type = "uct";
        long p1RandomSeed = -1;
        long p2RandomSeed = -1;
        long deckRandomSeed = -1;
        float p1ExplorationTerm = 2f;
        float p2ExplorationTerm = 2f;
        float p1MeeplePlacementProb = 0.5f;
        float p2MeeplePlacementProb = 0.5f;
        int p1TrainingIterations = 150;
        int p2TrainingIterations = 150;
        float p1ExplorationTermDelta = 0;
        float p2ExplorationTermDelta = 0;
        boolean p1heuristicPlayout = true;
        boolean p2heuristicPlayout = true;

        if (args[0].equals("-h") || args[0].equals("-help")) {
            printHelp();
            System.exit(0);
        }

        if (args.length % 2 != 0) {
            System.out.println("Have you missed specifying an argument?");
            System.exit(1);
        }

        try {
            int i = 0;
            while (true) {
                String val = args[i + 1].toLowerCase();
                switch (args[i]) {
                    case "--p1" -> {
                        p1Type = val;
                    }
                    case "--p2" -> {
                        p2Type = val;
                    }
                    case "--p1seed" -> {
                        p1RandomSeed = Long.parseLong(val);
                    }
                    case "--p2seed" -> {
                        p2RandomSeed = Long.parseLong(val);
                    }
                    case "--deckseed" -> {
                        deckRandomSeed = Long.parseLong(val);
                    }
                    case "--p1explorationterm" -> {
                        p1ExplorationTerm = Float.parseFloat(val);
                    }
                    case "--p2explorationterm" -> {
                        p2ExplorationTerm = Float.parseFloat(val);
                    }
                    case "--p1meepleplacementprob" -> {
                        p1MeeplePlacementProb = Float.parseFloat(val);
                    }
                    case "--p2meepleplacementprob" -> {
                        p2MeeplePlacementProb = Float.parseFloat(val);
                    }
                    case "--p1trainingiterations" -> {
                        p1TrainingIterations = Integer.parseInt(val);
                    }
                    case "--p2trainingiterations" -> {
                        p2TrainingIterations = Integer.parseInt(val);
                    }
                    case "--p1explorationtermdelta" -> {
                        p1ExplorationTermDelta = Float.parseFloat(val);
                    }
                    case "--p2explorationtermdelta" -> {
                        p2ExplorationTermDelta = Float.parseFloat(val);
                    }
                    case "--p1heuristicplayout" -> {
                        p1heuristicPlayout = Boolean.parseBoolean(val);
                    }
                    case "--p2heuristicplayout" -> {
                        p2heuristicPlayout = Boolean.parseBoolean(val);
                    }
                }

                if (i+2 == args.length) {
                    break;
                }

                i += 2;
            }

        } catch (NumberFormatException nfe) {
            System.out.println("Error: Faulty arguments.");
            System.exit(1);
        }

        GameStateSpace stateSpace = new GameStateSpace();

        Player[] players = new Player[2];

        switch (p1Type) {
            case "uct" -> players[0] = new MCTSPlayer(stateSpace, 1, p1ExplorationTerm, p1TrainingIterations, p1RandomSeed, p1MeeplePlacementProb, p1ExplorationTermDelta, "uct", p1heuristicPlayout);
            case "epsilon-greedy" -> players[0] = new MCTSPlayer(stateSpace, 1, p1ExplorationTerm, p1TrainingIterations, p1RandomSeed, p1MeeplePlacementProb, p1ExplorationTermDelta, "epsilon-greedy", p1heuristicPlayout);
            case "heuristic-mcts" -> players[0] = new MCTSPlayer(stateSpace, 1, p1ExplorationTerm, p1TrainingIterations, p1RandomSeed, p1MeeplePlacementProb, p1ExplorationTermDelta, "heuristic-mcts", p1heuristicPlayout);
            case "human" -> players[0] = new HumanPlayer(stateSpace, 1);
            case "random" -> players[0] = new RandomPlayer(stateSpace, 1, p1RandomSeed);
            case "heuristic" -> players[0] = new HeuristicPlayer(stateSpace, 1, p1RandomSeed);
            default -> System.out.println("Invalid player type for player 1. The options are 'uct'/'[heuristic-]epsilon-greedy'/'human'/'random'/'heuristic'/'heuristic-mcts'.");
        }

        switch (p2Type) {
            case "uct" -> players[1] = new MCTSPlayer(stateSpace, 2, p2ExplorationTerm, p2TrainingIterations, p2RandomSeed, p2MeeplePlacementProb, p2ExplorationTermDelta, "uct", p2heuristicPlayout);
            case "epsilon-greedy" -> players[1] = new MCTSPlayer(stateSpace, 2, p2ExplorationTerm, p2TrainingIterations, p2RandomSeed, p2MeeplePlacementProb, p2ExplorationTermDelta, "epsilon-greedy", p2heuristicPlayout);
            case "heuristic-mcts" -> players[1] = new MCTSPlayer(stateSpace, 2, p2ExplorationTerm, p2TrainingIterations, p2RandomSeed, p2MeeplePlacementProb, p2ExplorationTermDelta, "heuristic-mcts", p2heuristicPlayout);
            case "human" -> players[1] = new HumanPlayer(stateSpace, 2);
            case "random" -> players[1] = new RandomPlayer(stateSpace, 2, p2RandomSeed);
            case "heuristic" -> players[1] = new HeuristicPlayer(stateSpace, 2, p2RandomSeed);
            default -> System.out.println("Invalid player type for player 2. The options are 'uct'/'[heuristic-]epsilon-greedy'/'human'/'random'/'heuristic'/'heuristic-mcts'.");
        }

        this.deckRandomSeed = deckRandomSeed;

        return players;
    }

    public void printHelp() {
        String help = """
                Welcome! Please use the following arguments to specify how this module should be used:
                
                    --p[1/2] <type>                               Specify the type of player. The possible types are:
                                                                  ['uct'/'human'/'random'/'[heuristic-]epsilon-greedy'/'heuristic'/'heuristic-mcts']
                    --p[1/2]seed <Integer>                        Make the random actions for a given player reproducible by specifying a random seed.
                    --deckseed <Integer>                          Make the shuffling of the deck reproducible.
                    --p[1/2]explorationterm <Float>               The exploration term for the UCT player. Acts as the \u03B5 for \u03B5-greedy players.
                    --p[1/2]meepleplacementprob <Float>           Probability of considering a meeple placement for random playouts.
                    --p[1/2]trainingiterations <Integer>          The number of training iterations for a given MCTS player.
                    --p[1/2]explorationtermdelta <Float>          This term is added to the exploration term after every move a UCT player plays.
                    --p[1/2]heuristicplayout <Boolean>            Decides if the UCT player uses a heuristic in the playout step.
                    --help or -h                                  Print this message.
                """;

        System.out.println(help);
    }

    public long getDeckRandomSeed() {
        return deckRandomSeed;
    }
}

