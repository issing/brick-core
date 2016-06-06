package net.isger.brick.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.isger.brick.util.DesignLoader;
import net.isger.brick.util.CommandOperator;
import net.isger.util.Helpers;
import net.isger.util.Operator;
import net.isger.util.Reflects;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

/**
 * 抽象模块
 * 
 * @author issing
 * 
 */
@Ignore
public abstract class AbstractModule extends DesignLoader implements Module {

    public static final String TARGET = "target";

    /** 模块参数 */
    @Ignore(mode = Mode.INCLUDE)
    private Map<String, Object> parameters;

    private Operator operator;

    public AbstractModule() {
        parameters = new HashMap<String, Object>();
        operator = new CommandOperator(this);
    }

    public String name() {
        for (Entry<String, Module> entry : console.getModules().entrySet()) {
            if (entry.getValue() == this) {
                return entry.getKey();
            }
        }
        return Helpers.getAliasName(this.getClass(), "Module$");
    }

    /**
     * 获取参数
     * 
     * @param name
     * @return
     */
    protected Object getParameter(String name) {
        return parameters.get(name);
    }

    /**
     * 设置参数
     * 
     * @param name
     * @param value
     * @return
     */
    protected void setParameter(String name, Object value) {
        parameters.put(name, value);
    }

    /**
     * 获取目标类型
     */
    public Class<?> getTargetClass() {
        throw new IllegalStateException("The " + this.getClass()
                + " must override the method getTargetClass()");
    }

    /**
     * 获取实现类型
     * 
     * @return
     */
    public Class<?> getImplementClass() {
        return getImplementClass(TARGET);
    }

    /**
     * 获取实现类型
     * 
     * @param name
     * @return
     */
    protected final Class<?> getImplementClass(String name) {
        return getImplementClass(name, getBaseClass());
    }

    /**
     * 获取实现类型
     * 
     * @param name
     * @param baseClass
     * @return
     */
    protected final Class<?> getImplementClass(String name, Class<?> baseClass) {
        Class<?> implClass = Reflects.getClass(getParameter(name));
        if (implClass == null) {
            implClass = super.getImplementClass();
            if (implClass == getTargetClass()) {
                implClass = baseClass;
            }
        }
        return implClass;
    }

    /**
     * 获取默认实现
     * 
     * @return
     */
    protected abstract Class<?> getBaseClass();

    /**
     * 模块操作
     */
    protected void operate() {
        operator.operate();
    }

    protected void setInternal(String key, Object value) {
        ((InternalContext) Context.getAction()).setInternal(key, value);
    }

    protected Object getInternal(String key) {
        return ((InternalContext) Context.getAction()).getInternal(key);
    }

    /**
     * 模块初始
     */
    public void initial() {
    }

    /**
     * 模块注销
     */
    public void destroy() {
        parameters.clear();
    }

}
