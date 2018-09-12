package net.isger.brick.auth;

import net.isger.brick.Constants;
import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Command;
import net.isger.brick.core.Console;
import net.isger.brick.core.Context;
import net.isger.brick.core.Preparer;
import net.isger.util.Helpers;
import net.isger.util.Strings;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

/**
 * 认证制备器
 * 
 * @author issing
 *
 */
public class AuthPreparer extends Preparer {

    /** 控制台 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Console console;

    /** 认证模块 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.MOD_AUTH)
    private AuthModule module;

    /**
     * 创建上下文（认证干涉）
     */
    protected Context createContext(BaseCommand command) {
        return super.createContext(meddle(command));
    }

    /**
     * 更新上下文（认证干涉）
     */
    protected void updateContext(Context context, BaseCommand command) {
        super.updateContext(context, meddle(command));
    }

    /**
     * 认证干涉
     * 
     * @param command
     * @return
     */
    protected BaseCommand meddle(BaseCommand command) {
        if (command instanceof AuthCommand) {
            AuthCommand cmd = (AuthCommand) command;
            boolean result;
            Object token = cmd.getToken();
            /* 绕过权限 */
            if (result = Strings.isEmpty(cmd.getDomain())
                    && Strings.isEmpty(cmd.getOperate())
                    && token instanceof Command) {
                command = BaseCommand.cast((Command) token);
            }
            cmd.setResult(result);
        } else {
            String domain;
            if (module
                    .getGate(domain = console.getModuleName(command)) != null) {
                /* 检测干涉 */
                AuthCommand cmd = AuthHelper.toCommand(command.getIdentity(),
                        domain, command);
                cmd.setOperate(AuthCommand.OPERATE_CHECK);
                command = cmd;
            } else if (command.getIdentity() == null) {
                AuthCommand cmd = AuthHelper.toCommand(Constants.SYSTEM,
                        new BaseToken(Helpers.makeUUID(), command));
                cmd.setOperate(AuthCommand.OPERATE_LOGIN);
                console.execute(cmd);
                command.setIdentity(cmd.getIdentity());
            }
        }
        return command;
    }

}
