package net.isger.brick.core;

import net.isger.util.Manageable;
import net.isger.util.load.Loader;

/**
 * 模块接口
 * 
 * @author issing
 * 
 */
public interface Module extends Loader, Manageable {

    /**
     * 执行
     * 
     * @param cmd
     */
    public void execute(BaseCommand cmd);

}
