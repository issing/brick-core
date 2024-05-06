package net.isger.brick.bus;

import net.isger.brick.core.Handler;

/**
 * 总线载荷适配器
 * 
 * @author issing
 */
public abstract class PayloadAdapter implements Payload {

    public Handler getHandler() {
        return Handler.NOP;
    }

}
