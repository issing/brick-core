package net.isger.brick.inject;

import java.util.concurrent.Callable;

/**
 * 注入策略
 * 
 * @author issing
 * 
 */
public interface Strategy<T> {

    /**
     * 注入状态
     * 
     * @param instance
     * @return
     */
    public boolean hasInject(T instance);

    /**
     * 查找实例
     * 
     * @param container
     * @param type
     * @param name
     * @return
     */
    public T find(Container container, Class<T> type, String name);

    /**
     * 查找实例
     * 
     * @param container
     * @param type
     * @param name
     * @param callable
     * @return
     */
    public T find(Container container, Class<T> type, String name, Callable<? extends T> callable);

}
