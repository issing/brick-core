package net.isger.brick.inject;

/**
 * 容器实例供应器
 * 
 * @author issing
 * 
 */
public interface ContainerProvider {

    /**
     * 重载检测
     * 
     * @return
     */
    public boolean isReload();

    /**
     * 注册实例
     * 
     * @param builder
     */
    public void register(ContainerBuilder builder);

}
