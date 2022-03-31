public class Coordinates {
    public int x;
    public int y;

    public Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isEqualTo(Coordinates coords) {
        return x == coords.x && y == coords.y;
    }
}
