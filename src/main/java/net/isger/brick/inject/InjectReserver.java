package net.isger.brick.inject;

/**
 * 注入后备器
 * 
 * @author issing
 */
public interface InjectReserver {

    /**
     * 包含实例
     *
     * @param key
     * @return
     */
    public boolean contains(Key<?> key);

    /**
     * 备用实例
     *
     * @param key
     * @param conductor
     * @return
     */
    public <T> T alternate(Key<T> key, InjectConductor conductor);

}
