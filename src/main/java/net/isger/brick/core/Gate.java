package net.isger.brick.core;

import net.isger.util.Manageable;

/**
 * 关卡接口
 * 
 * @author issing
 *
 */
public interface Gate extends Manageable {

    public static final String KEY_GATE = "brick.core.gate";

    public void operate(GateCommand cmd);

}
