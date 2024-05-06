package net.isger.brick.bus;

import net.isger.brick.core.Handler;

/**
 * 总线载荷
 * 
 * @author issing
 */
public interface Payload {

    public String getTopic();

    public Handler getHandler();

}
