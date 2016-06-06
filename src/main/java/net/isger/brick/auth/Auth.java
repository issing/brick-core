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

    public static final String KEY_IDENTITIES = "brick.auth.identities";

}
