package net.isger.brick.util;

import net.isger.brick.core.BaseCommand;
import net.isger.util.DynamicOperator;
import net.isger.util.anno.Ignore;

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
     * 本实例动态绑定方法操作
     * 
     */
    public void operate() {
        operate(BaseCommand.getAction().getOperate());
    }

}
