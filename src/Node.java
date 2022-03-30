//import org.slf4j.//logger;
//import org.slf4j.//loggerFactory;

import java.util.*;

public class Node {
    private final GameState state;
    private int qValue;
    private int visits;
    private Node parent;
    private final List<Node> children;
    private final Tile drawnTile;
    private final int player;
    private final Random random;
    private final Coordinates move;
    private int meeplePlacement;
    private final int rotation;
    //long id = new Random().nextLong();

    /**
     * 0: Placement Node
     * 1: Meeple Node
     * 2: Chance Node
     */
    private final int type;

    ////logger //logger = //loggerFactory.get//logger("Node//logger");

    public Node(GameState state, int type, int player, Coordinates move, Tile tile, int rotation) {

        this.state = state;
        this.parent = null;
        this.type = type;
        this.player = player;
        this.drawnTile = tile;
        qValue = 0;
        visits = 0;
        children = new ArrayList<>();
        random = new Random();
        this.rotation = rotation;

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
        this.random = node.random;
        this.move = node.move;
        this.meeplePlacement = node.meeplePlacement;
        this.rotation = node.rotation;
        this.type = type;
    }

    public void addMeeple(int placement) {
        meeplePlacement = placement;
    }

    public int getRotation() {
        return rotation;
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

    public Coordinates getMove() {
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

    public Node getRandomChild() {
        if (!children.isEmpty()) {
            return children.get(random.nextInt(children.size()));
        } else {
            ////logger.error("No child available when querying for children!");
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

        if (visits > 30000) {
            ////logger.error("int doesn't suffice as a datatype for the visits!");
        }
    }

    public void updateQValue(int payoff) {
        qValue += payoff;

        if (qValue > 30000) {
            ////logger.error("int doesn't suffice as a datatype for the q value!");
        }

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
