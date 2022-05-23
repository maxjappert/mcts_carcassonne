import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class MCTSPlayer extends Player {

    /**
     *  Denotes the importance of exploration during the tree policy. Corresponds to the c variable in the UCT formula.
     */
    private float explorationTerm;
    final private float originalExplorationTerm;

    /**
     *  The number of training iterations for building the tree for choosing the next move.
     */
    private final int trainingIterations;

    /**
     *  The random object for performing the random playout (default policy).
     */
    private final Random random;

    /**
     *  The probability of considering a meeple placement during the playout (default policy).
     */
    private final float meeplePlacementProbability;

    /**
     *  The random seed for deciding on moves randomly as part of the playout (default policy).
     */
    private final long playoutSeed;

    /**
     *  The gradient of how the exploration term changes over time, i.e., the number added to the exploration term
     *  per move.
     */
    private final float explorationTermDelta;
    private final float backpropDelta;

    private final String treePolicyType;

    private final boolean heuristicPlayout;

    private float backpropWeight;

    private boolean generateGraphwizData;

    private final boolean ensemble;

    private final int ensembleIterations;

    private final int numPlayouts;

    private boolean deckCheat;

    File file;
    FileWriter fw;
    BufferedWriter br;

    /**
     * @param stateSpace A state space object.
     * @param playerID The player ID (1 or 2) of this player.
     * @param explorationTerm Exploration term for UCT (commonly denoted as variable c) the epsilon term in [0, 1] for epsilon-greedy.
     * @param trainingIterations Number of training iterations.
     * @param randomPlayoutSeed Random seed for making the playout reproducible.
     * @param meeplePlacementProbability Probability of placing a meeple during random playout.
     * @param explorationTermDelta Change of the exploration term per iteration.
     * @param treePolicyType Either 'uct' or 'epsilon-greedy'.
     */
    protected MCTSPlayer(GameStateSpace stateSpace, int playerID, float explorationTerm, int trainingIterations,
                         long randomPlayoutSeed, float meeplePlacementProbability, float explorationTermDelta,
                         String treePolicyType, boolean heuristicPlayout, float backpropDelta, boolean generateGraphwizData,
                         int ensembleIterations, int numPlayouts, boolean deckCheat) {
        super(stateSpace, playerID);

        this.explorationTerm = explorationTerm;
        this.trainingIterations = trainingIterations;
        if (randomPlayoutSeed == -1) {
            long seed = new Random().nextInt(Integer.MAX_VALUE);
            this.random = new Random(seed);
            this.playoutSeed = seed;
        } else {
            this.random = new Random(randomPlayoutSeed);
            this.playoutSeed = randomPlayoutSeed;
        }

        if (meeplePlacementProbability > 1 || meeplePlacementProbability < 0) {
            this.meeplePlacementProbability = 0.5f;
        } else {
            this.meeplePlacementProbability = meeplePlacementProbability;
        }

        this.explorationTermDelta = explorationTermDelta;
        this.treePolicyType = treePolicyType.toLowerCase();

        if (treePolicyType.equalsIgnoreCase("epsilon-greedy-uct") && explorationTerm >= 1) {
            Engine.printError(" Invalid epsilon.");
        }

        if (treePolicyType.equalsIgnoreCase("decaying-epsilon-greedy")) {
            originalExplorationTerm = 1;
        } else {
            originalExplorationTerm = explorationTerm;
        }

        this.heuristicPlayout = heuristicPlayout;
        this.backpropWeight = 1;
        this.backpropDelta = backpropDelta;
        this.generateGraphwizData = generateGraphwizData;

        this.ensembleIterations = ensembleIterations;

        ensemble = ensembleIterations > 1;

        this.numPlayouts = numPlayouts;
        this.deckCheat = deckCheat;
    }

    @Override
    Pair decideOnNextMove(GameState originalState, Tile tile, List<Tile> originalDeck, List<Move> legalMoves) throws IOException {
        GameState state = new GameState(originalState);
        Node root = new Node(state, 0, new Move(null, 0), tile);

//        generateGraphwizData = originalDeck.size() >= 70 && generateGraphwizData;
//
//        if (generateGraphwizData) {
//            file = new File("/home/maxja/mcts_carcassonne/tree.dot");
//            file.delete();
//            file.createNewFile();
//            fw = new FileWriter(file.getAbsoluteFile(), true);
//            br = new BufferedWriter(fw);
//
//            // For removing the points, the width can be set to 0.01.
//            br.write("graph \"\"\n" +
//                       "{\n" +
//                        "fontname=\"Helvetica,Arial,sans-serif\"\n" +
//                        "node [fontname=\"Helvetica,Arial,sans-serif\" width=0.1 shape=point]\n" +
//                        "edge [fontname=\"Helvetica,Arial,sans-serif\"]\n");
//
//
//            br.write("n" + root.id + " [label=\"\", fillcolor=" + root.getColour() + "] ; \n\n");
//            br.flush();
//        }

        for (Move move : legalMoves) {

            Node node = new Node(state, 1, move, tile);

            root.addChild(node);

            //System.out.println(root.id);
            if (generateGraphwizData) {
                br.write("n" + root.id + " -- n" + node.id + " ;\n\n");
                br.write("n" + node.id + " [label=\"\", fillcolor= " + node.getColour() + "] ;\n\n");
                br.flush();
            }
        }

        int[] moveChoices = new int[ensembleIterations];
        int[] meeplePlacements = new int[ensembleIterations];

        for (int k = 0; k < ensembleIterations; k++) {
            for (int i = 0; i < trainingIterations; i++) {
                List<Tile> deck = Engine.copyDeck(originalDeck);

                if (ensemble) {
                    Collections.shuffle(deck, random);
                }

                Node node = treePolicy(root, deck, i);

                for (int j = 0; j < numPlayouts; j++) {
                    int[] payoff = defaultPolicy(node.getState(), deck, heuristicPlayout);

                    backup(node, payoff);
                }

                updateBackpropWeight(backpropDelta);

                if (treePolicyType.contains("decaying")) {
                    explorationTerm = originalExplorationTerm / i;
                }
            }

            backpropWeight = 1;

            // Exploration term set to 0, since when executing we only want to consider the exploitation term.
            Node meepleNode = bestChildUCT(root, 0, 0);

            int moveChoice;
            if (legalMoves.contains(meepleNode.getMove())) {
                moveChoice = legalMoves.indexOf(meepleNode.getMove());
            } else {
                Engine.printError("Error in choice system of UCTPlayer!!!");
                moveChoice = -1;
            }

            Node chanceNode = bestChildUCT(meepleNode, 0, 0);

            int meeplePlacement = chanceNode.getMeeplePlacement();

            moveChoices[k] = moveChoice;
            meeplePlacements[k] = meeplePlacement;
        }

        updateExplorationTerm(explorationTermDelta);

        if (generateGraphwizData) {
            br.write("}");
            br.flush();
            br.close();
            fw.close();
        }

        if (ensemble) return new Pair(mostFrequent(moveChoices), mostFrequent(meeplePlacements));
        else return new Pair(moveChoices[0], meeplePlacements[0]);
    }

    static int mostFrequent(int arr[])
    {
        // Sort the array
        Arrays.sort(arr);

        // find the max frequency using linear traversal
        int max_count = 1, res = arr[0];
        int curr_count = 1;

        for (int i = 1; i < arr.length; i++) {
            if (arr[i] == arr[i - 1])
                curr_count++;
            else
                curr_count = 1;

            if (curr_count > max_count) {
                max_count = curr_count;
                res = arr[i - 1];
            }
        }
        return res;
    }

    private int getTreeSize(Node root) {
        int treeSize = 0;

        for (Node node : root.getChildren()) {
            treeSize++;
            treeSize += getTreeSize(node);
        }

        return treeSize;
    }

    public float getExplorationTerm() {
        return explorationTerm;
    }

    public int getTrainingIterations() {
        return trainingIterations;
    }

    private Node treePolicy(Node root, List<Tile> deck, int iterations) throws IOException {
        Node node = root;

        do {
            if (!node.hasChildren()) {
                do {
                    node = expand(node, deck);
                } while (node.getType() != 0 && !node.isTerminal());

                if (!deck.isEmpty() && !ensemble && !deckCheat) {
                    deck.remove(random.nextInt(deck.size()));
                }
                return node;
            } else {
                if (node.getType() == 2) {
                    node = node.getRandomChild(random);
                    deck.remove(random.nextInt(deck.size()));
                } else {
                    if (treePolicyType.contains("uct")) {
                        node = bestChildUCT(node, explorationTerm, iterations);
                    } else if (treePolicyType.equals("heuristic-mcts")) {
                        node = bestChildHeuristic(node);
                    } else if (treePolicyType.contains("epsilon-greedy")) {
                        if (random.nextFloat() < explorationTerm) {
                            node = node.getRandomChild(random);
                        } else {
                            node = treePolicyType.equalsIgnoreCase("heuristic-epsilon-greedy") ? bestChildHeuristic(node) : bestChildUCT(node, 0, 0);
                        }
                    } else if (treePolicyType.contains("boltzmann")) {
                        node = getBoltzmannNode(node.getChildren());
                    } else {
                        Engine.printError(" Invalid tree policy type");
                        System.exit(-1);
                    }
                }
            }
        } while (!node.isTerminal());

        return node;
    }

    /**
     * Random playout.
     * @param originalState Starting point.
     * @param deck The deck at the starting point.
     * @return The payoff at the end of the playout.
     */
    private int[] defaultPolicy(GameState originalState, List<Tile> deck, boolean heuristic) {
        GameState state = new GameState(originalState);

        while (deck.size() > 0) {
            Tile tile = Engine.drawTile(deck);
            List<Move> actions = stateSpace.placementSucc(state, tile);

            if (actions.isEmpty()) {
                deck.add(tile);
                Collections.shuffle(deck, random);
                continue;
            }

            Move action;

            if (heuristic) {
                action = new Move(new Coordinates(-1, -1), -1);
                int bestH = Integer.MIN_VALUE;

                for (Move cand : actions) {
                    int h = stateSpace.moveHeuristic(state, cand, tile, state.getPlayer());
                    if (h > bestH) {
                        bestH = h;
                        action = cand;
                    }
                }
            } else {
                action = actions.get(random.nextInt(actions.size()));
            }

            for (int i = 0; i < action.getRotation(); i++) {
                tile.rotate();
            }

            int meeplePlacement = -1;
            List<Integer> legalMeeples = stateSpace.meepleSucc(state, tile, action.getCoords(), playerID);

            if (heuristic) {
                int bestH = Integer.MIN_VALUE;

                for (int meeple : legalMeeples) {
                    int h = stateSpace.meepleHeuristic(state, tile, meeple, state.getPlayer());
                    if (h > bestH) {
                        bestH = h;
                        meeplePlacement = meeple;
                    }
                }
            } else {
                if (random.nextFloat() < meeplePlacementProbability) {
                    meeplePlacement = legalMeeples.get(random.nextInt(legalMeeples.size()));
                }
            }

            if (meeplePlacement > -1) {
                state.placeMeeple(meeplePlacement, state.getPlayer(), tile);
            }

            state.updateBoard(action.getCoords(), tile);

            state.checkForScoreAfterRound(false);
        }

        state.assignPointsAtEndOfGame();
        return state.getScore();
    }

    private void backup(Node node, int[] payoff) {
        while (node != null) {
            node.updateVisits();
            payoff[0] *= backpropWeight;
            payoff[1] *= backpropWeight;
            node.updateQValue(payoff);
            node = node.getParent();
        }
    }

    /**
     * Using the most visited child node to choose the next action minimises the payoff for some reason.
     * Choosing random actions is better.
     * @param parent The parent whose most visited child should be returned.
     * @return The most visited child of the parent.
     */
    private Node mostVisitedChild(Node parent) {
        if (!parent.hasChildren()) {
            return parent;
        }

        List<Node> childrenCopy  = new ArrayList<>(List.copyOf(parent.getChildren()));
        childrenCopy.sort((n1, n2) -> n2.getVisits() - n1.getVisits());
        return childrenCopy.get(0);
    }

    /**
     * Returns the child with the highest UCT value.
     * @param parent The node whose children should be evaluated.
     * @param c The exploration term.
     * @return The child with the highest UCT value.
     */
    private Node bestChildUCT(Node parent, double c, int iterations) {

        double highestValue = Double.MIN_VALUE;
        Node bestChild = null;//parent.getRandomChild(random);

        if (!parent.hasChildren()) {
            return parent;
        }

        boolean flag = false;
        for (Node child : parent.getChildren()) {

            if (child.getVisits() == 0 && c != 0) {
                return child;
            }

            int player = child.getState().getPlayer();

            double upperConfidenceBound = getUpperConfidenceBound(child, parent.getVisits(), iterations);
            double ucb1 = ((double) child.getQValue()[player-1] / child.getVisits()) + 2 * c * upperConfidenceBound;

            if (ucb1 > highestValue) {
                highestValue = ucb1;
                bestChild = child;
                flag = true;
            }
        }

        if (!flag) Engine.printError(" Random next move selected because child had 0 visits.");

        if (bestChild != null) {
            return bestChild;
        } else {
            return parent.getRandomChild(random);
        }
    }

    private Node bestChildHeuristic(Node parent) {
        int type = parent.getType();
        int highestH = Integer.MIN_VALUE;
        Node bestNode = null;

        if (!parent.hasChildren()) return parent;

        for (Node child : parent.getChildren()) {
            if (type == 0 && stateSpace.moveHeuristic(parent.getState(), child.getMove(), parent.getDrawnTile(), parent.getState().getPlayer()) > highestH) {
                highestH = stateSpace.moveHeuristic(parent.getState(), child.getMove(), parent.getDrawnTile(), parent.getState().getPlayer());
                bestNode = child;
            } else if (type == 1 && stateSpace.meepleHeuristic(parent.getState(), parent.getDrawnTile(), child.getMeeplePlacement(), parent.getState().getPlayer()) > highestH) {
                highestH = stateSpace.meepleHeuristic(parent.getState(), parent.getDrawnTile(), child.getMeeplePlacement(), parent.getState().getPlayer());
                bestNode = child;
            } else if (type == 2) {
                bestNode = parent.getRandomChild(random);
                break;
            }
        }

        return bestNode;
    }

    private List<Node> getMeepleNodes(Node parent) {
        List<Move> actions = stateSpace.placementSucc(parent.getState(), parent.getDrawnTile());
        List<Node> meepleNodes = new ArrayList<>();

        for (Move action : actions) {
            Node meepleNode = new Node(parent.getState(), 1, action, parent.getDrawnTile());

            meepleNodes.add(meepleNode);
        }

        return meepleNodes;
    }

    private List<Node> getChanceNodes(Node parent) {
        List<Integer> legalMeeplePlacements = stateSpace.meepleSucc(parent.getState(), parent.getDrawnTile(), parent.getCoords(), playerID);
        List<Node> chanceNodes = new ArrayList<>();

        if (parent.getState().getNumMeeples(parent.getState().getPlayer()) > 0) {
            for (int legalMeeple : legalMeeplePlacements) {
                Node chanceNode = new Node(parent, 2);

                chanceNode.addMeeple(legalMeeple);

                chanceNodes.add(chanceNode);
            }
        }

        return chanceNodes;
    }

    private List<Node> getPlacementNodes(Node parent, List<Tile> deck) {

        List<Node> placementNodes = new ArrayList<>();
        List<Integer> consideredTiles = new ArrayList<>();

        if (ensemble || deckCheat) {
            Tile tile = new Tile(deck.remove(0));

            GameState newState = new GameState(parent.getState());

            Tile newTile = new Tile(parent.getDrawnTile());

            newTile.rotateBy(parent.getRotation());
            newState.placeMeeple(parent.getMeeplePlacement(), parent.getState().getPlayer(), newTile);

            newState.updateBoard(parent.getCoords(), newTile);

            Node placementNode = new Node(newState, 0, new Move(new Coordinates(-1, -1), 0), new Tile(tile));
            placementNodes.add(placementNode);
        } else {
            for (Tile tile : deck) {
                if (!consideredTiles.contains(tile.getType())) {
                    consideredTiles.add(tile.getType());

                    GameState newState = new GameState(parent.getState());

                    Tile newTile = new Tile(parent.getDrawnTile());

                    newTile.rotateBy(parent.getRotation());
                    newState.placeMeeple(parent.getMeeplePlacement(), parent.getState().getPlayer(), newTile);

                    newState.updateBoard(parent.getCoords(), newTile);

                    Node placementNode = new Node(newState, 0, new Move(new Coordinates(-1, -1), 0), new Tile(tile));
                    placementNodes.add(placementNode);
                }
            }
        }

        return placementNodes;
    }

    private Node expand(Node node, List<Tile> deck) throws IOException {
        List<Node> children = new ArrayList<>();

        if (node.getType() == 0) {
            children = getMeepleNodes(node);
        } else if (node.getType() == 1) {
            children = getChanceNodes(node);
        } else if (node.getType() == 2) {
            children = getPlacementNodes(node, deck);
        } else {
            Engine.printError("Error in expand!!!");
        }

        if (children.isEmpty()) {
            return node;
        }

        node.addChildren(children);

        if (generateGraphwizData) {
            for (Node child : children) {
                br.write("n" + node.id + " -- n" + child.id + " ; \n\n");
                br.write("n" + child.id + " [label=\"\", fillcolor=" + child.getColour() + "] ;\n\n");
                br.flush();
                //System.out.println(node.id);
            }
        }

        return node.getRandomChild(random);
    }

    private int otherPlayer(int player) {
        return  (player == 1 ? 2 : 1);
    }

    public String getTypeAsString() {
        return "UCT Player";
    }

    public long getPlayoutSeed() {
        return playoutSeed;
    }

    public float getPlayoutMeeplePlacementProbability() {
        return meeplePlacementProbability;
    }

    public void updateExplorationTerm(float delta) {
        if (explorationTerm + delta < 0) {
            explorationTerm = 0;
        } else {
            explorationTerm = explorationTerm + delta;
        }
    }

    private void updateBackpropWeight(float delta) {
        backpropWeight += delta;
    }

    public Node getBoltzmannNode(List<Node> nodes) {
        double normalisationTerm = 0;

        for (Node node : nodes) {

            if (node.getVisits() == 0) {
                return node;
            }

            double exponent = ((double)node.getQValue()[playerID-1] / node.getVisits()) / explorationTerm;
            normalisationTerm += Math.exp(exponent);
        }

        List<Double> boltzmannValues = new ArrayList<>(nodes.size());
        double sum = 0;
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            double value = Math.exp(((double)node.getQValue()[playerID-1]/node.getVisits())/explorationTerm) / normalisationTerm;
            boltzmannValues.add(i, value);
            sum += value;
        }

        if (sum < 0.98 || sum > 1.02) Engine.printError("** Error in getBoltzmannNode(...):  Sum or probabilities is " + sum);

        while (true) {
            double r = random.nextDouble();
            int index = random.nextInt(boltzmannValues.size());
            double p = boltzmannValues.get(index);

            if (r < p) {
                return nodes.get(index);
            }
        }
    }

    private double getUpperConfidenceBound(Node child, int parentVisits, int iterations) {
        if (treePolicyType.equals("uct")) return Math.sqrt((2 * Math.log(parentVisits)) / child.getVisits());
        else if (treePolicyType.equals("uct-tuned")) return Math.sqrt((Math.log(parentVisits) / child.getVisits()) * Math.min(0.25, V(child, iterations)));
        return 0;
    }

    private double V(Node child, int iterations) {
        int player = child.getState().getPlayer();
        double slice = Math.pow((double)child.getQValue()[player-1] / child.getVisits(), 2);
        double logIts = iterations == 0 ? 0 : Math.log(iterations);
        return (1.0 / child.getVisits()) * slice * child.getVisits() - ((double)child.getQValue()[player-1] / child.getVisits()) + Math.sqrt((2 * logIts)/child.getVisits());
    }
}
