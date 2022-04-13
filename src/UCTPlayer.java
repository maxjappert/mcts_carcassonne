import java.util.*;

//TODO: rename to MCTSPlayer
public class UCTPlayer extends Player {

    /**
     *  Denotes the importance of exploration during the tree policy. Corresponds to the c variable in the UCT formula.
     */
    private float explorationTerm;

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

    private final String treePolicyType;

    private final boolean heuristicPlayout;

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
    protected UCTPlayer(GameStateSpace stateSpace, int playerID, float explorationTerm, int trainingIterations,
                        long randomPlayoutSeed, float meeplePlacementProbability, float explorationTermDelta, String treePolicyType, boolean heuristicPlayout) {
        super(stateSpace, playerID);

        this.explorationTerm = explorationTerm;
        this.trainingIterations = trainingIterations;
        if (randomPlayoutSeed == -1) {
            this.random = new Random();
        } else {
            this.random = new Random(randomPlayoutSeed);
        }

        this.playoutSeed = randomPlayoutSeed;

        if (meeplePlacementProbability > 1 || meeplePlacementProbability < 0) {
            this.meeplePlacementProbability = 0.5f;
        } else {
            this.meeplePlacementProbability = meeplePlacementProbability;
        }

        this.explorationTermDelta = explorationTermDelta;
        this.treePolicyType = treePolicyType.toLowerCase();

        if (treePolicyType.equalsIgnoreCase("epsilon-greedy-uct") && explorationTerm >= 1) {
            System.out.println("** Invalid epsilon.");
        }

        this.heuristicPlayout = heuristicPlayout;
    }

    @Override
    Pair decideOnNextMove(GameState originalState, Tile tile, List<Tile> originalDeck, List<Move> legalMoves) {
        GameState state = new GameState(originalState);
        Node root = new Node(state, 0, new Move(null, 0), tile);

        for (Move move : legalMoves) {

            Node node = new Node(state, 1, move, tile);

            if (root.getChildren().contains(node)) {
                System.out.println("");
            }

            root.addChild(node);
        }

        for (int i = 0; i < trainingIterations; i++) {
            List<Tile> deck = Engine.copyDeck(originalDeck);

            Node node = treePolicy(root, deck);

            int[] payoff = defaultPolicy(node.getState(), deck, heuristicPlayout);

            backup(node, payoff);
        }

        //visualizeGraph(root);

        // Exploration term set to 0, since when executing we only want to consider the exploitation term.
        Node meepleNode = bestChild(root, 0);

        int moveChoice;
        if (legalMoves.contains(meepleNode.getMove())) {
            moveChoice = legalMoves.indexOf(meepleNode.getMove());
        } else {
            System.out.println("Error in choice system of UCTPlayer!!!");
            moveChoice = -1;
        }

        Node chanceNode = bestChild(meepleNode, 0);
        //Node chanceNode = mostVisitedChild(meepleNode);

        int meeplePlacement = chanceNode.getMeeplePlacement();

        updateExplorationTerm(explorationTermDelta);

        return new Pair(moveChoice, meeplePlacement);
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

    //    private void visualizeGraph(Node root) {
//        Graph graph = new SingleGraph("Test");
//
//        graph.setStrict(false);
//        graph.setAutoCreate( true );
//
////        graph.addNode("A");
////        graph.addNode("B");
////        graph.addEdge("AB", "A", "B");
////
//        System.setProperty("org.graphstream.ui", "swing");
////        graph.display();
//
//        expandVisualisation(root, graph);
//
//        Viewer view = graph.display(false);
//        HierarchicalLayout hl = new HierarchicalLayout();
//        view.enableAutoLayout(hl);
//        //view.disableAutoLayout();
//    }

    //private void expandVisualisation(Node root, Graph graph) {
//        List<Node> children = root.getChildren();
//
//        if (children.isEmpty()) {
//            return;
//        }
//
//        for (Node child : children) {
//            graph.addEdge("" + root.id + child.id, Long.toString(root.id), Long.toString(child.id));
//        }
//
//        for (Node child : children) {
//            expandVisualisation(child, graph);
//        }
//    }

    private Node treePolicy(Node root, List<Tile> deck) {
        Node node = root;

        do {
            if (!node.hasChildren()) {
                do {
                    node = expand(node, deck);
                } while (node.getType() != 0 && !node.isTerminal());

                if (!deck.isEmpty()) {
                    deck.remove(random.nextInt(deck.size()));
                }
                return node;
            } else {
                if (node.getType() == 2) {
                    node = node.getRandomChild(random);
                    deck.remove(random.nextInt(deck.size()));
                } else {
                    if (treePolicyType.equalsIgnoreCase("uct")) {
                        node = bestChild(node, explorationTerm);
                    } else if (treePolicyType.equalsIgnoreCase("epsilon-greedy")) {
                        if (random.nextFloat() < explorationTerm) {
                            node = node.getRandomChild(random);
                        } else {
                            node = bestChild(node, 0);
                        }
                    } else {
                        System.out.println("** Invalid tree policy type");
                        System.exit(-1);
                    }
                }
            }
        } while (!node.isTerminal());

        return node;
    }

    private boolean hasUnexploredChildren(Node parent) {
        if (parent.getType() == 0) {
            List<Move> successors = stateSpace.placementSucc(parent.getState(), parent.getDrawnTile());
            return successors.size() != parent.getChildren().size();
        } else if (parent.getType() == 1) {
            List<Integer> successors = stateSpace.meepleSucc(parent.getState(), parent.getDrawnTile(), parent.getMove().getCoords(), parent.getState().getPlayer());
            return successors.size() != parent.getChildren().size();
        }

        return false;
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
            node.updateQValue(payoff);
            node = node.getParent();
        }
    }

    private void backupNegamax(Node node, int[] payoff) {
        while (node != null) {
            node.updateVisits();
            node.updateQValue(payoff);
            payoff[0] = -payoff[0];
            payoff[1] = -payoff[1];
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
    private Node bestChild(Node parent, double c) {

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

            double uct = ((double) child.getQValue()[player-1] / child.getVisits()) + 2 * c * ((2 * Math.log(parent.getVisits())) / child.getVisits());

            if (uct > highestValue) {
                highestValue = uct;
                bestChild = child;
                flag = true;
            }
        }

        if (!flag) System.out.println("** Random next move selected because child had 0 visits.");

        if (bestChild != null) {
            return bestChild;
        } else {
            return parent.getRandomChild(random);
        }
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

    private Node getUnexpandedMeepleNode(Node parent) {
        List<Move> actions = stateSpace.placementSucc(parent.getState(), parent.getDrawnTile());
        List<Node> children = parent.getChildren();

        for (Move action : actions) {
            boolean flag = true;
            for (Node child : children) {
                if (child.getMove().isEqualTo(action)) {
                    flag = false;
                    break;
                }
            }


            if (flag) return new Node(parent.getState(), 1, action, parent.getDrawnTile());
        }

        System.out.println("** Error in getting single meeple node.");
        return null;
    }

    private Node getUnexpandedChanceNode(Node parent) {
        List<Integer> legalMeeplePlacements = stateSpace.meepleSucc(parent.getState(), parent.getDrawnTile(), parent.getCoords(), playerID);
        List<Node> children = parent.getChildren();

        if (parent.getState().getNumMeeples(parent.getState().getPlayer()) > 0) {
            for (int legalMeeple : legalMeeplePlacements) {
                boolean flag = true;
                for (Node child : children) {
                    if (child.getMeeplePlacement() == legalMeeple) {
                        flag = false;
                        break;
                    }
                }

                if (flag) {
                    Node chanceNode = new Node(parent, 2);
                    chanceNode.addMeeple(legalMeeple);
                    return chanceNode;
                }
            }
        }

        System.out.println("** Error in getting single chance node.");
        return null;
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

        return placementNodes;
    }

    private Node expandSingleNode(Node node, List<Tile> deck) {
        List<Node> children = new ArrayList<>();

        if (node.getType() == 0) {
            Node child = getUnexpandedMeepleNode(node);
            node.addChild(child);
            return child;
        } else if (node.getType() == 1) {
            Node child = getUnexpandedChanceNode(node);

            node.addChild(child);
            return child;
        } else if (node.getType() == 2) {
            children = getPlacementNodes(node, deck);

            if (children.isEmpty()) {
                return node;
            }

            node.addChildren(children);
            return node.getRandomChild(random);
        } else {
            System.out.println("** Error in expand!!!");
            return null;
        }
    }

    private Node expand(Node node, List<Tile> deck) {
        List<Node> children = new ArrayList<>();

        if (node.getType() == 0) {
            children = getMeepleNodes(node);
        } else if (node.getType() == 1) {
            children = getChanceNodes(node);
        } else if (node.getType() == 2) {
            children = getPlacementNodes(node, deck);
        } else {
            System.out.println("** Error in expand!!!");
        }

        if (children.isEmpty()) {
            return node;
        }

        node.addChildren(children);

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
        if (explorationTerm - delta < 0) {
            System.out.println("** Negative exploration term!");
            System.exit(-1);
        }

        explorationTerm = explorationTerm + delta;
    }
}
