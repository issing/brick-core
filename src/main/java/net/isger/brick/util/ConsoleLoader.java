package net.isger.brick.util;

import net.isger.brick.Constants;
import net.isger.brick.core.Console;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;
import net.isger.util.load.BaseLoader;

/**
 * 控制台过滤器
 * 
 * @author issing
 *
 */
@Ignore
public class ConsoleLoader extends BaseLoader {

    /** 控制台 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    protected Console console;

    public ConsoleLoader() {
    }

    public ConsoleLoader(Class<?> targetClass) {
        super(targetClass);
    }

    protected Object load(String res) {
        if (console != null) {
            Object result = console.loadResource(res);
            if (result != null) {
                if (result instanceof String) {
                    result = super.load(result);
                } else {
                    result = load(result);
                }
                return result;
            }
        }
        return super.load(res);
    }

}
