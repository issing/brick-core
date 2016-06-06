package net.isger.brick.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.BaseHandler;
import net.isger.brick.core.Command;

public class Authorizer extends BaseHandler {

    private List<String> ignores;

    public Authorizer() {
        ignores = new ArrayList<String>();
    }

    public Boolean handle(Object message) {
        AuthCommand cmd = (AuthCommand) message;
        Object token = cmd.getToken();
        boolean result = (Boolean) cmd.getResult();
        if (result || check(token)) {
            /* 绕过认证 */
            cmd.setDomain(null);
            cmd.setOperate(null);
            Object value = super.handle(cmd);
            result = value instanceof Boolean ? (boolean) value : false;
        }
        return result;
    }

    protected boolean check(Object token) {
        return token instanceof Command
                && isIgnore(BaseCommand.cast((Command) token).getPermission());
    }

    protected boolean isIgnore(String permission) {
        return ignores.contains(permission);
    }

    public List<String> getIgnores() {
        return Collections.unmodifiableList(ignores);
    }

}
