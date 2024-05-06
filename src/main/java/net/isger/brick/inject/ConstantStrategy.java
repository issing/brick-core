package net.isger.brick.inject;

import java.util.concurrent.Callable;

import net.isger.util.Asserts;
import net.isger.util.reflect.Converter;

public class ConstantStrategy implements Strategy<Object> {

    private boolean injected;

    private Object instance;

    protected ConstantStrategy(Object instance) {
        this.setInstance(instance);
    }

    public boolean hasInject(Object instance) {
        return this.instance == instance && this.injected;
    }

    @Override
    public Object find(Container container, Class<Object> type, String name) {
        return this.find(container, type, name, null);
    }

    public Object find(Container container, Class<Object> type, String name, Callable<? extends Object> callable) {
        Object instance = this.getInstance();
        synchronized (instance) {
            if (!this.injected) {
                container.inject(instance);
                this.injected = true;
            }
        }
        return instance;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        if (this.instance != Asserts.isNotNull(instance)) {
            this.instance = instance;
            this.injected = false;
        }
    }

    /**
     * 设置常量策略
     * 
     * @param <T>
     * @param container
     * @param type
     * @param name
     * @param instance
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T set(Container container, Class<T> type, String name, Object instance) {
        Asserts.isInstance(type, instance);
        Object origin = null;
        Strategy<T> strategy = (Strategy<T>) container.getStrategy(type, name);
        set: {
            if (strategy != null) {
                try {
                    origin = strategy.find(container, type, name, null);
                } catch (Exception e) {
                }
                if (strategy instanceof ConstantStrategy) {
                    ((ConstantStrategy) strategy).setInstance(instance);
                    break set;
                }
            }
            container.setStrategy(type, name, (Strategy<T>) (new ConstantStrategy(instance)));
        }
        return (T) Converter.convert(type, origin);
    }

}
