package net.isger.brick.core;

import java.util.HashMap;
import java.util.Map;

import net.isger.brick.util.DesignLoader;
import net.isger.brick.util.DynamicOperator;
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
        operator = new DynamicOperator(this);
    }

    public String name() {
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
     * 获取目标对象
     * 
     * @return
     */
    public Class<?> getTargetClass() {
        return Reflects.getClass(getParameter(TARGET));
    }

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
