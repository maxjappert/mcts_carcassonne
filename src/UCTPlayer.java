import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class UCTPlayer extends Player {
    private final float explorationTerm;
    private final short trainingIterations;


    protected UCTPlayer(byte playerID, float explorationTerm, short trainingIterations) {
        super(playerID);

        this.explorationTerm = explorationTerm;
        this.trainingIterations = trainingIterations;
    }

    @Override
    byte[] decideOnNextMove(GameState originalState, GameStateSpace stateSpace, Tile tile, List<Tile> deck) throws Exception {
        GameState state = new GameState(originalState);
        Node root = new Node(state, (byte) 0, playerID, null, tile, (byte) 0);

        for (int i = 0; i < trainingIterations; i++) {
            Node node = treePolicy(root, stateSpace, deck);

            int payoff = defaultPolicy(node, stateSpace, deck);

            backup(node, payoff);
        }

        // Exploration term set to 0, since when executing we only want to consider the exploitation term.
        Node meepleNode = bestChild(root, 0);

        if (meepleNode == null) {
            return new byte[]{-1, -1};
        }

        byte[] move = meepleNode.getMove();

        tile.rotateBy(meepleNode.getRotation());

        Node chanceNode = bestChild(meepleNode, 0);

        if (chanceNode != null) {
            int meeplePlacement = chanceNode.getMeeplePlacement();

            if (meeplePlacement > -1) {
                tile.placeMeeple((byte) meeplePlacement, playerID, originalState);
            }
        }

        return move;
    }

    private Node treePolicy(Node root, GameStateSpace stateSpace, List<Tile> deck) throws Exception {
        Node node = root;

        do {
            if (!node.hasChildren()) {
                do {
                    node = expand(node, stateSpace, deck);
                } while (node.getType() != 0 && !node.isTerminal());

                return node;
            } else {
                if (node.getType() == 2) {
                    node = node.getRandomChild();
                } else {
                    node = bestChild(node, explorationTerm);
                }
            }
        } while (!node.isTerminal());

        return node;
    }

    private int defaultPolicy(Node leafNode, GameStateSpace stateSpace, List<Tile> deck_) {
        GameState state = new GameState(leafNode.getState());

        List<Tile> deck = Engine.copyDeck(deck_);

        while (!state.isTerminal()) {

            Tile tile = Engine.drawTile(deck);

            List<ActionRotationStateTriple> actions = stateSpace.succ(state, tile);

            if (actions.isEmpty()) {
                deck.add(tile);
                Collections.shuffle(deck);
                continue;
            }

            ActionRotationStateTriple action = actions.get(new Random().nextInt(actions.size()));

            for (int i = 0; i < action.getRotation(); i++) {
                tile.rotate();
            }

            List<Byte> legalMeeples = stateSpace.legalMeeples(state, tile, action.getAction());

            if (!legalMeeples.isEmpty()) {
                int meeplePlacement = legalMeeples.get(new Random().nextInt(legalMeeples.size()));

                if (meeplePlacement > -1) {
                    tile.placeMeeple((byte) meeplePlacement, state.getPlayer(), state);
                }
            }

            state.updateBoard(action.getAction(), tile);
            state.checkForScoreAfterRound();
        }

        state.assignPointsAtEndOfGame();

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

            double uct = ((double) child.getQValue() / child.getVisits()) + c * ((2 * Math.log(parent.getVisits())) / child.getVisits());

            if (uct > highestValue) {
                highestValue = uct;
                bestChild = child;
            }
        }

        return bestChild;
    }

    private List<Node> getMeepleNodes(Node parent, GameStateSpace stateSpace) {
        List<ActionRotationStateTriple> actions = stateSpace.succ(parent.getState(), parent.getDrawnTile());
        List<Node> meepleNodes = new ArrayList<>();

        for (ActionRotationStateTriple action : actions) {

            Node meepleNode = new Node(parent.getState(), (byte) 1, parent.getState().getPlayer(), action.getAction(), parent.getDrawnTile(), action.getRotation());

            meepleNodes.add(meepleNode);
            //parent.addChild(meepleNode);
        }

        return meepleNodes;
    }

    private List<Node> getChanceNodes(Node parent, GameStateSpace stateSpace) {
        List<Byte> legalMeeplePlacements = stateSpace.legalMeeples(parent.getState(), parent.getDrawnTile(), parent.getMove());
        List<Node> chanceNodes = new ArrayList<>();

        if (parent.getState().getNumMeeples(parent.getPlayer()) > 0) {
            for (int legalMeeple : legalMeeplePlacements) {
                Node chanceNode = new Node(parent, (byte) 2);

                chanceNode.addMeeple((byte) legalMeeple);
                //chanceNode.getState().removeMeeple(parent.getPlayer());

                chanceNodes.add(chanceNode);
            }
        }

        chanceNodes.add(new Node(parent, (byte) 2));

        return chanceNodes;
    }

    private List<Node> getPlacementNodes(Node parent, GameStateSpace stateSpace, List<Tile> deck) {
        List<Node> placementNodes = new ArrayList<>();
        List<Integer> consideredTiles = new ArrayList<>();

        for (Tile tile : deck) {
            if (!consideredTiles.contains(tile.getType())) {
                consideredTiles.add(tile.getType());

                GameState newState = new GameState(parent.getState());

                Tile newTile = new Tile(parent.getDrawnTile());

                newTile.rotateBy(parent.getRotation());
                newTile.placeMeeple(parent.getMeeplePlacement(), parent.getPlayer(), newState);

                newState.updateBoard(parent.getMove(), newTile);

                Node placementNode = new Node(newState, (byte) 0, otherPlayer(parent.getPlayer()), new byte[]{-1, -1}, tile, (byte) 0);
                placementNodes.add(placementNode);
            }
        }

        return placementNodes;
    }

    private Node expand(Node node, GameStateSpace stateSpace, List<Tile> deck) {
        List<Node> children = new ArrayList<>();

        if (node.getType() == 0) {
            children = getMeepleNodes(node, stateSpace);
        } else if (node.getType() == 1) {
            children = getChanceNodes(node, stateSpace);
        } else if (node.getType() == 2) {
            children = getPlacementNodes(node, stateSpace, deck);
        } else {
            System.out.println("** Error in expand!!!");
        }

        if (children.isEmpty()) {
            return node;
        }

        node.addChildren(children);

        return node.getRandomChild();
    }

//    private Node expand(Node placementNode, GameStateSpace stateSpace) throws Exception {
//        List<Node> meepleNodes = getMeepleNodes(placementNode, stateSpace);
//        List<Node> placementNodes = new ArrayList<>();
//
//        for (int i = 0; i < meepleNodes.size(); i++) {
//            Node meepleNode = meepleNodes.get(i);
//            placementNode.addChild(meepleNode);
//            List<Node> chanceNodes = getChanceNodes(meepleNode, stateSpace);
//
//            for (int j = 0; j < chanceNodes.size(); j++) {
//                Node chanceNode = chanceNodes.get(j);
//                meepleNode.addChild(chanceNode);
//                placementNodes = getPlacementNodes(chanceNode, stateSpace);
//
//                for (int k = 0; k < placementNodes.size(); k++) {
//                    Node newPlacementNode = placementNodes.get(k);
//                    chanceNode.addChild(newPlacementNode);
//
//                    if (chanceNode.getState().getNumMeeples(1) < 0 || chanceNode.getState().getNumMeeples(2) < 0 ||
//                            chanceNode.getState().getNumMeeples(1) > 7 || chanceNode.getState().getNumMeeples(2) > 7) {
//                        ////logger.error("Illegal amount of meeples: {} {}", chanceNode.getState().getNumMeeples(1), chanceNode.getState().getNumMeeples(2));
//                    }
//                }
//            }
//        }
//
//        if (placementNodes.isEmpty()) {
//            return placementNode;
//        }
//
//        // return a random following placement node.
//        return placementNodes.get(new Random().nextInt(placementNodes.size()));
//    }

    private byte otherPlayer(int player) {
        return (byte) (player == 1 ? 2 : 1);
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
