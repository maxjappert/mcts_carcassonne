import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArgParser {
    private long deckRandomSeed;

    public Player[] assignPlayers(String[] args) {
        String p1Type                   = "human";
        String p2Type                   = "decaying-uct-tuned";
        long p1RandomSeed               = -1;
        long p2RandomSeed               = -1;
        this.deckRandomSeed             = -1;
        float p1ExplorationTerm         = 4f;
        float p2ExplorationTerm         = 512f;
        float p1MeeplePlacementProb     = 0.3f;
        float p2MeeplePlacementProb     = 0.3f;
        int p1TrainingIterations        = 150;
        int p2TrainingIterations        = 1000;
        float p1ExplorationTermDelta    = 0;
        float p2ExplorationTermDelta    = 0;
        String p1Playout                = "random";
        String p2Playout                = "random";
        int p1backpropDelta             = 0;
        int p2backpropDelta             = 0;
        Engine.verbose                  = true;
        int p1MinimaxDepth              = 2;
        int p2MinimaxDepth              = 2;
        boolean graphviz                = false;
        int p1ensembleIterations        = 1;
        int p2ensembleIterations        = 1;
        int p1numplayouts               = 1;
        int p2numplayouts               = 1;
        boolean p1deckcheat             = false;
        boolean p2deckcheat             = false;

        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].toLowerCase();
        }

        // This is the case if the arguments are passed by Python using a list. In that case we need to convert the
        // Python syntax to a format which java understands.
        if (args[0].charAt(0) == '[') {
            String argString = Arrays.toString(args);
            argString = argString.replace("[", "");
            argString = argString.replace("]", "");
            argString = argString.replace("'", "");
            args = argString.split(",");

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
                    case "--p1":
                    case "-p1":
                        p1Type = val;
                        break;
                    case "--p2":
                    case "-p2":
                        p2Type = val;
                        break;
                    case "--p1seed":
                        p1RandomSeed = Long.parseLong(val);
                        break;
                    case "--p2seed":
                        p2RandomSeed = Long.parseLong(val);
                        break;
                    case "--deckseed":
                        this.deckRandomSeed = Long.parseLong(val);
                        break;
                    case "--p1explorationterm":
                    case "--p1explorationconstant":
                        p1ExplorationTerm = Float.parseFloat(val);
                        break;
                    case "--p2explorationterm":
                    case "--p2explorationconstant":
                        p2ExplorationTerm = Float.parseFloat(val);
                        break;
                    case "--p1meepleplacementprob":
                        p1MeeplePlacementProb = Float.parseFloat(val);
                        break;
                    case "--p2meepleplacementprob":
                        p2MeeplePlacementProb = Float.parseFloat(val);
                        break;
                    case "--p1trainingiterations":
                        p1TrainingIterations = Integer.parseInt(val);
                        break;
                    case "--p2trainingiterations":
                        p2TrainingIterations = Integer.parseInt(val);
                        break;
                    case "--p1explorationtermdelta":
                        p1ExplorationTermDelta = Float.parseFloat(val);
                        break;
                    case "--p2explorationtermdelta":
                        p2ExplorationTermDelta = Float.parseFloat(val);
                        break;
                    case "--p1playout":
                        p1Playout = val.toLowerCase();
                        break;
                    case "--p2playout":
                        p2Playout = val.toLowerCase();
                        break;
                    case "--p1backpropdelta":
                    case "--p1backpropconst":
                        p1backpropDelta = Integer.parseInt(val);
                        break;
                    case "--p2backpropdelta":
                    case "--p2backpropconst":
                        p2backpropDelta = Integer.parseInt(val);
                        break;
                    case "-v":
                    case "--verbose":
                        Engine.verbose = Boolean.parseBoolean(val);
                        break;
                    case "--p1minimaxdepth":
                        p1MinimaxDepth = Integer.parseInt(val);
                        break;
                    case "--p2minimaxdepth":
                        p2MinimaxDepth = Integer.parseInt(val);
                        break;
                    case "--p1ensembleiterations":
                        p1ensembleIterations = Integer.parseInt(val);
                        break;
                    case "--p2ensembleiterations":
                        p2ensembleIterations = Integer.parseInt(val);
                        break;
                    case "--p1numplayouts":
                        p1numplayouts = Integer.parseInt(val);
                        break;
                    case "--p2numplayouts":
                        p2numplayouts = Integer.parseInt(val);
                        break;
                    case "--p1deckcheat":
                        p1deckcheat = Boolean.parseBoolean(val);
                        break;
                    case "--p2deckcheat":
                        p2deckcheat = Boolean.parseBoolean(val);
                        break;
                    case "-g":
                    case "--graphviz":
                        graphviz = Boolean.parseBoolean(val);
                        break;
                    default: {
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
            case "uct":
                players[0] = new MCTSPlayer(stateSpace, 1, p1ExplorationTerm, p1TrainingIterations, p1RandomSeed, p1MeeplePlacementProb, p1ExplorationTermDelta, "uct", p1Playout, p1backpropDelta, graphviz, p1ensembleIterations, p1numplayouts, p1deckcheat);
                break;
            case "epsilon-greedy":
                players[0] = new MCTSPlayer(stateSpace, 1, p1ExplorationTerm, p1TrainingIterations, p1RandomSeed, p1MeeplePlacementProb, p1ExplorationTermDelta, "epsilon-greedy", p1Playout, p1backpropDelta, graphviz, p1ensembleIterations, p1numplayouts, p1deckcheat);
                break;
            case "decaying-epsilon-greedy":
                players[0] = new MCTSPlayer(stateSpace, 1, p1ExplorationTerm, p1TrainingIterations, p1RandomSeed, p1MeeplePlacementProb, p1ExplorationTermDelta, "decaying-epsilon-greedy", p1Playout, p1backpropDelta, graphviz, p1ensembleIterations, p1numplayouts, p1deckcheat);
                break;
            case "heuristic-mcts":
                players[0] = new MCTSPlayer(stateSpace, 1, p1ExplorationTerm, p1TrainingIterations, p1RandomSeed, p1MeeplePlacementProb, p1ExplorationTermDelta, "heuristic-mcts", p1Playout, p1backpropDelta, graphviz, p1ensembleIterations, p1numplayouts, p1deckcheat);
                break;
            case "uct-tuned":
                players[0] = new MCTSPlayer(stateSpace, 1, p1ExplorationTerm, p1TrainingIterations, p1RandomSeed, p1MeeplePlacementProb, p1ExplorationTermDelta, "uct-tuned", p1Playout, p1backpropDelta, graphviz, p1ensembleIterations, p1numplayouts, p1deckcheat);
                break;
            case "boltzmann":
                players[0] = new MCTSPlayer(stateSpace, 1, p1ExplorationTerm, p1TrainingIterations, p1RandomSeed, p1MeeplePlacementProb, p1ExplorationTermDelta, "boltzmann", p1Playout, p1backpropDelta, graphviz, p1ensembleIterations, p1numplayouts, p1deckcheat);
                break;
            case "human":
                players[0] = new HumanPlayer(stateSpace, 1);
                break;
            case "random":
                players[0] = new RandomPlayer(stateSpace, 1, p1RandomSeed);
                break;
            case "heuristic":
                players[0] = new HeuristicPlayer(stateSpace, 1, p1RandomSeed);
                break;
            case "minimax":
                players[0] = new MinimaxPlayer(stateSpace, 1, p1RandomSeed, p1MinimaxDepth, p1MeeplePlacementProb);
                break;
            default: {

                if (p1Type.contains("decaying")) {
                    players[0] = new MCTSPlayer(stateSpace, 1, p1ExplorationTerm, p1TrainingIterations, p1RandomSeed, p1MeeplePlacementProb, p1ExplorationTermDelta, p1Type, p1Playout, p1backpropDelta, graphviz, p1ensembleIterations, p1numplayouts, p1deckcheat);
                    break;
                }

                System.out.println("Invalid player type for player 1. The options are 'uct'/'[heuristic-]epsilon-greedy'/'human'/'random'/'heuristic'/'heuristic-mcts'.");
                System.exit(1);
            }
        }

        switch (p2Type) {
            case "uct":
                players[1] = new MCTSPlayer(stateSpace, 2, p2ExplorationTerm, p2TrainingIterations, p2RandomSeed, p2MeeplePlacementProb, p2ExplorationTermDelta, "uct", p2Playout, p2backpropDelta, graphviz, p2ensembleIterations, p2numplayouts, p2deckcheat);
                break;
            case "epsilon-greedy":
                players[1] = new MCTSPlayer(stateSpace, 2, p2ExplorationTerm, p2TrainingIterations, p2RandomSeed, p2MeeplePlacementProb, p2ExplorationTermDelta, "epsilon-greedy", p2Playout, p2backpropDelta, graphviz, p2ensembleIterations, p2numplayouts, p2deckcheat);
                break;
            case "decaying-epsilon-greedy":
                players[1] = new MCTSPlayer(stateSpace, 2, p2ExplorationTerm, p2TrainingIterations, p2RandomSeed, p2MeeplePlacementProb, p2ExplorationTermDelta, "decaying-epsilon-greedy", p2Playout, p2backpropDelta, graphviz, p2ensembleIterations, p2numplayouts, p2deckcheat);
                break;
            case "heuristic-mcts":
                players[1] = new MCTSPlayer(stateSpace, 2, p2ExplorationTerm, p2TrainingIterations, p2RandomSeed, p2MeeplePlacementProb, p2ExplorationTermDelta, "heuristic-mcts", p2Playout, p2backpropDelta, graphviz, p2ensembleIterations, p2numplayouts, p2deckcheat);
                break;
            case "uct-tuned":
                players[1] = new MCTSPlayer(stateSpace, 2, p2ExplorationTerm, p2TrainingIterations, p2RandomSeed, p2MeeplePlacementProb, p2ExplorationTermDelta, "uct-tuned", p2Playout, p2backpropDelta, graphviz, p2ensembleIterations, p2numplayouts, p2deckcheat);
                break;
            case "boltzmann":
                players[1] = new MCTSPlayer(stateSpace, 2, p2ExplorationTerm, p2TrainingIterations, p2RandomSeed, p2MeeplePlacementProb, p2ExplorationTermDelta, "boltzmann", p2Playout, p2backpropDelta, graphviz, p2ensembleIterations, p2numplayouts, p2deckcheat);
                break;
            case "human":
                players[1] = new HumanPlayer(stateSpace, 2);
                break;
            case "random":
                players[1] = new RandomPlayer(stateSpace, 2, p2RandomSeed);
                break;
            case "heuristic":
                players[1] = new HeuristicPlayer(stateSpace, 2, p2RandomSeed);
                break;
            case "minimax":
                players[1] = new MinimaxPlayer(stateSpace, 2, p2RandomSeed, p2MinimaxDepth, p2MeeplePlacementProb);
                break;
            default: {

                if (p2Type.contains("decaying")) {
                    players[1] = new MCTSPlayer(stateSpace, 2, p2ExplorationTerm, p2TrainingIterations, p2RandomSeed, p2MeeplePlacementProb, p2ExplorationTermDelta, p2Type, p2Playout, p2backpropDelta, graphviz, p2ensembleIterations, p2numplayouts, p2deckcheat);
                    break;
                }

                System.out.println("Invalid player type for player 2. The options are 'uct'/'[heuristic-]epsilon-greedy'/'human'/'random'/'heuristic'/'heuristic-mcts'.");
                System.exit(1);
            }
        }

        return players;
    }

    public long getDeckRandomSeed() {
        return deckRandomSeed;
    }
}

