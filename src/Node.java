import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Node {
    private GameState state;
    private int[] qValues;
    private int visits;
    private Node parent;
    private TreeSet<Node> children;
    private Tile drawnTile;
    private int player;
    private Random random;

//    private Comparator<Node> comparator = new Comparator<Node>() {
//        @Override
//        public int compare(Node n1, Node n2) {
//
//            uct1 =
//
//            return n1.qValues[player] - n2.qValues[player];
//        }
//    };

    /**
     * 0: Placement Node
     * 1: Meeple Node
     * 2: Chance Node
     */
    private int type;

    Logger logger = LoggerFactory.getLogger("NodeLogger");

    public Node(GameState state, Node parent, int type, int player) {


        this.state = state;
        this.parent = parent;
        this.type = type;
        this.player = player;
        qValues = new int[]{0, 0};
        visits = Integer.MAX_VALUE;
        children = new TreeSet<>();
        random = new Random();
    }

    /**
     * Copy constructor.
     */
    public Node(Node node) {
        this.state = new GameState(node.state);
        this.parent = node.parent;
        this.qValues = node.qValues;
        this.visits = node.visits;
        this.children = (TreeSet<Node>) Set.copyOf(children);
        this.drawnTile = node.drawnTile;
        this.player = node.player;
        this.random = node.random;
    }

//    public void expand(GameStateSpace stateSpace) {
//        if (drawnTile == null) {
//            logger.error("A node was expanded without it having a tile assigned.");
//        } else if (type != 0) {
//            logger.error("A wrong type of node was expanded.");
//        }
//
//        //List<ActionRotationStateTriple> ars = stateSpace.succ(state, drawnTile, )
//    }

    public void addChild(Node child) {
        children.add(child);
    }


    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public TreeSet<Node> getChildren() {
        return children;
    }

    public int getQValue(int player) {
        return qValues[player-1];
    }

    public boolean isChanceNode() {
        return type == 2;
    }

    public boolean isTerminal() {
        return state.deckSize() == 0;
    }

    public Node getRandomChild() {
        return children.stream().toList().get(random.nextInt(children.size()));
    }

    public GameState getState() {
        return state;
    }

    public void updateVisits() {
        visits++;
    }

    public void updateQValue(int payoff) {
        qValues[player-1] += payoff;
    }

    public Node getParent() {
        return parent;
    }

    public int getVisits() {
        return visits;
    }

    public Tile getDrawnTile() {
        return drawnTile;
    }

    public int getType() {
        return type;
    }

    public boolean isPlacementNode() {
        return type == 0;
    }

    public void setDrawnTile(Tile tile) {
        drawnTile = tile;
    }
}
