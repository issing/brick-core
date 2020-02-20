package net.isger.brick.util;

import java.util.HashMap;
import java.util.Map;

import net.isger.brick.Constants;
import net.isger.brick.inject.Container;
import net.isger.util.Reflects;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;
import net.isger.util.config.Designer;
import net.isger.util.reflect.ClassAssembler;

/**
 * 设计加载器
 * 
 * @author issing
 *
 */
@Ignore
public class DesignLoader extends ConsoleLoader {

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    protected Container container;

    /** 模块设计器 */
    private Map<String, Designer> designers;

    public DesignLoader() {
        this(null);
    }

    public DesignLoader(Class<?> targetClass) {
        super(targetClass);
        designers = new HashMap<String, Designer>();
    }

    protected Object create(Class<?> clazz, Map<String, Object> res, ClassAssembler assembler) {
        // 设计配置项
        Designer designer = getDesigner(clazz, assembler);
        if (designer != null) {
            designer.design(res);
        }
        // 创建实例
        return super.create(clazz, res, assembler);
    }

    /**
     * 目标设计器
     * 
     * @param rawClass
     * @return
     */
    protected Designer getDesigner(Class<?> rawClass, ClassAssembler assembler) {
        String name = rawClass.getName() + "Designer";
        Designer designer;
        synchronized (designers) {
            designer = designers.get(name);
            if (!designers.containsKey(name)) {
                newDesigner: {
                    try {
                        designer = (Designer) Reflects.newInstance(name, assembler);
                        if (designer != null) {
                            container.inject(designer);
                            break newDesigner;
                        }
                    } catch (Exception e) {
                    }
                    if (rawClass != getTargetClass()) {
                        designer = getDesigner(getTargetClass(), assembler);
                    }
                }
                designers.put(name, designer);
            }
        }
        return designer;
    }

}
