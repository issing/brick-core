package net.isger.brick.inject;

import java.util.concurrent.Callable;

import net.isger.brick.util.anno.Scoped;
import net.isger.util.Helpers;

/**
 * 作用域
 * 
 * @author issing
 * 
 */
public enum Scope {

    /** 默认 */
    DEFAULT {
        protected <T> InternalFactory<T> factory(Class<T> type, String name, InternalFactory<T> factory) {
            return factory;
        }
    },

    /** 单例 */
    SINGLETON {
        protected <T> InternalFactory<T> factory(final Class<T> type, final String name, final InternalFactory<T> factory) {
            return new InternalFactory<T>() {
                boolean injected;

                T instance;

                public boolean hasInject(InternalContext context, T instance) {
                    return this.injected;
                }

                public T create(InternalContext context) {
                    synchronized (context.container) {
                        if (this.instance == null) {
                            this.instance = (T) factory.create(context);
                            if (!(factory.hasInject(context, this.instance))) {
                                context.container.inject(Key.newInstance(type, name), this.instance, context);
                            }
                            this.injected = true;
                        }
                        return this.instance;
                    }
                }
            };
        }
    },

    /** 线程 */
    THREAD {
        protected <T> InternalFactory<T> factory(final Class<T> type, final String name, final InternalFactory<T> factory) {
            return new InternalFactory<T>() {
                final ThreadLocal<Object[]> threadLocal = new ThreadLocal<Object[]>();

                public boolean hasInject(InternalContext context, T instance) {
                    Object[] instances = this.threadLocal.get();
                    return instances != null && instances[0] == instance && Helpers.toBoolean(instances[1]);
                }

                @SuppressWarnings("unchecked")
                public T create(InternalContext context) {
                    synchronized (this.threadLocal) {
                        Object[] instances = this.threadLocal.get();
                        if (instances == null) {
                            instances = new Object[] { factory.create(context), false };
                            if (!factory.hasInject(context, (T) instances[0])) {
                                context.container.inject(Key.newInstance(type, name), instances[0], context);
                            }
                            instances[1] = true;
                            this.threadLocal.set(instances);
                        }
                        return (T) instances[0];
                    }
                }
            };
        }
    },

    /** 策略 */
    STRATEGY {
        protected <T> InternalFactory<T> factory(final Class<T> type, final String name, final InternalFactory<T> factory) {
            return new InternalFactory<T>() {
                public boolean hasInject(InternalContext context, T instance) {
                    Strategy<T> strategy = context.getStrategy(type, name);
                    return strategy != null && strategy.hasInject(instance);
                }

                public T create(InternalContext context) {
                    Strategy<T> strategy = context.getStrategy(type, name);
                    try {
                        return strategy.find(context.container, type, name, callable(context, factory));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }
    };

    /**
     * 工厂方法
     * 
     * @param <T>
     * @param type
     * @param name
     * @param factory
     * @return
     */
    protected abstract <T> InternalFactory<T> factory(Class<T> type, String name, InternalFactory<T> factory);

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

    /**
     * 回调方法
     * 
     * @param context
     * @param factory
     * @return
     */
    <T> Callable<? extends T> callable(final InternalContext context, final InternalFactory<T> factory) {
        return new Callable<T>() {
            public T call() throws Exception {
                return factory.create(context);
            }
        };
    }

}
