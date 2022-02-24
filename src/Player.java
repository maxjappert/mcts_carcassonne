import java.util.List;

abstract class Player {
    protected Tile drawnTile;

    public void draw(List<Tile> deck) {
        drawnTile = deck.remove(0);
    }

    abstract String decideOnNextMove() throws Exception;
}
