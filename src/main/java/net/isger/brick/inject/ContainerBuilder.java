package net.isger.brick.inject;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.brick.Constants;
import net.isger.brick.util.Assemblers;
import net.isger.util.Asserts;
import net.isger.util.Callable;
import net.isger.util.Reflects;

/**
 * 容器构建器
 * 
 * @author issing
 */
public final class ContainerBuilder {

    private static final Logger LOG;

    private final Map<Key<?>, InternalFactory<?>> factories;

    private boolean duplicated;

    static {
        LOG = LoggerFactory.getLogger(ContainerBuilder.class);
    }

    public ContainerBuilder() {
        this.factories = new HashMap<Key<?>, InternalFactory<?>>();
        this.duplicated = true;
    }

    /**
     * 重复状态
     * 
     * @param duplicates
     */
    public void setDuplicates(boolean duplicates) {
        this.duplicated = duplicates;
    }

    /**
     * 实例工厂
     * 
     * @param <T>
     * @param type
     * @param callable
     * @return
     */
    public <T> ContainerBuilder factory(Class<T> type, Callable<T> callable) {
        return factory(type, Constants.DEFAULT, callable, null);
    }

    /**
     * 实例工厂
     * 
     * @param <T>
     * @param type
     * @param callable
     * @param scope
     * @return
     */
    public <T> ContainerBuilder factory(Class<T> type, Callable<T> callable, Scope scope) {
        return factory(type, Constants.DEFAULT, callable, scope);
    }

    /**
     * 实例工厂
     * 
     * @param <T>
     * @param type
     * @param name
     * @param callable
     * @return
     */
    public <T> ContainerBuilder factory(Class<T> type, String name, Callable<T> callable) {
        return factory(type, name, callable, null);
    }

    /**
     * 实例工厂
     * 
     * @param <T>
     * @param type
     * @param name
     * @param callable
     * @param scope
     * @return
     */
    public <T> ContainerBuilder factory(Class<T> type, String name, final Callable<T> callable, Scope scope) {
        Asserts.isNotNull(callable);
        /* 构造默认实例内部工厂类，并定义作用域 */
        return factory(Key.newInstance(type, name), new InternalFactory<T>() {
            public boolean hasInject(InternalContext context, T instance) {
                return context.instances.contains(instance);
            }

            public T create(InternalContext context) {
                return callable.call(context.container);
            }
        }, scope == null ? Scope.get(type) : scope);
    }

    /**
     * 实例工厂
     * 
     * @param <T>
     * @param type
     * @return
     */
    public <T> ContainerBuilder factory(Class<T> type) {
        return factory(type, Constants.DEFAULT, type, null);
    }

    /**
     * 实例工厂
     * 
     * @param <T>
     * @param type
     * @param scope
     * @return
     */
    public <T> ContainerBuilder factory(Class<T> type, Scope scope) {
        return factory(type, Constants.DEFAULT, type, scope);
    }

    /**
     * 实例工厂
     * 
     * @param <T>
     * @param type
     * @param implementation
     * @return
     */
    public <T> ContainerBuilder factory(Class<T> type, Class<? extends T> implementation) {
        return factory(type, Constants.DEFAULT, implementation, null);
    }

    /**
     * 实例工厂
     * 
     * @param <T>
     * @param type
     * @param implementation
     * @param scope
     * @return
     */
    public <T> ContainerBuilder factory(Class<T> type, Class<? extends T> implementation, Scope scope) {
        return factory(type, Constants.DEFAULT, implementation, scope);
    }

    /**
     * 实例工厂
     * 
     * @param <T>
     * @param type
     * @param name
     * @return
     */
    public <T> ContainerBuilder factory(Class<T> type, String name) {
        return factory(type, name, type, null);
    }

    /**
     * 实例工厂
     * 
     * @param <T>
     * @param type
     * @param name
     * @param implementation
     * @return
     */
    public <T> ContainerBuilder factory(Class<T> type, String name, Class<? extends T> implementation) {
        return factory(type, name, implementation, null);
    }

    /**
     * 实例工厂
     * 
     * @param <T>
     * @param type
     * @param name
     * @param scope
     * @return
     */
    public <T> ContainerBuilder factory(Class<T> type, String name, Scope scope) {
        return factory(type, name, type, scope);
    }

    /**
     * 实例工厂
     * 
     * @param <T>
     * 
     * @param type
     * @param name
     * @param implementation
     * @param scope
     * @return
     */
    public <T> ContainerBuilder factory(Class<T> type, String name, final Class<? extends T> implementation, Scope scope) {
        Asserts.isAssignable(type, implementation);
        /* 构造默认实例内部工厂类，并定义作用域 */
        return factory(Key.newInstance(type, name), new InternalFactory<T>() {
            public boolean hasInject(InternalContext context, T instance) {
                return context.instances.contains(instance);
            }

            public T create(InternalContext context) {
                return Reflects.newInstance(implementation, Assemblers.createAssembler());
            }
        }, scope == null ? Scope.get(implementation) : scope);
    }

    /**
     * 常量工厂
     * 
     * @param name
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
    public ContainerBuilder constant(String name, Object value) {
        return constant((Class<Object>) value.getClass(), name, value);
    }

    /**
     * 常量工厂
     * 
     * @param type
     * @param name
     * @param value
     * @return
     */
    public <T> ContainerBuilder constant(Class<T> type, String name, final T value) {
        return factory(Key.newInstance(type, name), new InternalFactory<T>() {
            public boolean hasInject(InternalContext context, T instance) {
                return context.instances.contains(instance);
            }

            public T create(InternalContext context) {
                return value;
            }
        }, Scope.SINGLETON);
    }

    /**
     * 实例工厂
     * 
     * @param key
     * @param factory
     * @param scope
     * @return
     */
    private <T> ContainerBuilder factory(Key<T> key, InternalFactory<T> factory, Scope scope) {
        /* 检测重复 */
        if (this.factories.containsKey(key)) {
            Asserts.throwState(duplicated, "Dependency mapping for [%s] already exists", key);
            LOG.warn("(!) Dependency mapping for [{}] already exists", key);
        }
        /* 划分作用域 */
        this.factories.put(key, scope.factory(key.getType(), key.getName(), factory));
        return this;
    }

    /**
     * 创建容器
     * 
     * @return
     */
    public Container create(String name) {
        return new InternalContainer(name, this.factories);
    }

}
