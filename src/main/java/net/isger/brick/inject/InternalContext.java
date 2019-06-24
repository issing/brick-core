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

    final InjectConductor conductor;

    final List<Object> instances;

    InternalContext(InternalContainer container, InjectConductor conductor) {
        this.container = container;
        this.conductor = conductor;
        this.instances = new ArrayList<Object>();
    }

    public boolean hasInject(Object instance) {
        return instance == null || instances.contains(instance) || (conductor != null && conductor.hasInject(instance));
    }

    public Strategy getStrategy(Class<?> type, String name) {
        Strategy strategy = container.getStrategy(type, name);
        Asserts.throwState(strategy != null, "Scope strategy not set. Please call Container.setStrategy().");
        return strategy;
    }

}
