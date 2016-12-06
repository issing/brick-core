package net.isger.brick.auth;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Command;
import net.isger.brick.core.CommandHandler;
import net.isger.util.Helpers;

/**
 * 认证器
 * 
 * @author issing
 *
 */
public class Authenticator extends CommandHandler {

    /**
     * 认证处理
     */
    public final AuthToken<?> handle(Object message) {
        AuthCommand cmd = (AuthCommand) message;
        Object token = cmd.getToken();
        if (isSupport(cmd)) {
            /* 绕过认证，执行命令 */
            cmd.setDomain(null);
            cmd.setOperate(null);
            if (Helpers.toBoolean(super.handle(cmd))) {
                token = BaseCommand.cast((Command) token).getResult();
            }
        }
        return makeToken(cmd, token);
    }

    /**
     * 支持认证
     * 
     * @param cmd
     * @return
     */
    protected boolean isSupport(AuthCommand cmd) {
        return cmd.getToken() instanceof Command;
    }

    /**
     * 制作认证
     * 
     * @param cmd
     * @param token
     * @return
     */
    protected AuthToken<?> makeToken(AuthCommand cmd, Object token) {
        return token instanceof AuthToken ? (AuthToken<?>) token : null;
    }

}
