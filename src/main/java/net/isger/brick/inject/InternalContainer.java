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
 * 
 */
class InternalContainer implements Container {

    /** 容器名称 */
    final String name;

    /** 实例工厂集合 */
    final Map<Key<?>, InternalFactory<?>> factories;

    /** 实例策略集合 */
    final Map<Key<?>, Strategy> strategies;

    /** 注入实例后备 */
    transient InjectReserver reserver;

    /** 容器构建上下文 */
    private volatile ThreadLocal<InternalContext[]> context;

    InternalContainer(String name, Map<Key<?>, InternalFactory<?>> factories) {
        this.name = name;
        this.factories = new ConcurrentHashMap<Key<?>, InternalFactory<?>>(factories);
        strategies = new ConcurrentHashMap<Key<?>, Strategy>();
        reserver = new InjectReserver() {
            public boolean contains(Key<?> key) {
                return false;
            }

            public <T> T alternate(Key<T> key, InjectConductor conductor) {
                return null;
            }
        };
        context = new ThreadLocal<InternalContext[]>() {
            protected InternalContext[] initialValue() {
                return new InternalContext[1];
            }
        };
    }

    public void initial() {
        /* 托管容器自身 */
        factories.put(Key.newInstance(Container.class, name), new InternalFactory<Container>() {
            public Container create(InternalContext context) {
                return InternalContainer.this;
            }
        });
        /* 添加注入后备器 */
        for (InjectReserver reserver : Helpers.sort(new ArrayList<InjectReserver>(getInstances(InjectReserver.class).values()))) {
            this.reserver = InjectMulticaster.addInternal(this.reserver, reserver);
        }
    }

    public boolean contains(Class<?> type) {
        return contains(type, Constants.DEFAULT);
    }

    public boolean contains(Class<?> type, String name) {
        boolean result;
        contains: if (!(result = contains(Key.newInstance(type, name)))) {
            // 检索父类
            Class<?> superClass = type.getSuperclass();
            while (superClass != null && superClass != Object.class) {
                if (contains(Key.newInstance(superClass, name))) {
                    result = true;
                    break contains;
                }
                superClass = superClass.getSuperclass();
            }
            // 检索接口
            Class<?>[] interfaceClasses = Reflects.getInterfaces(type);
            for (Class<?> interfaceClass : interfaceClasses) {
                if (contains(Key.newInstance(interfaceClass, name))) {
                    result = true;
                    break contains;
                }
            }
            // 检索子类
            result = getInstances(type).containsKey(name);
        }
        return result;
    }

    private boolean contains(Key<?> key) {
        return factories.containsKey(key) || strategies.containsKey(key) || reserver.contains(key);
    }

    public Strategy getStrategy(Class<?> type) {
        return getStrategy(type, Constants.DEFAULT);
    }

    public Strategy getStrategy(Class<?> type, String name) {
        return strategies.get(Key.newInstance(type, name));
    }

    public Strategy setStrategy(Class<?> type, Strategy strategy) {
        return setStrategy(type, Constants.DEFAULT, strategy);
    }

    public Strategy setStrategy(Class<?> type, String name, Strategy strategy) {
        Key<?> key = Key.newInstance(type, name);
        /** 移除策略 */
        if (strategy == null) {
            return strategies.remove(key);
        }
        return strategies.put(key, strategy);
    }

    public <T> T getInstance(Class<T> type) {
        return getInstance(type, Constants.DEFAULT);
    }

    public <T> T getInstance(Class<T> type, String name) {
        return getInstance(type, name, null);
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
                        instance = getInstances(type).get(name);
                    }
                }
                return (T) instance;
            }
        }, conductor);
    }

    @SuppressWarnings("unchecked")
    private <T> T getInstance(Key<T> key, final InternalContext context) {
        T result = null;
        InternalFactory<T> factory = (InternalFactory<T>) factories.get(key);
        if (factory == null) {
            /* 策略模式查找对象 */
            if (strategies.containsKey(key)) {
                try {
                    result = strategies.get(key).find(key.getType(), key.getName(), null);
                } catch (Exception e) {
                    throw Asserts.state(e.getMessage(), e.getCause());
                }
            }
        } else {
            result = factory.create(context);
        }
        /* 注入替补 */
        if (result == null) {
            result = reserver.alternate(key, new InjectConductor() {
                public boolean hasInject(Object instance) {
                    return context.hasInject(instance);
                }
            });
        }
        /* 注入对象 */
        inject(key, result, context);
        return result;
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
        Strategy strategy;
        Key<?> key;
        for (Entry<Key<?>, Strategy> entry : strategies.entrySet()) {
            // 允许实例向上赋值
            key = entry.getKey();
            if (type.isAssignableFrom(key.type) && ((strategy = entry.getValue()) != null)) {
                try {
                    instance = (T) strategy.find(key.type, key.name, null);
                    if (instance != null) {
                        inject(key, instance, context);
                        instances.put(key.getName(), instance);
                    }
                } catch (Exception e) {
                    throw Asserts.state(e.getMessage(), e.getCause());
                }
            }
        }
        // 获取工厂注册对象
        InternalFactory<?> factory;
        for (Entry<Key<?>, InternalFactory<?>> entry : factories.entrySet()) {
            // 允许实例向上赋值
            key = entry.getKey();
            if (type.isAssignableFrom(key.type) && (factory = entry.getValue()) != null) {
                try {
                    instance = (T) factory.create(context);
                    if (instance != null) {
                        inject(key, instance, context);
                        instances.put(key.getName(), instance);
                    }
                } catch (Exception e) {
                    throw Asserts.state(e.getMessage(), e.getCause());
                }
            }
        }
        return instances;
    }

    public <T> T inject(final T instance) {
        return call(new Callable<T>() {
            public T call(Object... args) {
                inject(Key.newInstance(instance.getClass(), Constants.DEFAULT), instance, (InternalContext) args[0]);
                return instance;
            }
        }, null);
    }

    /**
     * 依赖注入
     * 
     * @param key
     * @param instance
     *            注入实例
     * @param context
     *            上下文
     */
    private void inject(Key<?> key, Object instance, InternalContext context) {
        if (context.hasInject(instance)) {
            return;
        }
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
                if (!setInstance(instance, field, fieldType, fieldName)) {
                    setInstance(instance, field, fieldType, fieldName = field.getName());
                }
                if (field.isInject() && (infect = field.getValue(instance)) != null) {
                    inject(Key.newInstance(fieldType, fieldName), infect, context);
                }
            }
        }
        List<BoundMethod> methods = Reflects.getBoundMethods(instanceClass, Digest.class);
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
        InternalContext[] reference = context.get();
        if (reference[0] == null) {
            reference[0] = new InternalContext(this, conductor);
            try {
                return callable.call(reference[0]);
            } finally {
                reference[0] = null;
                context.remove();
            }
        } else {
            return callable.call(reference[0]);
        }
    }

    public void destroy() {
        InternalContext[] reference = context.get();
        if (reference[0] != null) {
            for (Object instance : reference[0].instances) {
                List<BoundMethod> methods = Reflects.getBoundMethods(instance.getClass(), Digest.class);
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
        strategies.clear();
        factories.clear();
        context.remove();
    }

    public String toString() {
        return name;
    }

}
