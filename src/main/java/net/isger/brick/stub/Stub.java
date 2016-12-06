package net.isger.brick.stub;

import net.isger.brick.core.Gate;

public interface Stub extends Gate {

    /**
     * 创建
     * 
     * @param cmd
     */
    public void create(StubCommand cmd);

    /**
     * 新增
     * 
     * @param cmd
     */
    public void insert(StubCommand cmd);

    /**
     * 删除
     * 
     * @param cmd
     */
    public void delete(StubCommand cmd);

    /**
     * 修改
     * 
     * @param cmd
     */
    public void update(StubCommand cmd);

    /**
     * 查询
     * 
     * @param cmd
     */
    public void select(StubCommand cmd);

    /**
     * 移除
     * 
     * @param cmd
     */
    public void remove(StubCommand cmd);

}
