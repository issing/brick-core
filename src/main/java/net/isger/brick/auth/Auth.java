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

    public void login(AuthCommand cmd);

    public void check(AuthCommand cmd);

    public void auth(AuthCommand cmd);

}
