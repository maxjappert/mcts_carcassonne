import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UCTPlayer extends Player {
    private final double explorationTerm;
    private final int trainingIterations;


    protected UCTPlayer(int playerID, double explorationTerm, int trainingIterations) {
        super(playerID);

        this.explorationTerm = explorationTerm;
        this.trainingIterations = trainingIterations;
    }

    @Override
    int[] decideOnNextMove(GameState state, GameStateSpace stateSpace, Tile tile) throws Exception {
        Node root = new Node(state, null, 0, playerID, null, tile, 0);

        for (int i = 0; i < trainingIterations; i++) {
            Node node = treePolicy(root, stateSpace);

            int payoff = defaultPolicy(node, stateSpace);

            backup(node, payoff);
        }

        // Exploration term set to 0, since when executing we only want to consider the exploitation term.
        Node meepleNode = bestChild(root, 0);

        int[] move = meepleNode.getMove();

        for (int i = 0; i < meepleNode.getDrawnTile().getRotation(); i++) {
            tile.rotate();
        }

        Node chanceNode = bestChild(meepleNode, 0);

        int meeplePlacement = chanceNode.getDrawnTile().getMeeple()[0];

        if (meeplePlacement > -1) {
            tile.placeMeeple(meeplePlacement, playerID);
        }

        return move;
    }

    private Node treePolicy(Node root, GameStateSpace stateSpace) {
        Node node = root;

        while (!node.isTerminal()) {
            if (!node.hasChildren()) {
                return expand(node, stateSpace);
            } else {
                node = bestChild(node, explorationTerm);
            }
        }

        return node;
    }

    private int defaultPolicy(Node leafNode, GameStateSpace stateSpace) {
        Node node = new Node(leafNode, 0);

        GameState state = new GameState(leafNode.getState());

        while (!state.isTerminal()) {

            Tile tile = state.drawTile();

            List<ActionRotationStateTriple> actions = stateSpace.succ(state, tile);

            ActionRotationStateTriple action = actions.get(new Random().nextInt(actions.size()));

            for (int i = 0; i < action.getRotation(); i++) {
                tile.rotate();
            }

            List<Integer> legalMeeples = stateSpace.legalMeeples(state, tile, action.getAction());

            int meeplePlacement = legalMeeples.get(new Random().nextInt(legalMeeples.size()));

            if (meeplePlacement > -1) {
                tile.placeMeeple(meeplePlacement, state.getPlayer());
            }

            state.updateBoard(action.getAction(), tile);
            state.checkForScoreAfterRound();
        }

        state.assignPointsAtEndOfGame();

        return node.getState().getScore()[playerID - 1];
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
            logger.error("bestChild can't be called for a node without children");
        }

        for (Node child : parent.getChildren()) {

            if (child.getVisits() == 0) {
                return child;
            }

            double uct = ((double) child.getQValue() / child.getVisits()) + c * ((2 * Math.log(parent.getVisits())) / child.getVisits());

            if (uct > highestValue) {
                highestValue = uct;
                bestChild = child;
            }
        }

        return bestChild;
    }

    private List<Node> addMeepleNodes(Node parent, GameStateSpace stateSpace) {
        List<ActionRotationStateTriple> actions = stateSpace.succ(parent.getState(), parent.getDrawnTile());
        List<Node> meepleNodes = new ArrayList<>();

        for (ActionRotationStateTriple action : actions) {
            Node meepleNode = new Node(parent.getState(), parent, 1, playerID, action.getAction(), parent.getDrawnTile(), action.getRotation());

            meepleNodes.add(meepleNode);
            parent.addChild(meepleNode);
        }

        return meepleNodes;
    }

    private List<Node> addChanceNodes(Node parent, GameStateSpace stateSpace) {
        List<Integer> legalMeeplePlacements = stateSpace.legalMeeples(parent.getState(), parent.getDrawnTile(), parent.getMove());
        List<Node> chanceNodes = new ArrayList<>();

        for (int legalMeeple : legalMeeplePlacements) {
            Node chanceNode = new Node(parent, 2);
            chanceNode.addMeeple(legalMeeple);

            parent.addChild(chanceNode);
            chanceNodes.add(chanceNode);
        }

        return chanceNodes;
    }

    private List<Node> addPlacementNodes(Node parent, GameStateSpace stateSpace) {
        List<Tile> deck = parent.getState().getDeck();
        List<Node> placementNodes = new ArrayList<>();
        List<Integer> consideredTiles = new ArrayList<>();

        for (Tile tile : deck) {
            if (!consideredTiles.contains(tile)) {
                consideredTiles.add(tile.getType());

                GameState newState = new GameState(parent.getState());
                Tile newTile = new Tile(parent.getDrawnTile());

                for (int i = 0; i < parent.getRotation(); i++) {
                    newTile.rotate();
                }

                newState.updateBoard(parent.getMove(), newTile);

                Node placementNode = new Node(newState, parent, 0, otherPlayer(playerID), new int[]{-1, -1}, tile, 0);
                placementNodes.add(placementNode);
                parent.addChild(placementNode);
            }
        }

        return placementNodes;
    }

    private Node expand(Node placementNode, GameStateSpace stateSpace) {
        List<Node> meepleNodes = addMeepleNodes(placementNode, stateSpace);
        List<Node> placementNodes = new ArrayList<>();

        for (Node meepleNode : meepleNodes) {
            List<Node> chanceNodes = addChanceNodes(meepleNode, stateSpace);

            for (Node chanceNode : chanceNodes) {
                placementNodes = addPlacementNodes(chanceNode, stateSpace);
            }
        }

        // return a random following placement node.
        return placementNodes.get(new Random().nextInt(placementNodes.size()));
    }

    private int otherPlayer(int player) {
        return player == 1 ? 2 : 1;
    }

    /**
     * @return The child with the best approximated game theoretic value.
     */
    private Node getMostPromisingChild(Node parent) {
        double highestValue = Double.MIN_VALUE;
        Node mostPromisingChild = null;

        for (Node child : parent.getChildren()) {

            double gameTheoreticValue = (double)child.getQValue() / child.getVisits();

            if (gameTheoreticValue > highestValue) {
                highestValue = gameTheoreticValue;
                mostPromisingChild = child;
            }
        }

        return mostPromisingChild;
    }
}
