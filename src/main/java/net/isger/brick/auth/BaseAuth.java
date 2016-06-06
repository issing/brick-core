package net.isger.brick.auth;

import net.isger.brick.Constants;
import net.isger.brick.cache.Cache;
import net.isger.brick.cache.CacheModule;
import net.isger.brick.core.BaseGate;
import net.isger.brick.core.Console;
import net.isger.brick.core.Module;
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

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Console console;

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Container container;

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.MOD_CACHE)
    private Module caches;

    /** 认证器 */
    private Authenticator authenticator;

    /** 授权器 */
    private Authorizer authorizer;

    public void initial() {
        super.initial();
        if (authenticator == null) {
            authenticator = new Authenticator();
        }
        container.inject(authenticator);
        if (authorizer == null) {
            authorizer = new Authorizer();
        }
        container.inject(authorizer);
    }

    /**
     * 登录
     */
    public void login() {
        Cache cache = ((CacheModule) caches).getCache(KEY_IDENTITIES);
        AuthCommand cmd = AuthCommand.getAction();
        String identity = cmd.getIdentity();
        if (Strings.isEmpty(identity)) {
            do {
                identity = Helpers.makeUUID();
            } while (cache.get(identity) != null);
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
        Cache cache = ((CacheModule) caches).getCache(KEY_IDENTITIES);
        if (token != null) {
            cache.set(identity, token);
        }
        return token;
    }

    /**
     * 检查
     */
    public void check() {
        AuthCommand cmd = AuthCommand.getAction();
        String identity = cmd.getIdentity();
        if (Strings.isNotEmpty(identity)) {
            Object auth = auth(identity, cmd.getToken());
            cmd.setResult(auth instanceof Boolean ? (Boolean) auth : false);
        } else {
            cmd.setResult(false);
        }
        cmd.setResult(authorizer.handle(cmd));
    }

    /**
     * 授权
     * 
     * @param identity
     * @return
     */
    protected Object auth(String identity, Object token) {
        return ((CacheModule) caches).getCache(KEY_IDENTITIES).get(identity);
    }

    public void destroy() {
    }

}
