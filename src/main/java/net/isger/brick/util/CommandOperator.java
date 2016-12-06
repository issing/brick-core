package net.isger.brick.util;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Command;
import net.isger.util.DynamicOperator;
import net.isger.util.anno.Ignore;
import net.isger.util.reflect.BoundMethod;

/**
 * 命令操作器
 * 
 * @author issing
 *
 */
@Ignore
public class CommandOperator extends DynamicOperator {

    public CommandOperator() {
    }

    public CommandOperator(Object source) {
        super(source);
    }

    /**
     * 动态绑定方法操作
     * 
     */
    public void operate() {
        this.operate(BaseCommand.getAction());
    }

    /**
     * 动态绑定方法操作
     * 
     * @param cmd
     */
    public void operate(BaseCommand cmd) {
        BoundMethod method;
        String operate = cmd.getOperate();
        /* 尝试带命令操作方法 */
        Class<?> paramType = cmd.getClass();
        do {
            if ((method = getMethod(BoundMethod.makeMethodDesc(operate,
                    Void.TYPE, paramType))) == null) {
                paramType = paramType.getSuperclass();
                continue;
            }
            method.invoke(getSource(), cmd);
            return;
        } while (paramType != Command.class);
        /* 采用无参数操作方法 */
        method = getMethod(operate);
        if (method != null) {
            method.invoke(getSource());
            return;
        }
        /* 采用无参数默认方法 */
        super.operate();
    }

}
