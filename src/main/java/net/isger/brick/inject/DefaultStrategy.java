package net.isger.brick.inject;

import java.util.concurrent.Callable;

import net.isger.util.Reflects;

/**
 * 默认策略
 * 
 * @author issing
 *
 */
public class DefaultStrategy implements Strategy {

    private static final Strategy DEFAULT;

    static {
        DEFAULT = new DefaultStrategy();
    }

    protected DefaultStrategy() {
    }

    public <T> T find(Class<T> type, String name, Callable<? extends T> callable)
            throws Exception {
        return Reflects.newInstance(type);
    }

    public static Strategy set(Container container, Class<?> type, String name) {
        Strategy strategy = container.getStrategy(type, name);
        if (!(strategy instanceof DefaultStrategy)) {
            container.setStrategy(type, name, DefaultStrategy.DEFAULT);
        }
        return strategy;
    }

}
