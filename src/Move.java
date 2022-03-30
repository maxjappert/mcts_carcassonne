class Move {
    private final Coordinates coords;
    private final int rotation;

    Move(Coordinates coords, int rotation) {
        this.coords = coords;
        this.rotation = (int) rotation;
    }

    public Coordinates getCoords() {
        return coords;
    }

    public int getRotation() {
        return rotation;
    }
}