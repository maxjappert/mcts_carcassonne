import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Node {
    private GameState state;
    private int[] qValues;
    private int visits;
    private Node parent;
    private List<Node> children;
    private Tile drawnTile;
    private int player;
    private Random random;
    private int[] move;

    /**
     * 0: Placement Node
     * 1: Meeple Node
     * 2: Chance Node
     */
    private int type;

    Logger logger = LoggerFactory.getLogger("NodeLogger");

    public Node(GameState state, Node parent, int type, int player, int[] move, Tile tile) {


        this.state = state;
        this.parent = parent;
        this.type = type;
        this.player = player;
        this.drawnTile = tile;
        qValues = new int[]{0, 0};
        visits = 0;
        children = new ArrayList<>();
        random = new Random();

        this.move = move;
    }

    /**
     * Copy constructor.
     */
    public Node(Node node) {
        this.state = new GameState(node.state);
        this.parent = node.parent;
        this.qValues = node.qValues;
        this.visits = node.visits;
        this.children = List.copyOf(node.children);
        this.drawnTile = node.drawnTile;
        this.player = node.player;
        this.random = node.random;
        this.move = node.getMove();
        this.drawnTile = node.drawnTile;
    }

    public void addChild(Node child) {
        children.add(child);

        if (child.parent != this) {
            logger.error("Child initialized with wrong parent.");
        }
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public int[] getMove() {
        return move;
    }


    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public List<Node> getChildren() {
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
