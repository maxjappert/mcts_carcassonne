import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArgParser {
    private long deckRandomSeed;

    public Player[] assignPlayers(String[] args) {
        String p1Type                   = "human";
        String p2Type                   = "uct";
        long p1RandomSeed               = -1;
        long p2RandomSeed               = -1;
        this.deckRandomSeed             = -1;
        float p1ExplorationTerm         = 2f;
        float p2ExplorationTerm         = 2f;
        float p1MeeplePlacementProb     = 0.5f;
        float p2MeeplePlacementProb     = 0.5f;
        int p1TrainingIterations        = 150;
        int p2TrainingIterations        = 150;
        float p1ExplorationTermDelta    = 0;
        float p2ExplorationTermDelta    = 0;
        boolean p1heuristicPlayout      = true;
        boolean p2heuristicPlayout      = true;
        float p1backpropDelta           = 0;
        float p2backpropDelta           = 0;
        Engine.verbose                  = true;
        int p1MinimaxDepth              = 2;
        int p2MinimaxDepth              = 2;
        boolean graphwiz                = false;

        if (args[0].equals("-h") || args[0].equals("--help")) {
            printHelp();
            System.exit(0);
        }

        // This is the case if the arguments are passed by Python using a list. In that case we need to convert the
        // Python syntax to a format which java understands.
        if (args[0].charAt(0) == '[') {
            String argString = Arrays.toString(args);
            argString = argString.replace("[", "");
            argString = argString.replace("]", "");
            argString = argString.replace("'", "");
            String[] tempArgs = argString.split(",");
            args = new String[tempArgs.length];
            args = tempArgs;

            for (int i = 0; i < args.length; i++) {
                args[i] = args[i].replace(" ", "");
            }
        }

        if (args.length % 2 != 0) {
            System.out.println("Have you missed specifying an argument?");
            System.out.println("Specified arguments: \n" + new ArrayList<>(List.of(args)));
            System.exit(1);
        }

        try {
            int i = 0;
            while (true) {
                String val = args[i + 1].toLowerCase();
                switch (args[i]) {
                    case "--p1", "-p1"              -> p1Type = val;
                    case "--p2", "-p2"              -> p2Type = val;
                    case "--p1seed"                 -> p1RandomSeed = Long.parseLong(val);
                    case "--p2seed"                 -> p2RandomSeed = Long.parseLong(val);
                    case "--deckseed"               -> this.deckRandomSeed = Long.parseLong(val);
                    case "--p1explorationterm"      -> p1ExplorationTerm = Float.parseFloat(val);
                    case "--p2explorationterm"      -> p2ExplorationTerm = Float.parseFloat(val);
                    case "--p1meepleplacementprob"  -> p1MeeplePlacementProb = Float.parseFloat(val);
                    case "--p2meepleplacementprob"  -> p2MeeplePlacementProb = Float.parseFloat(val);
                    case "--p1trainingiterations"   -> p1TrainingIterations = Integer.parseInt(val);
                    case "--p2trainingiterations"   -> p2TrainingIterations = Integer.parseInt(val);
                    case "--p1explorationtermdelta" -> p1ExplorationTermDelta = Float.parseFloat(val);
                    case "--p2explorationtermdelta" -> p2ExplorationTermDelta = Float.parseFloat(val);
                    case "--p1playout"              -> p1heuristicPlayout = val.equalsIgnoreCase("heuristic");
                    case "--p2playout"              -> p2heuristicPlayout = val.equalsIgnoreCase("heuristic");
                    case "--p1backpropdelta"        -> p1backpropDelta = Float.parseFloat(val);
                    case "--p2backpropdelta"        -> p2backpropDelta = Float.parseFloat(val);
                    case "-v", "--verbose"          -> Engine.verbose = Boolean.parseBoolean(val);
                    case "--p1minimaxdepth"         -> p1MinimaxDepth = Integer.parseInt(val);
                    case "--p2minimaxdepth"         -> p2MinimaxDepth = Integer.parseInt(val);
                    case "-g", "--graphwiz"         -> graphwiz = Boolean.parseBoolean(val);
                    default -> {
                        System.out.println("Unrecognised argument: " + args[i] + "\n\nPlease try again.");
                        System.exit(1);
                    }
                }

                if (i+2 == args.length) {
                    break;
                }

                i += 2;
            }

        } catch (NumberFormatException nfe) {
            Engine.printError("Error: Faulty arguments.");
            System.exit(1);
        }

        GameStateSpace stateSpace = new GameStateSpace();

        Player[] players = new Player[2];

        switch (p1Type) {
            case "uct"              -> players[0] = new MCTSPlayer(stateSpace, 1, p1ExplorationTerm, p1TrainingIterations, p1RandomSeed, p1MeeplePlacementProb, p1ExplorationTermDelta, "uct", p1heuristicPlayout, p1backpropDelta, graphwiz);
            case "epsilon-greedy"   -> players[0] = new MCTSPlayer(stateSpace, 1, p1ExplorationTerm, p1TrainingIterations, p1RandomSeed, p1MeeplePlacementProb, p1ExplorationTermDelta, "epsilon-greedy", p1heuristicPlayout, p1backpropDelta, graphwiz);
            case "heuristic-mcts"   -> players[0] = new MCTSPlayer(stateSpace, 1, p1ExplorationTerm, p1TrainingIterations, p1RandomSeed, p1MeeplePlacementProb, p1ExplorationTermDelta, "heuristic-mcts", p1heuristicPlayout, p1backpropDelta, graphwiz);
            case "uct-tuned"        -> players[0] = new MCTSPlayer(stateSpace, 1, p1ExplorationTerm, p1TrainingIterations, p1RandomSeed, p1MeeplePlacementProb, p1ExplorationTermDelta, "uct-tuned", p1heuristicPlayout, p1backpropDelta, graphwiz);
            case "boltzmann"        -> players[0] = new MCTSPlayer(stateSpace, 1, p1ExplorationTerm, p1TrainingIterations, p1RandomSeed, p1MeeplePlacementProb, p1ExplorationTermDelta, "boltzmann", p1heuristicPlayout, p1backpropDelta, graphwiz);
            case "human"            -> players[0] = new HumanPlayer(stateSpace, 1);
            case "random"           -> players[0] = new RandomPlayer(stateSpace, 1, p1RandomSeed);
            case "heuristic"        -> players[0] = new HeuristicPlayer(stateSpace, 1, p1RandomSeed);
            case "minimax"          -> players[0] = new MinimaxPlayer(stateSpace, 1, p1RandomSeed, p1MinimaxDepth, p1MeeplePlacementProb);
            default -> {
                System.out.println("Invalid player type for player 1. The options are 'uct'/'[heuristic-]epsilon-greedy'/'human'/'random'/'heuristic'/'heuristic-mcts'.");
                System.exit(1);
            }
        }

        switch (p2Type) {
            case "uct"              -> players[1] = new MCTSPlayer(stateSpace, 2, p2ExplorationTerm, p2TrainingIterations, p2RandomSeed, p2MeeplePlacementProb, p2ExplorationTermDelta, "uct", p2heuristicPlayout, p2backpropDelta, graphwiz);
            case "epsilon-greedy"   -> players[1] = new MCTSPlayer(stateSpace, 2, p2ExplorationTerm, p2TrainingIterations, p2RandomSeed, p2MeeplePlacementProb, p2ExplorationTermDelta, "epsilon-greedy", p2heuristicPlayout, p2backpropDelta, graphwiz);
            case "heuristic-mcts"   -> players[1] = new MCTSPlayer(stateSpace, 2, p2ExplorationTerm, p2TrainingIterations, p2RandomSeed, p2MeeplePlacementProb, p2ExplorationTermDelta, "heuristic-mcts", p2heuristicPlayout, p2backpropDelta, graphwiz);
            case "uct-tuned"        -> players[1] = new MCTSPlayer(stateSpace, 2, p2ExplorationTerm, p2TrainingIterations, p2RandomSeed, p2MeeplePlacementProb, p2ExplorationTermDelta, "uct-tuned", p2heuristicPlayout, p2backpropDelta, graphwiz);
            case "boltzmann"        -> players[1] = new MCTSPlayer(stateSpace, 2, p2ExplorationTerm, p2TrainingIterations, p2RandomSeed, p2MeeplePlacementProb, p2ExplorationTermDelta, "boltzmann", p2heuristicPlayout, p2backpropDelta, graphwiz);
            case "human"            -> players[1] = new HumanPlayer(stateSpace, 2);
            case "random"           -> players[1] = new RandomPlayer(stateSpace, 2, p2RandomSeed);
            case "heuristic"        -> players[1] = new HeuristicPlayer(stateSpace, 2, p2RandomSeed);
            case "minimax"          -> players[1] = new MinimaxPlayer(stateSpace, 2, p2RandomSeed, p2MinimaxDepth, p2MeeplePlacementProb);
            default -> {
                System.out.println("Invalid player type for player 2. The options are 'uct'/'[heuristic-]epsilon-greedy'/'human'/'random'/'heuristic'/'heuristic-mcts'.");
                System.exit(1);
            }
        }

        return players;
    }

    public void printHelp() {
        String help = """
                Welcome! Please use the following arguments to specify how this module should be used:
                
                  --p[1/2] <tree policy type>            Specify the type of tree policy the MCTS player should
                                                         use. The possible types are:
                                                         ['uct'/'human'/'random'/'[heuristic-]epsilon-greedy[-decaying]'/
                                                         'heuristic'/'heuristic-mcts'/'boltzmann'/'minimax']
                  --p[1/2]seed <Integer>                 Make the random actions for a given player reproducible
                                                         by specifying a seed.
                  --deckseed <Integer>                   Make the shuffling of the deck reproducible.
                  --p[1/2]explorationterm <Float>        The exploration term for the UCT player. Acts as the
                                                         \u03B5 for \u03B5-greedy players.
                  --p[1/2]meepleplacementprob <Float>    Probability of considering a meeple placement for random
                                                         playouts.
                  --p[1/2]trainingiterations <Integer>   The number of training iterations for a given MCTS player.
                  --p[1/2]explorationtermdelta <Float>   This term is added to the exploration term after every
                                                         move a MCTS  player plays.
                  --p[1/2]playout <Type>                 Decides the type of playout the MCTS player will perform.
                                                         Possible types are: ['random'/'heuristic']
                  --p[1/2]backpropdelta <Float>          This term is added to the backpropagation-weight
                                                         after every move an MCTS-player makes.
                  --p[1/2]minimaxdepth <Integer>         How deep does the Minimax-Player actually perform a
                                                         Minimax-Search before switching to the default policy.
                  -g or --graphwiz <Boolean>             Specify if a .dot file should be created which can be
                                                         used to visualise the MCTS-tree.
                  -v or --verbose <Boolean>              Controls if detailed information on the game progress
                                                         should be printed to console. True by default. If set
                                                         to false, then the only things printed to console are
                                                         a dump of the configurations, the final board and the
                                                         final score.
                  -h or --help                           Print this message.
                """;

        System.out.println(help);
    }

    public long getDeckRandomSeed() {
        return deckRandomSeed;
    }
}

