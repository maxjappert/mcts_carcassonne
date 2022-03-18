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
        Node root = new Node(state, null, 0, playerID, null, tile);

        for (int i = 0; i < trainingIterations; i++) {
            Node node = treePolicy(root, stateSpace, tile);

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

    private Node treePolicy(Node root, GameStateSpace stateSpace, Tile tile) {
        Node node = root;

        while (!node.isTerminal()) {
            if (!node.hasChildren()) {
                return expand(node, stateSpace, tile);
            } else {
                node = bestChild(node, explorationTerm);
            }
        }

        return node;
    }

    private int defaultPolicy(Node leafNode, GameStateSpace stateSpace) {
        Node node = new Node(leafNode);

        while (!node.isTerminal()) {
            node = expand(node, stateSpace, node.getState().drawTile());
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

    private Node expand(Node placementNode, GameStateSpace stateSpace, Tile tile) {

        if (!placementNode.isPlacementNode()) {
            logger.error("Node needs to be placement node in order to be expanded.");
        }

        List<ActionRotationStateTriple> ars = stateSpace.succ(placementNode.getState(), placementNode.getDrawnTile());

        // First we add the meeple nodes.
        for (ActionRotationStateTriple move : ars) {
            Node meepleNode = new Node(move.getState(), placementNode, 1, playerID, move.getAction(), tile);

            placementNode.addChild(meepleNode);
        }

        // Now we add the chance nodes
        for (Node meepleNode : placementNode.getChildren()) {
            // The legalMeeples method needs the state without the tiles having been placed, therefore we pass the state
            // of the parent node.
            List<Integer> legalMeeples = stateSpace.legalMeeples(placementNode.getState(), meepleNode.getDrawnTile(), meepleNode.getMove());

            for (int meeplePlacement : legalMeeples) {

                GameState newState = new GameState(placementNode.getState());

                Tile newTile = new Tile(meepleNode.getDrawnTile());

                newTile.placeMeeple(meeplePlacement, playerID);

                newState.updateBoard(meepleNode.getMove(), newTile);

                Node chanceNode = new Node(newState, meepleNode, 2, playerID, meepleNode.getMove(), tile);
                meepleNode.addChild(chanceNode);
            }

            // Now add the chance node corresponding to not placing a meeple.
            meepleNode.addChild(new Node(meepleNode.getState(), meepleNode, 2, playerID, meepleNode.getMove(), meepleNode.getDrawnTile()));

            // TODO: From here on there are problems.
            // Then for each chance node we add the placement nodes corresponding to drawing each tile in the deck.
            for (int i = 0; i < meepleNode.getChildren().size(); i++) {

                Node chanceNode = meepleNode.getChildren().get(i);
                List<Tile> tilesInDeck = chanceNode.getState().getDeck();
                List<Integer> consideredTileTypes = new ArrayList<>();


                for (int j = 0; j < tilesInDeck.size(); j++) {
                    Tile tileInDeck = tilesInDeck.get(j);

                    // We don't need placement node for each tile in the deck, only for each tile type in the deck.
                    if (!consideredTileTypes.contains(tileInDeck.getType())) {
                        consideredTileTypes.add(tileInDeck.getType());
                        GameState newState = new GameState(chanceNode.getState());

                        boolean successfullyRemoved = newState.removeFromDeck(tileInDeck);

                        if (!successfullyRemoved) {
                            logger.error("Unsuccessfully removed tile from deck.");
                        }

                        Node newPlacementNode = new Node(newState, chanceNode, 0, playerID, null, tileInDeck);
                        chanceNode.addChild(newPlacementNode);
                    }
                }
            }
        }

        Node returnNode = placementNode.getRandomChild().getRandomChild();

        // Returns a newly generated placement node at random.
        return returnNode;
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
