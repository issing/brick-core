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
import java.util.concurrent.ConcurrentHashMap;

import net.isger.brick.Constants;
import net.isger.brick.util.anno.Collect;
import net.isger.brick.util.anno.Digest;
import net.isger.util.Callable;
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

    /** 实例工厂集合 */
    final Map<Key<?>, InternalFactory<?>> facs;

    /** 实例策略集合 */
    final Map<Key<?>, Strategy> stgs;

    /** 容器构建上下文 */
    private ThreadLocal<InternalContext[]> context;

    InternalContainer(Map<Key<?>, InternalFactory<?>> factories) {
        this.facs = new ConcurrentHashMap<Key<?>, InternalFactory<?>>(
                factories);
        this.stgs = new ConcurrentHashMap<Key<?>, Strategy>();
        this.context = new ThreadLocal<InternalContext[]>() {
            protected InternalContext[] initialValue() {
                return new InternalContext[1];
            }
        };
    }

    public void initial() {
        /* 托管容器自身 */
        this.facs.put(Key.newInstance(Container.class, Constants.SYSTEM),
                new InternalFactory<Container>() {
                    public Container create(InternalContext context) {
                        return InternalContainer.this;
                    }
                });
    }

    public boolean contains(Class<?> type) {
        return contains(type, Constants.DEFAULT);
    }

    public boolean contains(Class<?> type, String name) {
        boolean hasContains = contains(Key.newInstance(type, name));
        find: if (!hasContains) {
            Class<?> superclass = type.getSuperclass();
            while (superclass != null) {
                if (hasContains = contains(Key.newInstance(superclass, name))) {
                    break find;
                }
                superclass = superclass.getSuperclass();
            }
            Class<?>[] interfaces = Reflects.getInterfaces(type);
            for (Class<?> interfaceClass : interfaces) {
                if (hasContains = contains(
                        Key.newInstance(interfaceClass, name))) {
                    break find;
                }
            }
            hasContains = getInstances(type).containsKey(name);
        }
        return hasContains;
    }

    private boolean contains(Key<?> key) {
        return facs.get(key) != null || stgs.containsKey(key);
    }

    public Strategy getStrategy(Class<?> type) {
        return getStrategy(type, Constants.DEFAULT);
    }

    public Strategy getStrategy(Class<?> type, String name) {
        return stgs.get(Key.newInstance(type, name));
    }

    public Strategy setStrategy(Class<?> type, Strategy strategy) {
        return setStrategy(type, Constants.DEFAULT, strategy);
    }

    public Strategy setStrategy(Class<?> type, String name, Strategy strategy) {
        Key<?> key = Key.newInstance(type, name);
        /** 移除策略 */
        if (strategy == null) {
            return stgs.remove(key);
        }
        return stgs.put(key, strategy);
    }

    public <T> T getInstance(Class<T> type) {
        return getInstance(type, Constants.DEFAULT);
    }

    public <T> T getInstance(final Class<T> type, final String name) {
        return call(new Callable<T>() {
            @SuppressWarnings("unchecked")
            public T call(Object... args) {
                Object instance = getInstance(Key.newInstance(type, name),
                        (InternalContext) args[0]);
                find: if (instance == null) {
                    Class<?> superclass = type.getSuperclass();
                    while (superclass != null) {
                        instance = getInstance(
                                Key.newInstance(superclass, name),
                                (InternalContext) args[0]);
                        if (instance != null && type.isInstance(instance)) {
                            break find;
                        }
                        superclass = superclass.getSuperclass();
                    }
                    Class<?>[] interfaces = Reflects.getInterfaces(type);
                    for (Class<?> interfaceClass : interfaces) {
                        instance = getInstance(
                                Key.newInstance(interfaceClass, name),
                                (InternalContext) args[0]);
                        if (instance != null && type.isInstance(instance)) {
                            break find;
                        }
                    }
                    instance = getInstances(type).get(name);
                }
                return (T) instance;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <T> T getInstance(Key<T> key, InternalContext context) {
        InternalFactory<T> factory = (InternalFactory<T>) facs.get(key);
        T result;
        if (factory == null) {
            /* 策略模式查找对象 */
            if (stgs.containsKey(key)) {
                try {
                    result = stgs.get(key).find(key.getType(), key.getName(),
                            null);
                } catch (Exception e) {
                    throw new IllegalStateException(e.getMessage(),
                            e.getCause());
                }
            } else {
                return null;
            }
        } else {
            result = factory.create(context);
        }
        /* 注入对象 */
        inject(result, context);
        return result;
    }

    public <T> Map<String, T> getInstances(final Class<T> type) {
        return call(new Callable<Map<String, T>>() {
            public Map<String, T> call(Object... args) {
                return getInstances(type, (InternalContext) args[0]);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <T> Map<String, T> getInstances(Class<T> type,
            InternalContext context) {
        Map<String, T> instances = new HashMap<String, T>();
        T instance;
        // 获取策略模式对象
        Strategy strategy;
        for (Key<?> key : stgs.keySet()) {
            // 跳过存在指定类型实例工厂配置
            if (key.type == type && facs.get(key) != null) {
                break;
            }
            // 允许实例向上赋值
            if (type.isAssignableFrom(key.type)
                    && ((strategy = stgs.get(key)) != null)) {
                try {
                    instance = (T) strategy.find(key.type, key.name, null);
                    if (instance != null) {
                        inject(instance, context);
                        instances.put(key.getName(), instance);
                    }
                } catch (Exception e) {
                    throw new IllegalStateException(e.getMessage(),
                            e.getCause());
                }
            }
        }
        // 获取工厂注册对象
        InternalFactory<T> factory;
        for (Key<?> key : facs.keySet()) {
            if (key.type == type
                    && (factory = (InternalFactory<T>) facs.get(key)) != null) {
                try {
                    instance = factory.create(context);
                    if (instances != null) {
                        inject(instance, context);
                        instances.put(key.getName(), instance);
                    }
                } catch (Exception e) {
                    throw new IllegalStateException(e.getMessage(),
                            e.getCause());
                }
            }
        }
        return instances;
    }

    public <T> T inject(final T instance) {
        return call(new Callable<T>() {
            public T call(Object... args) {
                inject(instance, (InternalContext) args[0]);
                return instance;
            }
        });
    }

    /**
     * 依赖注入
     * 
     * @param instance
     *            注入实例
     * @param context
     *            上下文
     */
    private void inject(Object instance, InternalContext context) {
        if (context.hasInject(instance)) {
            Class<?> instanceClass = instance.getClass();
            Class<?> fieldType;
            Object infect;
            for (List<BoundField> fields : Reflects
                    .getBoundFields(instanceClass).values()) {
                for (BoundField field : fields) {
                    // 根据字段类型及其绑定名称获取容器注册实例
                    fieldType = field.getField().getType();
                    if (!setInstance(instance, field, fieldType, Strings
                            .empty(field.getAlias(), Constants.DEFAULT))) {
                        setInstance(instance, field, fieldType,
                                field.getName());
                    }
                    if (field.isInject()
                            && (infect = field.getValue(instance)) != null) {
                        inject(infect, context);
                    }
                }
            }
            List<BoundMethod> methods = Reflects.getBoundMethods(instanceClass,
                    Digest.class);
            Collections.sort(methods, new Comparator<BoundMethod>() {
                public int compare(BoundMethod prev, BoundMethod next) {
                    return prev.getAnnotation(Digest.class).value()
                            - next.getAnnotation(Digest.class).value();
                }
            });
            for (BoundMethod method : methods) {
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
    private boolean setInstance(Object instance, BoundField field,
            Class<?> fieldType, String fieldName) {
        if (field.getField().getAnnotation(Collect.class) != null) {
            Type genericType = field.getField().getGenericType();
            values: if (fieldType.isArray()) {
                Class<?> componentType = fieldType.getComponentType();
                Map<String, ?> instances = getInstances(componentType);
                int size = instances.size();
                if (size > 0) {
                    Object value = Array.newInstance(componentType, size);
                    System.arraycopy(instances.values().toArray(), 0, value, 0,
                            size);
                    field.setValue(instance, value);
                    return true;
                }
            } else if (genericType instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) genericType;
                Type[] actualTypes = paramType.getActualTypeArguments();
                Object value;
                if (fieldType.isAssignableFrom(ArrayList.class)) {
                    Map<String, ?> instances = getInstances(
                            (Class<?>) actualTypes[0]);
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
    private <T> T call(Callable<T> callable) {
        InternalContext[] reference = context.get();
        if (reference[0] == null) {
            reference[0] = new InternalContext(this);
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
        stgs.clear();
        facs.clear();
        context.remove();
    }

}
