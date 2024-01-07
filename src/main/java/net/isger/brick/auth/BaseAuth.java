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
    protected AuthChecker checker;

    /** 认证器 */
    protected Authenticator authenticator;

    /** 授权器 */
    protected Authorizer authorizer;

    /**
     * 初始
     */
    public void initial() {
        super.initial();
        /* 检验器 */
        if (this.checker == null) this.checker = new AuthChecker();
        this.container.inject(this.checker);
        /* 认证器 */
        if (this.authenticator == null) this.authenticator = new Authenticator();
        this.container.inject(this.authenticator);
        /* 授权器 */
        if (this.authorizer == null) this.authorizer = new Authorizer();
        this.container.inject(this.authorizer);
    }

    /**
     * 登录
     */
    public final void login(AuthCommand cmd) {
        /* 身份处理 */
        AuthIdentity identity = cmd.getIdentity();
        if (identity == null) cmd.setIdentity(identity = createIdentity());
        else if (identity.isLogin()) logout(identity);
        /* 登录处理 */
        Object result = null;
        AuthToken<?> token = login(identity, this.authenticator.handle(cmd));
        if (token != null) result = token.getSource();
        cmd.setResult(result);
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
    protected AuthToken<?> login(AuthIdentity identity, AuthToken<?> token) {
        if (token != null) identity.setToken(token);
        return token;
    }

    /**
     * 检查
     */
    public final void check(AuthCommand cmd) {
        Object token = cmd.getToken();
        /* 认证初验 */
        AuthIdentity identity = cmd.getIdentity();
        cmd.setResult(identity != null && Helpers.toBoolean(this.check(identity, token)) || this.isIgnore(token));
        /* 检验器终验 */
        this.checker.handle(cmd);
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
     * 忽略
     * 
     * @param token
     * @return
     */
    protected boolean isIgnore(Object token) {
        return this.checker.isIgnore(token);
    }

    /**
     * 授权
     */
    public final void auth(AuthCommand cmd) {
        cmd.setResult(this.authorizer.handle(cmd));
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
        identity.clear();
        identity.setToken(null);
    }

    /**
     * 注销
     */
    public void destroy() {
        super.destroy();
    }

}
