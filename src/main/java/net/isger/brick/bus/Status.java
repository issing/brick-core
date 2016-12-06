package net.isger.brick.bus;

public enum Status {

    INACTIVATE(0), ACTIVATED(1), DEACTIVATED(2);

    public static final int COUNT = 3;

    public final int value;

    private Status(int value) {
        this.value = value;
    }

}
