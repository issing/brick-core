package net.isger.brick.bus;

import net.isger.util.Manageable;
import net.isger.util.Named;

public interface Endpoint extends Manageable, Named {

    public static final String BRICK_ENDPOINT = "brick.core.endpoint";

    public Status getStatus();

    public void operate(BusCommand cmd);

}
