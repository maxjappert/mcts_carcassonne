import java.util.*;

public class Node {

    /**
     *  The state which this node represents.
     */
    private final GameState state;

    /**
     *  The total accumulated payoff achieved while passing through this node during the tree policy..
     */
    private final float[] qValue;

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

    public Node(GameState state, int type, Move move, Tile tile) {

        this.state = state;
        this.parent = null;
        this.type = type;
        this.drawnTile = tile;
        qValue = new float[]{0, 0};
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
        this.qValue = new float[]{0, 0};
        this.visits = 0;
        this.children = new ArrayList<>();
        this.drawnTile = node.drawnTile;
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
            Engine.printError(" Duplicate child added.");
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

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public List<Node> getChildren() {
        return children;
    }

    public float[] getQValue() {
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

    public void updateQValue(int[] score) {
        qValue[0] += score[0];
        qValue[1] += score[1];
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
