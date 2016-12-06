package net.isger.brick.auth;

import net.isger.brick.core.CoreHelper;

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

    public static AuthCommand toCommand(String identity, String domain,
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

}
