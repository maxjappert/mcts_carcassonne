class ActionRotationStateTriple {
    private int[] action;
    private int rotation;
    private GameState state;

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