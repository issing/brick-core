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

    public static AuthCommand toCommand(Object token) {
        AuthCommand cmd = AuthCommand.newAction();
        if (cmd == null) {
            cmd = new AuthCommand();
        }
        cmd.setToken(token);
        return cmd;
    }

    public static AuthCommand toCommand(String domain, Object token) {
        AuthCommand cmd = toCommand(token);
        cmd.setDomain(domain);
        return cmd;
    }

    public static AuthCommand toCommand(AuthIdentity identity, Object token) {
        AuthCommand cmd = toCommand(token);
        cmd.setIdentity(identity);
        return cmd;
    }

    public static AuthCommand toCommand(AuthIdentity identity, String domain,
            Object token) {
        AuthCommand cmd = toCommand(domain, token);
        cmd.setIdentity(identity);
        return cmd;
    }

    public static AuthCommand toLogin(Object token) {
        AuthCommand cmd = toCommand(token);
        cmd.setOperate(AuthCommand.OPERATE_LOGIN);
        CoreHelper.toConsole(cmd);
        return cmd;
    }

    public static AuthCommand toLogin(AuthIdentity identity, Object token) {
        AuthCommand cmd = toCommand(identity, token);
        cmd.setOperate(AuthCommand.OPERATE_LOGIN);
        CoreHelper.toConsole(cmd);
        return cmd;
    }

    public static AuthCommand toLogin(String domain, Object token) {
        AuthCommand cmd = toCommand(domain, token);
        cmd.setOperate(AuthCommand.OPERATE_LOGIN);
        CoreHelper.toConsole(cmd);
        return cmd;
    }

    public static AuthCommand toCheck(String domain, Object token) {
        AuthCommand cmd = toCommand(domain, token);
        cmd.setOperate(AuthCommand.OPERATE_CHECK);
        CoreHelper.toConsole(cmd);
        return cmd;
    }

    public static AuthCommand toLogout(Object token) {
        AuthCommand cmd = toCommand(token);
        cmd.setOperate(AuthCommand.OPERATE_LOGOUT);
        CoreHelper.toConsole(cmd);
        return cmd;
    }

}
