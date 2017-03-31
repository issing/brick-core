package net.isger.brick.inject;

import java.util.ArrayList;
import java.util.List;

import net.isger.util.Asserts;

/**
 * 内部上下文
 * 
 * @author issing
 * 
 */
class InternalContext {

    final InternalContainer container;

    private List<Object> instances;

    InternalContext(InternalContainer container) {
        this.container = container;
        this.instances = new ArrayList<Object>();
    }

    public boolean hasInject(Object instance) {
        boolean result;
        if (result = instance != null && !this.instances.contains(instance)) {
            this.instances.add(instance);
        }
        return result;
    }

    public Strategy getStrategy(Class<?> type, String name) {
        Strategy strategy = container.getStrategy(type, name);
        Asserts.throwState(strategy != null,
                "Scope strategy not set. Please call Container.setStrategy().");
        return strategy;
    }

}
