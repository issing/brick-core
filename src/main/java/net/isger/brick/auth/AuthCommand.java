package net.isger.brick.auth;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Command;
import net.isger.brick.core.GateCommand;

/**
 * 认证命令
 * 
 * @author issing
 *
 */
public class AuthCommand extends GateCommand {

    public static final String CTRL_TOKEN = "auth-token";

    public static final String OPERATE_LOGIN = "login";

    public static final String OPERATE_AUTH = "auth";

    public static final String OPERATE_CHECK = "check";

    public static final String OPERATE_LOGOUT = "logout";

    volatile transient boolean checked;

    public AuthCommand() {
    }

    public AuthCommand(Command cmd) {
        super(cmd);
    }

    public AuthCommand(boolean hasShell) {
        super(hasShell);
    }

    public static AuthCommand getAction() {
        return cast(BaseCommand.getAction());
    }

    public static AuthCommand newAction() {
        return cast(BaseCommand.newAction());
    }

    public static AuthCommand mockAction() {
        return cast(BaseCommand.mockAction());
    }

    public static AuthCommand realAction() {
        return cast(BaseCommand.realAction());
    }

    public static AuthCommand cast(BaseCommand cmd) {
        return cmd == null || cmd.getClass() == AuthCommand.class ? (AuthCommand) cmd : cmd.infect(new AuthCommand(false));
    }

    public Object getToken() {
        return getToken(this);
    }

    public void setToken(Object token) {
        setToken(this, token);
    }

    public static Object getToken(BaseCommand cmd) {
        return cmd.getHeader(CTRL_TOKEN);
    }

    public static void setToken(BaseCommand cmd, Object token) {
        cmd.setHeader(CTRL_TOKEN, token);
    }

}
