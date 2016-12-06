package net.isger.brick.auth;

import net.isger.brick.Constants;
import net.isger.brick.cache.Cache;
import net.isger.brick.cache.CacheModule;
import net.isger.brick.core.BaseGate;
import net.isger.brick.core.Console;
import net.isger.brick.inject.Container;
import net.isger.util.Helpers;
import net.isger.util.Strings;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

/**
 * 认证基类
 * 
 * @author issing
 *
 */
public class BaseAuth extends BaseGate implements Auth {

    private static final String KEY_IDENTITIES = "brick.auth.identities";

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Console console;

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Container container;

    /** 认证缓存 */
    private transient Cache identities;

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
        identities = ((CacheModule) console.getModule(Constants.MOD_CACHE))
                .getCache(KEY_IDENTITIES);
    }

    /**
     * 检查
     */
    public final void check(AuthCommand cmd) {
        /* 认证初验 */
        String identity = cmd.getIdentity();
        cmd.setResult(checker.isIgnore(cmd.getToken())
                || Strings.isNotEmpty(identity)
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
    protected Object check(String identity, Object token) {
        return identities.get(identity);
    }

    /**
     * 登录
     */
    public final void login(AuthCommand cmd) {
        String identity = cmd.getIdentity();
        if (Strings.isEmpty(identity)) {
            do {
                identity = Helpers.makeUUID();
            } while (identities.get(identity) != null);
            cmd.setIdentity(identity);
        }
        cmd.setResult(save(identity, authenticator.handle(cmd)));
    }

    /**
     * 保存
     * 
     * @param identity
     * @param token
     * @return
     */
    protected Object save(String identity, Object token) {
        if (token != null) {
            identities.set(identity, token);
        }
        return token;
    }

    /**
     * 授权
     */
    public final void auth(AuthCommand cmd) {
        String identity = cmd.getIdentity();
        if (identities.get(identity) != null) {
            cmd.setResult(authorizer.handle(cmd));
        }
    }

    /**
     * 注销
     */
    public void destroy() {
    }

}
