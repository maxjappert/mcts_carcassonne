class ActionRotationStateTriple {
    private final int[] action;
    private final int rotation;
    private final GameState state;

    ActionRotationStateTriple(int[] action, int rotation, GameState state) {
        this.action = action;
        this.rotation = rotation;
        this.state = state;
    }

    public int[] getAction() {
        return action;
    }

    public int getRotation() {
        return rotation;
    }

    public GameState getState() {
        return state;
    }
}