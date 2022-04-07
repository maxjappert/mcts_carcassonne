//import org.slf4j.//logger;
//import org.slf4j.//loggerFactory;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

public class Node {

    /**
     *  The state which this node represents.
     */
    private final GameState state;

    /**
     *  The total accumulated payoff achieved while passing through this node during the tree policy..
     */
    private int qValue;

    /**
     *  The number of times this node was visited during the tree policy.
     */
    private int visits;

    /**
     *  A reference to the parent node. null if this is a root node.
     */
    private Node parent;

    /**
     *  A list of references to the children of this node. Empty list if this is unexpanded.
     */
    private final List<Node> children;

    /**
     *  The drawn tile for which a move needs to be decided.
     */
    private final Tile drawnTile;

    // TODO: delete this!
    private final int player;

    /**
     *  The move which this node represents. null if this is a placement node.
     */
    private final Move move;

    /**
     *  The meeple placement which this node represents. null if this is not a chance node.
     */
    private int meeplePlacement;
    //long id = new Random().nextLong();

    /**
     * 0: Placement Node
     * 1: Meeple Node
     * 2: Chance Node
     */
    private final int type;

    public Node(GameState state, int type, int player, Move move, Tile tile) {

        this.state = state;
        this.parent = null;
        this.type = type;
        this.player = player;
        this.drawnTile = tile;
        qValue = 0;
        visits = 0;
        children = new ArrayList<>();

        this.move = move;
        this.meeplePlacement = -1;
    }

    /**
     * Copy constructor.
     */
    public Node(Node node, int type) {
        this.state = node.state;
        this.parent = node;
        this.qValue = 0;
        this.visits = 0;
        this.children = new ArrayList<>();
        this.drawnTile = node.drawnTile;
        this.player = node.player;
        this.move = node.move;
        this.meeplePlacement = node.meeplePlacement;
        this.type = type;
    }

    public void addMeeple(int placement) {
        meeplePlacement = placement;
    }

    public int getRotation() {
        return move.getRotation();
    }

    public int getMeeplePlacement() {
        return meeplePlacement;
    }

    public void addChild(Node child) {

        if (children.contains(child)) {
            System.out.println("** Duplicate child added.");
            return;
        }

        children.add(child);
        child.setParent(this);
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Coordinates getCoords() {
        return move.getCoords();
    }

    public Move getMove() {
        return move;
    }

    public int getPlayer() {
        return player;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public List<Node> getChildren() {
        return children;
    }

    public int getQValue() {
        return qValue;
    }

    public boolean isChanceNode() {
        return type == 2;
    }

    public boolean isTerminal() {
        return state.getDeckSize() == 1;
    }

    public Node getRandomChild(Random random) {
        if (!children.isEmpty()) {
            int index = random.nextInt(children.size());
            return children.get(index);
        } else {
            return null;
        }
    }

    public void addChildren(List<Node> newChildren) {
        for (Node child : newChildren) {
            addChild(child);
        }
    }

    public GameState getState() {
        return state;
    }

    public void updateVisits() {
        visits++;
    }

    public void updateQValue(int payoff) {
        qValue += payoff;
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
}
