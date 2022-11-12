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

    /**
     * 获取协议
     * 
     * @param name
     * @return
     */
    public Protocol getProtocol(String name);

    /**
     * 获取端点
     * 
     * @param name
     * @return
     */
    public Endpoint getEndpoint(String name);

}
