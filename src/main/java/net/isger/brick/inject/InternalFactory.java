package net.isger.brick.inject;

/**
 * 内部工厂
 * 
 * @author issing
 *
 * @param <T> 实例类型
 */
interface InternalFactory<T> {

    /**
     * 注入状态
     * 
     * @param context
     * @param instance
     * @return
     */
    public boolean hasInject(InternalContext context, T instance);

    /**
     * 创建实例
     * 
     * @param context
     * @return
     */
    public T create(InternalContext context);

}
