package net.isger.brick.inject;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import net.isger.brick.Constants;
import net.isger.brick.util.anno.Collect;
import net.isger.brick.util.anno.Digest;
import net.isger.brick.util.anno.Digest.Stage;
import net.isger.util.Asserts;
import net.isger.util.Callable;
import net.isger.util.Helpers;
import net.isger.util.Reflects;
import net.isger.util.Strings;
import net.isger.util.reflect.BoundField;
import net.isger.util.reflect.BoundMethod;

/**
 * 内部容器
 * 
 * @author issing
 */
class InternalContainer implements Container {

    /** 初始化状态 */
    private transient volatile Status status;

    /** 容器构建上下文 */
    private volatile ThreadLocal<InternalContext[]> context;

    /** 容器名称 */
    final String name;

    /** 实例工厂集合 */
    final Map<Key<?>, InternalFactory<?>> factories;

    /** 实例策略集合 */
    final Map<Key<?>, Strategy<?>> strategies;

    /** 注入实例后备 */
    transient InjectReserver reserver;

    InternalContainer(String name, Map<Key<?>, InternalFactory<?>> factories) {
        this.context = new ThreadLocal<InternalContext[]>() {
            protected InternalContext[] initialValue() {
                return new InternalContext[1];
            }
        };
        this.name = name;
        this.factories = new ConcurrentHashMap<Key<?>, InternalFactory<?>>(factories);
        this.strategies = new ConcurrentHashMap<Key<?>, Strategy<?>>();
        this.reserver = new InjectReserver() {
            public boolean contains(Key<?> key) {
                return false;
            }

            public <T> T alternate(Key<T> key) {
                return null;
            }
        };
        this.status = Status.UNINITIALIZED;
    }

    public final boolean hasReady() {
        return this.status == Status.INITIALIZED;
    }

    public final Status getStatus() {
        return this.status;
    }

    public synchronized void initial() {
        if (!(status == Status.UNINITIALIZED || status == Status.DESTROYED)) return;
        this.status = Status.INITIALIZING;
        /* 托管容器自身 */
        this.factories.put(Key.newInstance(Container.class, this.name), new InternalFactory<Container>() {
            public boolean hasInject(InternalContext context, Container instance) {
                return true;
            }

            public Container create(InternalContext context) {
                return InternalContainer.this;
            }
        });
        /* 添加注入后备器（第三方注入容器扩展接口） */
        for (InjectReserver reserver : Helpers.sort(new ArrayList<InjectReserver>(this.getInstances(InjectReserver.class).values()))) {
            this.reserver = InjectMulticaster.addInternal(this.reserver, reserver);
        }
        this.status = Status.INITIALIZED;
    }

    public boolean contains(Class<?> type) {
        return this.contains(type, Constants.DEFAULT);
    }

    public boolean contains(Class<?> type, String name) {
        boolean result;
        contains: if (!(result = this.contains(Key.newInstance(type, name)))) {
            // 检索父类
            Class<?> superClass = type.getSuperclass();
            while (superClass != null && superClass != Object.class) {
                if (this.contains(Key.newInstance(superClass, name))) {
                    result = true;
                    break contains;
                }
                superClass = superClass.getSuperclass();
            }
            // 检索接口
            Class<?>[] interfaceClasses = Reflects.getInterfaces(type);
            for (Class<?> interfaceClass : interfaceClasses) {
                if (this.contains(Key.newInstance(interfaceClass, name))) {
                    result = true;
                    break contains;
                }
            }
            // 检索子类
            result = this.getInstances(type).containsKey(name);
        }
        return result;
    }

    private boolean contains(Key<?> key) {
        return this.factories.containsKey(key) || this.strategies.containsKey(key) || this.reserver.contains(key);
    }

    public <T> Strategy<T> getStrategy(Class<T> type) {
        return this.getStrategy(type, Constants.DEFAULT);
    }

    @SuppressWarnings("unchecked")
    public <T> Strategy<T> getStrategy(Class<T> type, String name) {
        return (Strategy<T>) this.strategies.get(Key.newInstance(type, name));
    }

    public <T> Strategy<T> setStrategy(Class<T> type, Strategy<T> strategy) {
        return this.setStrategy(type, Constants.DEFAULT, strategy);
    }

    @SuppressWarnings("unchecked")
    public <T> Strategy<T> setStrategy(Class<T> type, String name, Strategy<T> strategy) {
        Key<T> key = Key.newInstance(type, name);
        return (Strategy<T>) (strategy == null ? this.strategies.remove(key) : this.strategies.put(key, strategy));
    }

    public <T> T getInstance(Class<T> type) {
        return this.getInstance(type, Constants.DEFAULT);
    }

    public <T> T getInstance(Class<T> type, String name) {
        return this.getInstance(type, name, null);
    }

    public <T> T getInstance(final Class<T> type, final String name, InjectConductor conductor) {
        return call(new Callable<T>() {
            @SuppressWarnings("unchecked")
            public T call(Object... args) {
                Object instance = getInstance(Key.newInstance(type, name), (InternalContext) args[0]);
                find: if (instance == null) {
                    // 检索父类
                    Class<?> superclass = type.getSuperclass();
                    while (superclass != null) {
                        instance = getInstance(Key.newInstance(superclass, name), (InternalContext) args[0]);
                        if (instance != null && type.isInstance(instance)) {
                            break find;
                        }
                        superclass = superclass.getSuperclass();
                    }
                    // 检索接口
                    Class<?>[] interfaces = Reflects.getInterfaces(type);
                    for (Class<?> interfaceClass : interfaces) {
                        instance = getInstance(Key.newInstance(interfaceClass, name), (InternalContext) args[0]);
                        if (instance != null && type.isInstance(instance)) {
                            break find;
                        }
                    }
                    // 检索子类
                    if (!(type == Object.class && Constants.DEFAULT.equals(name))) {
                        instance = getInstances(type).get(name); // 该过程可能导致同类型其它实例被提前注入（如：启动阶段“brick”环境参数还未完善）
                    }
                }
                return (T) instance;
            }
        }, conductor);
    }

    @SuppressWarnings("unchecked")
    private <T> T getInstance(final Key<T> key, final InternalContext context) {
        T instance = null;
        /* 工厂模式创建对象 */
        if (this.factories.containsKey(key)) {
            InternalFactory<T> factory = (InternalFactory<T>) this.factories.get(key);
            instance = factory.create(context);
            if (factory.hasInject(context, instance) && !context.hasInject(instance)) {
                context.instances.add(instance);
            }
        }
        /* 策略模式查找对象 */
        else if (this.strategies.containsKey(key)) {
            Strategy<T> strategy = (Strategy<T>) this.strategies.get(key);
            instance = strategy.find(this, key.getType(), key.getName());
            if (strategy.hasInject(instance) && !context.hasInject(instance)) {
                context.instances.add(instance);
            }
        }
        /* 备选方案 */
        if (instance == null) {
            instance = this.reserver.alternate(key);
        }
        /* 注入对象 */
        inject(key, instance, context);
        return instance;
    }

    public <T> Map<String, T> getInstances(Class<T> type) {
        return getInstances(type, (InjectConductor) null);
    }

    public <T> Map<String, T> getInstances(final Class<T> type, InjectConductor conductor) {
        return call(new Callable<Map<String, T>>() {
            public Map<String, T> call(Object... args) {
                return getInstances(type, (InternalContext) args[0]);
            }
        }, conductor);
    }

    @SuppressWarnings("unchecked")
    private <T> Map<String, T> getInstances(Class<T> type, InternalContext context) {
        Map<String, T> instances = new HashMap<String, T>();
        T instance;
        // 获取策略模式对象
        Strategy<T> strategy;
        Key<?> key;
        for (Entry<Key<?>, Strategy<?>> entry : this.strategies.entrySet()) {
            // 允许实例向上赋值
            key = entry.getKey();
            if (type.isAssignableFrom(key.type) && ((strategy = (Strategy<T>) entry.getValue()) != null)) {
                try {
                    instance = strategy.find(this, (Class<T>) key.type, key.name);
                    if (instance == null) continue;
                    if (strategy.hasInject(instance)) {
                        if (!context.hasInject(instance)) context.instances.add(instance);
                    } else {
                        this.inject(key, instance, context);
                    }
                    instances.put(key.getName(), instance);
                } catch (Exception e) {
                    throw (e instanceof RuntimeException) ? (RuntimeException) e : Asserts.state(e.getMessage(), e.getCause());
                }
            }
        }
        // 获取工厂注册对象
        InternalFactory<T> factory;
        for (Entry<Key<?>, InternalFactory<?>> entry : factories.entrySet()) {
            // 允许实例向上赋值
            key = entry.getKey();
            if (type.isAssignableFrom(key.type) && (factory = (InternalFactory<T>) entry.getValue()) != null) {
                try {
                    instance = factory.create(context);
                    if (instance == null) continue;
                    if (factory.hasInject(context, instance)) {
                        if (!context.hasInject(instance)) context.instances.add(instance);
                    } else {
                        this.inject(key, instance, context);
                    }
                    instances.put(key.getName(), instance);
                } catch (Exception e) {
                    throw (e instanceof RuntimeException) ? (RuntimeException) e : Asserts.state(e.getMessage(), e.getCause());
                }
            }
        }
        return instances;
    }

    public <T> T inject(final T instance) {
        return call(new Callable<T>() {
            public T call(Object... args) {
                return inject(Key.newInstance(instance.getClass(), Constants.DEFAULT), instance, (InternalContext) args[0]);
            }
        }, null);
    }

    /**
     * 依赖注入
     * 
     * @param key
     * @param instance
     * @param context
     * @return
     */
    <T> T inject(Key<?> key, T instance, InternalContext context) {
        if (context.hasInject(instance)) return instance;
        context.instances.add(instance);
        Class<?> instanceClass = instance.getClass();
        Class<?> fieldType;
        String fieldName;
        Object infect;
        for (List<BoundField> fields : Reflects.getBoundFields(instanceClass).values()) {
            for (BoundField field : fields) {
                // 根据字段类型及其绑定名称获取容器注册实例
                fieldType = field.getField().getType();
                fieldName = Strings.empty(field.getAlias(), Constants.DEFAULT);
                if (!this.setInstance(instance, field, fieldType, fieldName)) {
                    this.setInstance(instance, field, fieldType, fieldName = field.getName());
                }
                if (field.isInject() && (infect = field.getValue(instance)) != null) {
                    this.inject(Key.newInstance(fieldType, fieldName), infect, context);
                }
            }
        }
        List<BoundMethod> methods = Reflects.getBoundMethods(instanceClass, Digest.class, false);
        Collections.sort(methods, new Comparator<BoundMethod>() {
            public int compare(BoundMethod prev, BoundMethod next) {
                return prev.getAnnotation(Digest.class).value() - next.getAnnotation(Digest.class).value();
            }
        });
        for (BoundMethod method : methods) {
            Digest digest = method.getAnnotation(Digest.class);
            if (digest.stage() == Stage.INITIAL) {
                method.invoke(instance);
            }
        }
        return instance;
    }

    /**
     * 设置实例字段值
     * 
     * @param instance
     * @param field
     * @param fieldType
     * @param fieldName
     * @param isDefault
     * @return
     */
    private boolean setInstance(Object instance, BoundField field, Class<?> fieldType, String fieldName) {
        if (field.getField().getAnnotation(Collect.class) != null) {
            Type genericType = field.getField().getGenericType();
            values: if (fieldType.isArray()) {
                Class<?> componentType = fieldType.getComponentType();
                Map<String, ?> instances = getInstances(componentType);
                int size = instances.size();
                if (size > 0) {
                    Object value = Array.newInstance(componentType, size);
                    System.arraycopy(instances.values().toArray(), 0, value, 0, size);
                    field.setValue(instance, value);
                    return true;
                }
            } else if (genericType instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) genericType;
                Type[] actualTypes = paramType.getActualTypeArguments();
                Object value;
                if (fieldType.isAssignableFrom(ArrayList.class)) {
                    Map<String, ?> instances = getInstances((Class<?>) actualTypes[0]);
                    value = new ArrayList<Object>(instances.values());
                } else if (fieldType.isAssignableFrom(HashMap.class)) {
                    value = getInstances((Class<?>) actualTypes[1]);
                } else {
                    break values;
                }
                field.setValue(instance, value);
                return true;
            }
        }
        boolean result;
        if (result = contains(fieldType, fieldName)) {
            field.setValue(instance, getInstance(fieldType, fieldName));
        }
        return result;
    }

    /**
     * 上下文回调
     * 
     * @param callable
     * @return
     */
    private <T> T call(Callable<T> callable, InjectConductor conductor) {
        InternalContext[] reference = this.context.get();
        if (reference[0] == null) {
            reference[0] = new InternalContext(this, conductor);
            try {
                return callable.call(reference[0]);
            } finally {
                reference[0] = null;
                this.context.remove();
            }
        } else {
            return callable.call(reference[0]);
        }
    }

    public synchronized void destroy() {
        if (this.status == Status.UNINITIALIZED || this.status == Status.DESTROYED) return;
        InternalContext[] reference = this.context.get();
        if (reference[0] != null) {
            for (Object instance : reference[0].instances) {
                List<BoundMethod> methods = Reflects.getBoundMethods(instance.getClass(), Digest.class, false);
                Collections.sort(methods, new Comparator<BoundMethod>() {
                    public int compare(BoundMethod prev, BoundMethod next) {
                        return prev.getAnnotation(Digest.class).value() - next.getAnnotation(Digest.class).value();
                    }
                });
                for (BoundMethod method : methods) {
                    Digest digest = method.getAnnotation(Digest.class);
                    if (digest.stage() == Stage.DESTROY) {
                        method.invoke(instance);
                    }
                }
            }
        }
        this.strategies.clear();
        this.factories.clear();
        this.context.remove();
        this.status = Status.DESTROYED;
    }

    public String toString() {
        return this.name;
    }

}
