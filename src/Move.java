class Move {
    private final Coordinates coords;
    private final int rotation;

    Move(Coordinates coords, int rotation) {
        this.coords = coords;
        this.rotation = rotation;
    }

    public Coordinates getCoords() {
        return coords;
    }

    public int getRotation() {
        return rotation;
    }

    public boolean isEqualTo(Move move) {
        return coords.isEqualTo(move.coords) && rotation == move.getRotation();
    }
}