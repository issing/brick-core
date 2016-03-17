package net.isger.brick.inject;

import java.util.Map;

import net.isger.util.Manageable;

/**
 * 容器接口
 * 
 * @author issing
 * 
 */
public interface Container extends Manageable {

    /**
     * 包含托管实例
     * 
     * @param type
     *            实例类型
     * @return 包含状态
     */
    public boolean contains(Class<?> type);

    /**
     * 包含托管实例
     * 
     * @param type
     *            实例类型
     * @param name
     *            托管名称
     * @return 包含状态
     */
    public boolean contains(Class<?> type, String name);

    /**
     * 获取策略
     * 
     * @param type
     *            实例类型
     * @return 实例策略
     */
    public Strategy getStrategy(Class<?> type);

    /**
     * 获取策略
     * 
     * @param type
     *            实例类型
     * @param name
     *            托管名称
     * @return 实例策略
     */
    public Strategy getStrategy(Class<?> type, String name);

    /**
     * 设置策略
     * 
     * @param type
     *            实例类型
     * @param strategy
     *            实例策略
     * @return 实例策略（原配置策略，没有则返回“null”值️）
     */
    public Strategy setStrategy(Class<?> type, Strategy strategy);

    /**
     * 设置策略
     * 
     * @param type
     *            实例类型
     * @param name
     *            托管名称
     * @param strategy
     *            实例策略（策略为“null”值将会移除当前配置策略）
     * @return 实例策略（原配置策略，没有则返回“null”值️）
     */
    public Strategy setStrategy(Class<?> type, String name, Strategy strategy);

    /**
     * 获取托管实例
     * 
     * @param type
     *            实例类型
     * @return 托管实例
     */
    public <T> T getInstance(Class<T> type);

    /**
     * 获取托管实例
     * 
     * @param type
     *            实例类型
     * @param name
     *            托管名称
     * @return 托管实例
     */
    public <T> T getInstance(Class<T> type, String name);

    /**
     * 获取托管实例
     * 
     * @param type
     *            实例类型
     * @return 指定类型的实例集合（键：托管名称；值：托管实例）
     */
    public <T> Map<String, T> getInstances(Class<T> type);

    /**
     * 依赖注入（根据实例属性设置和容器管控类型完成注入）
     * 
     * @param instance
     *            注入实例
     * @return 注入实例
     */
    public <T> T inject(T instance);

}
