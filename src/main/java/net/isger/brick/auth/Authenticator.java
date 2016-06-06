package net.isger.brick.auth;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.BaseHandler;
import net.isger.brick.core.Command;

/**
 * 认证器
 * 
 * @author issing
 *
 */
public class Authenticator extends BaseHandler {

    public AuthToken handle(Object message) {
        AuthCommand cmd = (AuthCommand) message;
        Object token = cmd.getToken();
        if (token instanceof Command) {
            /* 绕过认证 */
            cmd.setDomain(null);
            cmd.setOperate(null);
            if ((Boolean) super.handle(cmd)) {
                return makeToken(BaseCommand.cast((Command) token).getResult());
            }
        }
        return null;
    }

    protected AuthToken makeToken(Object token) {
        return token instanceof AuthToken ? (AuthToken) token : null;
    }

}
