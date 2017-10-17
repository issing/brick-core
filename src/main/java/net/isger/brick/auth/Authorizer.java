package net.isger.brick.auth;

import javax.inject.Inject;

import net.isger.brick.core.Command;
import net.isger.brick.core.CommandHandler;
import net.isger.brick.core.Handler;

/**
 * 授权器
 * 
 * @author issing
 *
 */
public class Authorizer extends CommandHandler {

    /** 自定义授权处理器 */
    @Inject
    private Handler handler;

    public Authorizer() {
        handler = Handler.NOP;
    }

    /**
     * 授权处理
     */
    public final AuthInfo handle(Object message) {
        AuthCommand cmd = (AuthCommand) message;
        Object info = cmd.getToken();
        if (info instanceof Command) {
            /* 绕过认证，执行命令 */
            cmd.setDomain(null);
            cmd.setOperate(null);
            info = super.handle(cmd);
        } else {
            info = handler.handle(message);
        }
        return makeInfo(info);
    }

    /**
     * 制作授权
     * 
     * @param info
     * @return
     */
    protected AuthInfo makeInfo(Object info) {
        return info instanceof AuthInfo ? (AuthInfo) info : null;
    }

}
