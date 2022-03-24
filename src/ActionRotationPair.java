class ActionRotationPair {
    private final byte[] action;
    private final byte rotation;

    ActionRotationPair(byte[] action, int rotation) {
        this.action = action;
        this.rotation = (byte) rotation;
    }

    public byte[] getAction() {
        return action;
    }

    public byte getRotation() {
        return rotation;
    }
}