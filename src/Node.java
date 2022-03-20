import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Node {
    private GameState state;
    private int qValue;
    private int visits;
    private Node parent;
    private List<Node> children;
    private Tile drawnTile;
    private int player;
    private Random random;
    private int[] move;
    private int meeplePlacement;
    private int rotation;

    /**
     * 0: Placement Node
     * 1: Meeple Node
     * 2: Chance Node
     */
    private int type;

    Logger logger = LoggerFactory.getLogger("NodeLogger");

    public Node(GameState state, int type, int player, int[] move, Tile tile, int rotation) {


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
        this.state = new GameState(node.state);
        this.parent = node.parent;
        this.qValue = node.qValue;
        this.visits = node.visits;
        this.children = new ArrayList<>(List.copyOf(node.children));
        this.drawnTile = node.drawnTile;
        this.player = node.player;
        this.random = node.random;
        this.move = node.getMove();
        this.drawnTile = node.drawnTile;
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

    public void addChild(Node child) throws Exception {
        if (children.contains(child)) {
            throw new Exception();
        }

        children.add(child);
        child.setParent(this);

//        if (child.parent != this) {
//
////            logger.error("Child initialized with wrong parent.");
////            throw new Exception();
//        }
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public int[] getMove() {
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
        return state.deckSize() == 0;
    }

    public Node getRandomChild() {
        if (!children.isEmpty()) {
            return children.get(random.nextInt(children.size()));
        } else {
            logger.error("No child available when querying for children!");
            return null;
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

    public boolean isPlacementNode() {
        return type == 0;
    }

    public void setDrawnTile(Tile tile) {
        drawnTile = tile;
    }
}
