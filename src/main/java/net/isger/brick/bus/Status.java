package net.isger.brick.bus;

public enum Status {

    ACTIVATED(0), DEACTIVATED(1);

    public static final int COUNT = 2;

    public final int value;

    private Status(int value) {
        this.value = value;
    }

}
