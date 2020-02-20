package net.isger.brick.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.CommandHandler;
import net.isger.brick.core.Handler;
import net.isger.util.Helpers;

/**
 * 检验器
 * 
 * @author issing
 *
 */
public class AuthChecker extends CommandHandler {

    /** 忽略项集合 */
    private List<String> ignores;

    /** 自定义检验处理器 */
    @Inject
    private Handler handler;

    public AuthChecker() {
        ignores = new ArrayList<String>();
        handler = Handler.NOP;
    }

    public List<String> getIgnores() {
        return Collections.unmodifiableList(ignores);
    }

    /**
     * 检验处理
     */
    public final Object handle(Object message) {
        AuthCommand cmd = (AuthCommand) message;
        Object result;
        if (Helpers.toBoolean(cmd.getResult())) {
            /* 绕过认证，执行命令 */
            cmd.setDomain(null);
            cmd.setOperate(null);
            result = super.handle(cmd);
        } else {
            result = handler.handle(cmd);
        }
        return result;
    }

    /**
     * 忽略检验
     * 
     * @param token
     * @return
     */
    public boolean isIgnore(Object token) {
        return token instanceof BaseCommand && isIgnore(((BaseCommand) token).getPermission());
    }

    /**
     * 忽略检验
     * 
     * @param permission
     * @return
     */
    public boolean isIgnore(String permission) {
        return ignores.contains(permission);
    }

}
