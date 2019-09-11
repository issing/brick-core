package net.isger.brick.bus;

import net.isger.brick.core.CoreHelper;

public class BusHelper extends CoreHelper {

    protected BusHelper() {
    }

    public static Bus getBus() {
        return ((BusModule) getModule()).getBus();
    }

    public static Endpoint getEndpoint() {
        return ((BusModule) getModule()).getEndpoint();
    }

}
