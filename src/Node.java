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
    //private final Random random;
    private final Move move;
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
