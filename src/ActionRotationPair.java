class ActionRotationPair {
    private final int[] action;
    private final int rotation;

    ActionRotationPair(int[] action, int rotation) {
        this.action = action;
        this.rotation = (int) rotation;
    }

    public int[] getAction() {
        return action;
    }

    public int getRotation() {
        return rotation;
    }
}