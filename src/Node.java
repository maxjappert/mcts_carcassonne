//import org.slf4j.//logger;
//import org.slf4j.//loggerFactory;

import java.util.*;

public class Node {
    private final GameState state;
    private short qValue;
    private short visits;
    private Node parent;
    private final List<Node> children;
    private final Tile drawnTile;
    private final byte player;
    private final Random random;
    private final byte[] move;
    private byte meeplePlacement;
    private final byte rotation;

    /**
     * 0: Placement Node
     * 1: Meeple Node
     * 2: Chance Node
     */
    private final byte type;

    ////logger //logger = //loggerFactory.get//logger("Node//logger");

    public Node(GameState state, byte type, byte player, byte[] move, Tile tile, byte rotation) {


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
    public Node(Node node, byte type) {
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

    public void addMeeple(byte placement) {
        meeplePlacement = placement;
    }

    public byte getRotation() {
        return rotation;
    }

    public byte getMeeplePlacement() {
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

    public byte[] getMove() {
        return move;
    }

    public byte getPlayer() {
        return player;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public List<Node> getChildren() {
        return children;
    }

    public short getQValue() {
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
            return this;
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
            ////logger.error("Short doesn't suffice as a datatype for the visits!");
        }
    }

    public void updateQValue(int payoff) {
        qValue += payoff;

        if (qValue > 30000) {
            ////logger.error("Short doesn't suffice as a datatype for the q value!");
        }

    }

    public Node getParent() {
        return parent;
    }

    public short getVisits() {
        return visits;
    }

    public Tile getDrawnTile() {
        return drawnTile;
    }

    public byte getType() {
        return type;
    }
}
