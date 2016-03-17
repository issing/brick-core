package net.isger.brick.inject;

/**
 * 内部工厂
 * 
 * @author issing
 *
 * @param <T>
 *            实例类型
 */
interface InternalFactory<T> {

    /**
     * 创建实例
     * 
     * @param context
     * @return
     */
    public T create(InternalContext context);

}
