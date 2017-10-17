package net.isger.brick.auth;

import net.isger.brick.core.Gate;

/**
 * 认证接口
 * 
 * @author issing
 *
 */
public interface Auth extends Gate {

    public static final String KEY_AUTH = "brick.core.auth";

    /**
     * 登录
     *
     * @param cmd
     */
    public void login(AuthCommand cmd);

    /**
     * 检测
     *
     * @param cmd
     */
    public void check(AuthCommand cmd);

    /**
     * 授权
     *
     * @param cmd
     */
    public void auth(AuthCommand cmd);

    /**
     * 退出
     *
     * @param cmd
     */
    public void logout(AuthCommand cmd);

}
