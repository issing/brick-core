package net.isger.brick.inject;

import java.util.concurrent.Callable;

import net.isger.brick.util.anno.Scoped;

/**
 * 作用域
 * 
 * @author issing
 * 
 */
public enum Scope {

    /** 默认 */
    DEFAULT {
        protected <T> InternalFactory<? extends T> factory(Class<T> type,
                String name, InternalFactory<? extends T> factory) {
            return factory;
        }
    },

    /** 单例 */
    SINGLETON {
        protected <T> InternalFactory<? extends T> factory(Class<T> type,
                String name, final InternalFactory<? extends T> factory) {
            return new InternalFactory<T>() {
                T instance;

                public T create(InternalContext context) {
                    synchronized (context.container) {
                        if (instance == null) {
                            instance = factory.create(context);
                        }
                        return instance;
                    }
                }
            };
        }
    },

    /** 线程 */
    THREAD {
        protected <T> InternalFactory<? extends T> factory(Class<T> type,
                String name, final InternalFactory<? extends T> factory) {
            return new InternalFactory<T>() {
                private final ThreadLocal<T> threadLocal = new ThreadLocal<T>();

                public T create(final InternalContext context) {
                    T instance = threadLocal.get();
                    if (instance == null) {
                        instance = factory.create(context);
                        threadLocal.set(instance);
                    }
                    return instance;
                }
            };
        }
    },

    /** 策略 */
    STRATEGY {
        protected <T> InternalFactory<? extends T> factory(final Class<T> type,
                final String name, final InternalFactory<? extends T> factory) {
            return new InternalFactory<T>() {
                public T create(InternalContext context) {
                    Strategy strategy = context.getStrategy(type, name);
                    try {
                        return strategy.find(type, name,
                                toCallable(context, factory));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }
    };

    /**
     * 回调方法
     * 
     * @param context
     * @param factory
     * @return
     */
    protected <T> Callable<? extends T> toCallable(
            final InternalContext context,
            final InternalFactory<? extends T> factory) {
        return new Callable<T>() {
            public T call() throws Exception {
                return factory.create(context);
            }
        };
    }

    /**
     * 工厂方法
     * 
     * @param type
     * @param name
     * @param factory
     * @return
     */
    protected abstract <T> InternalFactory<? extends T> factory(Class<T> type,
            String name, InternalFactory<? extends T> factory);

    /**
     * 获取作用域
     * 
     * @param clazz
     * @return
     */
    public static Scope get(Class<?> clazz) {
        Scoped scoped = clazz.getAnnotation(Scoped.class);
        if (scoped != null) {
            Scope scope = scoped.value();
            if (scope != null) {
                return scope;
            }
        }
        return Scope.DEFAULT;
    }

}
