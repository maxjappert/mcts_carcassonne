import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class UCTPlayer extends Player {
    private final float explorationTerm;
    private final int trainingIterations;


    protected UCTPlayer(GameStateSpace stateSpace, int playerID, float explorationTerm, int trainingIterations) {
        super(stateSpace, playerID);

        this.explorationTerm = explorationTerm;
        this.trainingIterations = trainingIterations;
    }

    @Override
    Pair<Integer, Integer> decideOnNextMove(GameState originalState, Tile tile, List<Tile> originalDeck, List<Move> legalMoves) throws Exception {
        GameState state = new GameState(originalState);
        Node root = new Node(state, 0, playerID, new Move(null, 0), tile);

        for (Move move : legalMoves) {
            root.addChild(new Node(state, 1, playerID, move, tile));
        }

        for (int i = 0; i < trainingIterations; i++) {
            List<Tile> deck = Engine.copyDeck(originalDeck);

            Node node = treePolicy(root, deck);

            int payoff = defaultPolicy(node, deck, 0.5f);

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

        int meeplePlacement = chanceNode.getMeeplePlacement();

        return new Pair<>(moveChoice, meeplePlacement);
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

    private Node treePolicy(Node root, List<Tile> deck) throws Exception {
        Node node = root;

        Random random = new Random();

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
                    node = node.getRandomChild();
                    deck.remove(random.nextInt(deck.size()));
                } else {
                    node = bestChild(node, explorationTerm);
                }
            }
        } while (!node.isTerminal());

        return node;
    }

    /**
     * Random playout.
     * @param leafNode Starting point.
     * @param deck The deck at the starting point.
     * @param meeplePlacementProbability Probability of placing a meeple at a given round.
     * @return The payoff at the end of the playout.
     */
    private int defaultPolicy(Node leafNode, List<Tile> deck, float meeplePlacementProbability) {
        GameState state = new GameState(leafNode.getState());

        // TODO: Player is not allowed to directly change the game state!

        Random random = new Random();
        while (deck.size() > 0) {

            Tile tile = Engine.drawTile(deck);

            List<Move> actions = stateSpace.placementSucc(state, tile);

            if (actions.isEmpty()) {
                deck.add(tile);
                Collections.shuffle(deck);
                continue;
            }

            Move action = actions.get(random.nextInt(actions.size()));

            for (int i = 0; i < action.getRotation(); i++) {
                tile.rotate();
            }

            if (random.nextFloat() < meeplePlacementProbability) {
                List<Integer> legalMeeples = stateSpace.meepleSucc(state, tile, action.getCoords(), playerID);

                if (!legalMeeples.isEmpty()) {
                    int meeplePlacement = legalMeeples.get(new Random().nextInt(legalMeeples.size()));

                    if (meeplePlacement > -1) {
                        state.placeMeeple( meeplePlacement, state.getPlayer(), tile);
                    }
                }
            }

            state.updateBoard(action.getCoords(), tile);

            state.checkForScoreAfterRound();
        }

//        long time1 = System.nanoTime();
        state.assignPointsAtEndOfGame();
//        long time2 = System.nanoTime();
//        System.out.printf("Training needed %f seconds.\n", (double) (time2 - time1) / Math.pow(10, 9));
        return state.getScore()[playerID - 1];
    }

    private void backup(Node node, int payoff) {
        while (node != null) {
            node.updateVisits();
            node.updateQValue(payoff);
            node = node.getParent();
        }
    }

    /**
     * Returns the child with the highest UCT value.
     * @param parent The node whose children should be evaluated.
     * @param c The exploration term.
     * @return The child with the highest UCT value.
     */
    private Node bestChild(Node parent, double c) {
        double highestValue = Double.MIN_VALUE;
        Node bestChild = null;

        if (!parent.hasChildren()) {
            return parent;
            //logger.error("bestChild can't be called for a node without children. Deck size: {}", parent.getState().getDeck().size());
        }

        for (Node child : parent.getChildren()) {

            if (child.getVisits() == 0 && c != 0) {
                return child;
            }

            double uct = ((double) child.getQValue() / child.getVisits()) + 2 * c * ((2 * Math.log(parent.getVisits())) / child.getVisits());

            if (uct > highestValue) {
                highestValue = uct;
                bestChild = child;
            }
        }

        return bestChild;
    }

    private List<Node> getMeepleNodes(Node parent) {
        List<Move> actions = stateSpace.placementSucc(parent.getState(), parent.getDrawnTile());
        List<Node> meepleNodes = new ArrayList<>();

        for (Move action : actions) {

            Node meepleNode = new Node(parent.getState(),  1, parent.getState().getPlayer(), action, parent.getDrawnTile());

            meepleNodes.add(meepleNode);
            //parent.addChild(meepleNode);
        }

        return meepleNodes;
    }

    private List<Node> getChanceNodes(Node parent) {
        List<Integer> legalMeeplePlacements = stateSpace.meepleSucc(parent.getState(), parent.getDrawnTile(), parent.getCoords(), playerID);
        List<Node> chanceNodes = new ArrayList<>();

        if (parent.getState().getNumMeeples(parent.getPlayer()) > 0) {
            for (int legalMeeple : legalMeeplePlacements) {
                Node chanceNode = new Node(parent, 2);

                chanceNode.addMeeple( legalMeeple);

                chanceNodes.add(chanceNode);
            }
        }

        chanceNodes.add(new Node(parent, 2));

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
                newState.placeMeeple(parent.getMeeplePlacement(), parent.getPlayer(), newTile);

                newState.updateBoard(parent.getCoords(), newTile);

                Node placementNode = new Node(newState, 0, otherPlayer(parent.getPlayer()), new Move(new Coordinates(-1, -1), 0), new Tile(tile));
                placementNodes.add(placementNode);
            }
        }

        return placementNodes;
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

        return node.getRandomChild();
    }

    private int otherPlayer(int player) {
        return  (player == 1 ? 2 : 1);
    }
}
