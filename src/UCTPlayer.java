import java.util.List;
import java.util.Random;

public class UCTPlayer extends Player {
    private double explorationTerm;

    protected UCTPlayer(int playerID, double explorationTerm) {
        super(playerID);

        this.explorationTerm = explorationTerm;
    }

    @Override
    int[] decideOnNextMove(GameState state, GameStateSpace stateSpace, Tile tile) throws Exception {
        Node root = new Node(state, null, 0, playerID);

        for (int i = 0; i < 5000; i++) {
            Node node = treePolicy(root, stateSpace);

            int payoff = defaultPolicy(node);

            backup(node, payoff);
        }

        return null;
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

    private int defaultPolicy(Node node) {
        while (!node.isTerminal()) {
            node = node.getRandomChild();
        }

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

            double uct = ((double) child.getQValue(playerID) / child.getVisits()) + c * ((2 * Math.log(parent.getVisits())) / child.getVisits());

            if (uct > highestValue) {
                highestValue = uct;
                bestChild = child;
            }
        }

        return bestChild;
    }

    private Node expand(Node placementNode, GameStateSpace stateSpace) {

        if (!placementNode.isPlacementNode()) {
            logger.error("Node needs to be placement node in order to be expanded.");
        }

        List<ActionRotationStateTriple> ars = stateSpace.succ(placementNode.getState(), placementNode.getDrawnTile());

        // First we add the meeple nodes.
        for (ActionRotationStateTriple move : ars) {
            Node meepleNode = new Node(move.getState(), placementNode, 1, playerID);

            placementNode.addChild(meepleNode);
        }

        //Node randomNode = placementNode.getRandomChild();

        // Now we add the chance nodes
        for (Node meepleNode : placementNode.getChildren()) {
            List<Integer> legalMeeples = stateSpace.legalMeeples(meepleNode.getState(), meepleNode.getDrawnTile(), meepleNode.getState().getCoordinates(meepleNode.getDrawnTile()));

            for (int meeplePlacement : legalMeeples) {

                GameState newState = new GameState(meepleNode.getState());
                newState.getTile(newState.getCoordinates(meepleNode.getDrawnTile())).placeMeeple(meeplePlacement, playerID);

                Node chanceNode = new Node(newState, meepleNode, 2, playerID);
                meepleNode.addChild(chanceNode);
            }

            // Then for each chance node we add the placement nodes corresponding to drawing each tile in the deck.
            for (Node chanceNode : meepleNode.getChildren()) {

                List<Tile> tilesInDeck = chanceNode.getState().getDeck();

                //
                for (Tile tile : tilesInDeck) {
                    GameState newState = new GameState(chanceNode.getState());

                    boolean successfullyRemoved = newState.removeFromDeck(tile);

                    if (!successfullyRemoved) {
                        logger.error("Unsuccessfully removed tile from deck.");
                    }

                    Node newPlacementNode = new Node(newState, chanceNode, 0, playerID);
                    newPlacementNode.setDrawnTile(tile);
                    chanceNode.addChild(newPlacementNode);
                }

            }
        }

        // Returns a newly generated placement node at random.
        return placementNode.getRandomChild().getRandomChild().getRandomChild();
    }

    /**
     * @return The child with the best approximated game theoretic value.
     */
    private Node getMostPromisingChild(Node parent) {
        double highestValue = Double.MIN_VALUE;
        Node mostPromisingChild = null;

        for (Node child : parent.getChildren()) {

            double gameTheoreticValue = (double)child.getQValue(playerID) / child.getVisits();

            if (gameTheoreticValue > highestValue) {
                highestValue = gameTheoreticValue;
                mostPromisingChild = child;
            }
        }

        return mostPromisingChild;
    }
}
