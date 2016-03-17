package net.isger.brick.inject;

import java.util.concurrent.Callable;

public class ConstantStrategy implements Strategy {

    private Object instance;

    protected ConstantStrategy(Object instance) {
        this.instance = instance;
    }

    @SuppressWarnings("unchecked")
    public <T> T find(Class<T> type, String name, Callable<? extends T> callable)
            throws Exception {
        return (T) getInstance();
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public static <T> T set(Container container, Class<? extends T> type,
            String name, T instance) {
        T oldInstance = null;
        Strategy strategy = container.getStrategy(type, name);
        set: {
            if (strategy != null) {
                try {
                    oldInstance = strategy.find(type, name, null);
                } catch (Exception e) {
                }
                if (strategy instanceof ConstantStrategy) {
                    ((ConstantStrategy) strategy).setInstance(instance);
                    break set;
                }
            }
            container.setStrategy(type, name, new ConstantStrategy(instance));
        }
        return oldInstance;
    }

}
