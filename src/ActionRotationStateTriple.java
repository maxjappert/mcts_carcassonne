class ActionRotationStateTriple {
    private final byte[] action;
    private final byte rotation;
    private final GameState state;

    ActionRotationStateTriple(byte[] action, int rotation, GameState state) {
        this.action = action;
        this.rotation = (byte) rotation;
        this.state = state;
    }

    public byte[] getAction() {
        return action;
    }

    public byte getRotation() {
        return rotation;
    }

    public GameState getState() {
        return state;
    }
}