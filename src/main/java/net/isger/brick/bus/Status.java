package net.isger.brick.bus;

public enum Status {

    INACTIVATE(0), ACTIVATING(1), ACTIVATED(2), DEACTIVATED(3);

    public static final int COUNT = 4;

    public final int value;

    private Status(int value) {
        this.value = value;
    }

}
