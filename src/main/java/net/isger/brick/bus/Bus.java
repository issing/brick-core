package net.isger.brick.bus;

import net.isger.brick.bus.protocol.Protocol;
import net.isger.util.Manageable;

/**
 * 总线接口
 * 
 * @author issing
 *
 */
public interface Bus extends Manageable {

    public Protocol getProtocol(String name);

    public Endpoint getEndpoint(String name);

}
