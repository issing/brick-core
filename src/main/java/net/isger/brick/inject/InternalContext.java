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
        return instance == null || this.instances.contains(instance) || (this.conductor != null && this.conductor.hasInject(instance));
    }

    public <T> Strategy<T> getStrategy(Class<T> type, String name) {
        Strategy<T> strategy = this.container.getStrategy(type, name);
        Asserts.throwState(strategy != null, "Scope strategy not set. Please call Container.setStrategy().");
        return strategy;
    }

}
