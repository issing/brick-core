package net.isger.brick.util;

import java.util.Map;

import net.isger.brick.Constants;
import net.isger.brick.inject.Container;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;
import net.isger.util.config.Designer;
import net.isger.util.reflect.Converter;
import net.isger.util.reflect.conversion.Conversion;

/**
 * 抽象设计器
 * 
 * @author issing
 *
 */
public abstract class AbstractDesigner implements Designer {

    /** 预处理状态 */
    private transient boolean prepared;

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Container container;

    /**
     * 设计
     */
    public void design(Map<String, Object> config) {
        synchronized (this) {
            if (!this.prepared) {
                this.prepare();
                this.prepared = true;
            }
        }
    }

    /**
     * 预处理
     */
    protected abstract void prepare();

    /**
     * 添加转换器
     * 
     * @param conversion
     */
    protected final void addConversion(Conversion conversion) {
        if (!Converter.contains(conversion)) {
            this.container.inject(conversion);
            Converter.addConversion(conversion);
        }
    }

}
