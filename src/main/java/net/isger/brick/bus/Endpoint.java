package net.isger.brick.bus;

import net.isger.util.Manageable;
import net.isger.util.Named;

/**
 * 端点接口
 * 
 * @author issing
 */
public interface Endpoint extends Manageable, Named {

    public static final String BRICK_ENDPOINT = "brick.core.endpoint";

    public Status getStatus();

    public boolean isActive();

    public void operate(BusCommand cmd);

}
