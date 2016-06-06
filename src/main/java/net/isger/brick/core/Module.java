package net.isger.brick.core;

import net.isger.util.Manageable;
import net.isger.util.Named;
import net.isger.util.load.Loader;

/**
 * 模块接口
 * 
 * @author issing
 * 
 */
public interface Module extends Loader, Manageable, Named {

    public static final String KEY_MODULE = "brick.core.module";

    /**
     * 执行
     * 
     */
    public void execute();

}
