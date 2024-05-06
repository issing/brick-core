package net.isger.brick.core;

import java.util.HashMap;
import java.util.Map;

import net.isger.brick.util.CommandOperator;
import net.isger.brick.util.DesignLoader;
import net.isger.util.Asserts;
import net.isger.util.Callable;
import net.isger.util.Reflects;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

/**
 * 抽象模块
 * 
 * @author issing
 */
public abstract class AbstractModule extends DesignLoader implements Module {

    public static final String TARGET = "target";

    /** 模块操作器 */
    @Ignore(mode = Mode.INCLUDE, serialize = false)
    private CommandOperator operator;

    /** 模块参数 */
    @Ignore(mode = Mode.INCLUDE)
    private Map<String, Object> parameters;

    public AbstractModule() {
        this.operator = new CommandOperator(this);
        this.parameters = new HashMap<String, Object>();
    }

    /**
     * 获取参数
     * 
     * @param name
     * @return
     */
    protected Object getParameter(String name) {
        return this.parameters.get(name);
    }

    /**
     * 设置参数
     * 
     * @param name
     * @param value
     * @return
     */
    protected void setParameter(String name, Object value) {
        if (value == null) {
            this.parameters.remove(name);
        } else {
            this.parameters.put(name, value);
        }
    }

    /**
     * 获取目标类型
     */
    public Class<?> getTargetClass() {
        throw Asserts.state("The %s must override the method getTargetClass()", this.getClass());
    }

    /**
     * 获取实现类型
     * 
     * @return
     */
    public Class<?> getImplementClass() {
        return this.getImplementClass(TARGET);
    }

    /**
     * 获取实现类型
     * 
     * @param name
     * @return
     */
    protected final Class<?> getImplementClass(String name) {
        return this.getImplementClass(name, this.getBaseClass());
    }

    /**
     * 获取实现类型
     * 
     * @param name
     * @param baseClass
     * @return
     */
    protected final Class<?> getImplementClass(String name, Class<?> baseClass) {
        Class<?> implClass = Reflects.getClass(this.getParameter(name));
        if (implClass == null) {
            implClass = super.getImplementClass();
            if (implClass == this.getTargetClass()) {
                implClass = baseClass;
            }
        }
        return implClass;
    }

    /**
     * 获取基础实现
     * 
     * @return
     */
    protected abstract Class<?> getBaseClass();

    /**
     * 创建目标实例（默认不支持键值对以外配置方式）
     */
    protected Object create(Object res, Callable<?> assembler) {
        throw Asserts.argument("Unexpected config: %s", res);
    }

    /**
     * 创建默认实现
     *
     * @return
     */
    protected Object create() {
        return super.create(this.getImplementClass(), null, null);
    }

    /**
     * 设置内部参数（只限于命令会话生命周期内）
     *
     * @param key
     * @param value
     */
    protected void setInternal(String key, Object value) {
        ((InternalContext) Context.getAction()).setInternal(key, value);
    }

    /**
     * 获取内部参数（只限于命令会话生命周期内）
     *
     * @param key
     * @return
     */
    protected Object getInternal(String key) {
        return ((InternalContext) Context.getAction()).getInternal(key);
    }

    /**
     * 模块初始
     */
    public void initial() {
    }

    /**
     * 模块执行
     */
    public void execute(BaseCommand cmd) {
        this.operator.operate(cmd);
    }

    /**
     * 模块注销
     */
    public void destroy() {
        this.parameters.clear();
    }

}
