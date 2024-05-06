package net.isger.brick.util;

import net.isger.brick.Constants;
import net.isger.brick.core.Console;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;
import net.isger.util.load.BaseLoader;
import net.isger.util.reflect.ClassAssembler;

/**
 * 控制台过滤器
 * 
 * @author issing
 *
 */
@Ignore
public class ConsoleLoader extends BaseLoader {

    /** 控制台 */
    @Alias(Constants.SYSTEM)
    @Ignore(mode = Mode.INCLUDE, serialize = false)
    protected Console console;

    public ConsoleLoader() {
    }

    public ConsoleLoader(Class<?> targetClass) {
        super(targetClass);
    }

    protected Object load(String res, ClassAssembler assembler) {
        if (this.console != null) {
            Object result = this.console.loadResource(res);
            if (result != null) {
                if (result instanceof String) {
                    result = super.load(result, assembler);
                } else {
                    result = this.load(result, assembler);
                }
                return result;
            }
        }
        return super.load(res, assembler);
    }

}
