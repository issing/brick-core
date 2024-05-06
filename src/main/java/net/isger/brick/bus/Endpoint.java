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

    public static final String KEY_PROTOCOL = "protocol";

    public static final String PARAM_MESSAGE = "message";

    public void operate(BusCommand cmd);

}
