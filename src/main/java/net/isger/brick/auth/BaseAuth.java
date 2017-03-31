package net.isger.brick.auth;

import net.isger.brick.core.BaseGate;
import net.isger.util.Helpers;

/**
 * 认证基类
 * 
 * @author issing
 *
 */
public class BaseAuth extends BaseGate implements Auth {

    /** 检验器 */
    private AuthChecker checker;

    /** 认证器 */
    private Authenticator authenticator;

    /** 授权器 */
    private Authorizer authorizer;

    /**
     * 初始
     */
    public void initial() {
        super.initial();
        /* 检验器 */
        if (checker == null) {
            checker = new AuthChecker();
        }
        container.inject(checker);
        /* 认证器 */
        if (authenticator == null) {
            authenticator = new Authenticator();
        }
        container.inject(authenticator);
        /* 授权器 */
        if (authorizer == null) {
            authorizer = new Authorizer();
        }
        container.inject(authorizer);
    }

    /**
     * 登录
     */
    public final void login(AuthCommand cmd) {
        AuthIdentity identity = cmd.getIdentity();
        if (identity == null) {
            cmd.setIdentity(identity = createIdentity());
        } else if (identity.isLogin()) {
            logout(identity);
        }
        cmd.setResult(login(identity, authenticator.handle(cmd)));
    }

    /**
     * 创建身份
     * 
     * @return
     */
    protected AuthIdentity createIdentity() {
        return new AuthIdentity();
    }

    /**
     * 登录
     * 
     * @param identity
     * @param token
     * @return
     */
    protected Object login(AuthIdentity identity, Object token) {
        if (token != null) {
            identity.setToken(token);
        }
        return token;
    }

    /**
     * 检查
     */
    public final void check(AuthCommand cmd) {
        /* 认证初验 */
        AuthIdentity identity = cmd.getIdentity();
        cmd.setResult(checker.isIgnore(cmd.getToken()) || identity != null
                && Helpers.toBoolean(check(identity, cmd.getToken())));
        /* 检验器终验 */
        checker.handle(cmd);
    }

    /**
     * 检查
     * 
     * @param identity
     * @param token
     * @return
     */
    protected Object check(AuthIdentity identity, Object token) {
        return identity.getToken().equals(token);
    }

    /**
     * 授权
     */
    public final void auth(AuthCommand cmd) {
        // String identity = cmd.getIdentity();
        // if (identities.get(identity) != null) {
        cmd.setResult(authorizer.handle(cmd));
        // }
    }

    /**
     * 登出
     */
    public final void logout(AuthCommand cmd) {
        AuthIdentity identity = cmd.getIdentity();
        if (identity != null && identity.isLogin()) {
            logout(identity);
        }
    }

    /**
     * 登出
     * 
     * @param identity
     */
    protected void logout(AuthIdentity identity) {
        if (identity == null) {
            return;
        }
        identity.setToken(null);
        identity.clear();
    }

    /**
     * 注销
     */
    public void destroy() {
    }

}
