package net.isger.brick.auth;

import net.isger.brick.core.CoreHelper;

/**
 * 认证助手
 * 
 * @author issing
 */
public class AuthHelper extends CoreHelper {

    protected AuthHelper() {
    }

    public static AuthCommand makeCommand(Object token) {
        AuthCommand cmd = AuthCommand.newAction();
        if (cmd == null) {
            cmd = new AuthCommand();
        }
        cmd.setToken(token);
        return cmd;
    }

    public static AuthCommand makeCommand(String domain, Object token) {
        AuthCommand cmd = makeCommand(token);
        cmd.setDomain(domain);
        return cmd;
    }

    public static AuthCommand makeCommand(AuthIdentity identity, Object token) {
        AuthCommand cmd = makeCommand(token);
        cmd.setIdentity(identity);
        return cmd;
    }

    public static AuthCommand makeCommand(AuthIdentity identity, String domain, Object token) {
        AuthCommand cmd = makeCommand(domain, token);
        cmd.setIdentity(identity);
        return cmd;
    }

    public static AuthCommand toLogin(Object token) {
        AuthCommand cmd = makeCommand(token);
        cmd.setOperate(AuthCommand.OPERATE_LOGIN);
        CoreHelper.toConsole(cmd);
        return cmd;
    }

    public static AuthCommand toLogin(AuthIdentity identity, Object token) {
        AuthCommand cmd = makeCommand(identity, token);
        cmd.setOperate(AuthCommand.OPERATE_LOGIN);
        CoreHelper.toConsole(cmd);
        return cmd;
    }

    public static AuthCommand toLogin(String domain, Object token) {
        AuthCommand cmd = makeCommand(domain, token);
        cmd.setOperate(AuthCommand.OPERATE_LOGIN);
        CoreHelper.toConsole(cmd);
        return cmd;
    }

    public static AuthCommand toCheck(String domain, Object token) {
        AuthCommand cmd = makeCommand(domain, token);
        cmd.setOperate(AuthCommand.OPERATE_CHECK);
        CoreHelper.toConsole(cmd);
        return cmd;
    }

    public static AuthCommand toLogout(Object token) {
        AuthCommand cmd = makeCommand(token);
        cmd.setOperate(AuthCommand.OPERATE_LOGOUT);
        CoreHelper.toConsole(cmd);
        return cmd;
    }

}
