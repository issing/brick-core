package net.isger.brick.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.isger.brick.auth.AuthIdentity;
import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Command;
import net.isger.util.Asserts;
import net.isger.util.DynamicOperator;
import net.isger.util.Helpers;
import net.isger.util.Strings;
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
        String operate = cmd.getOperate();
        if (Strings.isEmpty(operate)) {
            return;
        }
        /* 采用默认操作方法 */
        BoundMethod boundMethod;
        matchMethod: {
            // 获取指定操作方法
            if (BoundMethod.isMethodDesc(operate)) {
                boundMethod = getMethod(operate);
                if (boundMethod == null) {
                    operate = BoundMethod.getName(operate);
                } else {
                    break matchMethod;
                }
            }
            // 尝试纯命令操作方法
            Class<?> paramType = cmd.getClass();
            do {
                if ((boundMethod = getMethod(BoundMethod.makeMethodDesc(operate, Void.TYPE, paramType))) == null) {
                    paramType = paramType.getSuperclass();
                    continue;
                }
                boundMethod.invoke(getSource(), cmd);
                return;
            } while (paramType != Command.class);
            boundMethod = getMethod(operate);
        }
        /* 采用指定操作方法 */
        if (boundMethod != null) {
            Method method = boundMethod.getMethod();
            Class<?>[] paramTypes = method.getParameterTypes();
            Annotation[][] annos = method.getParameterAnnotations();
            List<Object> params = new ArrayList<Object>();
            int size = paramTypes.length;
            for (int i = 0; i < size; i++) {
                params.add(getParameter(paramTypes[i], cmd, Strings.empty(Helpers.getAliasName(annos[i]), operate + i)));
            }
            Object result = boundMethod.invoke(getSource(), params.toArray());
            if (!Void.TYPE.equals(method.getReturnType())) {
                cmd.setResult(result);
            }
            return;
        }
        /* 采用无参数默认方法 */
        try {
            super.operate();
        } catch (Throwable cause) {
            throw Asserts.state("Failure to invoke [%s] in %s", operate, getSource(), cause);
        }
    }

    /**
     * 获取类型参数
     *
     * @param type
     * @param cmd
     * @param name
     * @return
     */
    protected Object getParameter(Class<?> type, BaseCommand cmd, String name) {
        Object value;
        AuthIdentity identity = cmd.getIdentity();
        if (Command.class.isAssignableFrom(type) && type.isInstance(cmd)) {
            value = cmd;
        } else if (AuthIdentity.class.isAssignableFrom(type) && type.isInstance(identity)) {
            value = identity;
        } else {
            value = cmd.getParameter(name);
        }
        return value;
    }

}
