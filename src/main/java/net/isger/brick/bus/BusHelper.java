package net.isger.brick.bus;

import net.isger.brick.core.CoreHelper;

public class BusHelper extends CoreHelper {

    protected BusHelper() {
    }

    public static Bus getPlugin() {
        return (Bus) CoreHelper.getGate();
    }

}
