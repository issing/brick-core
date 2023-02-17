package net.isger.brick.core;

public interface Airfone {

    public static final int ACTION_INITIAL = 0;

    public static final int ACTION_DESTROY = 1;

    public void ack(int action);

}
