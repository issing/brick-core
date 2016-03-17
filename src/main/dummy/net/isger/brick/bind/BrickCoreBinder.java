package net.isger.brick.bind;

import net.isger.brick.inject.ContainerProvider;

public class BrickCoreBinder {

    private static final BrickCoreBinder BINDER;

    static {
        BINDER = new BrickCoreBinder();
    }

    private BrickCoreBinder() {
    }

    public static BrickCoreBinder getBinder() {
        return BINDER;
    }

    public ContainerProvider getProvider() {
        return new DummyProvider();
    }

    public String toString() {
        return "Dummy";
    }

}
