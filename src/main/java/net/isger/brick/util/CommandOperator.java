package net.isger.brick.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
        BoundMethod boundMethod;
        String operate = cmd.getOperate();
        if (Strings.isEmpty(operate)) {
            return;
        }
        /* 尝试带命令操作方法 */
        Class<?> paramType = cmd.getClass();
        do {
            if ((boundMethod = getMethod(BoundMethod.makeMethodDesc(operate,
                    Void.TYPE, paramType))) == null) {
                paramType = paramType.getSuperclass();
                continue;
            }
            boundMethod.invoke(getSource(), cmd);
            return;
        } while (paramType != Command.class);
        /* 采用带参数操作方法 */
        boundMethod = getMethod(operate);
        if (boundMethod != null) {
            Method method = boundMethod.getMethod();
            Class<?>[] paramTypes = method.getParameterTypes();
            Annotation[][] annos = method.getParameterAnnotations();
            List<Object> params = new ArrayList<Object>();
            int size = paramTypes.length;
            String paramName;
            Object paramValue;
            for (int i = 0; i < size; i++) {
                if (paramTypes[i].isInstance(cmd)
                        && Command.class.isAssignableFrom(paramTypes[i])) {
                    paramValue = cmd;
                } else {
                    paramName = Helpers.getAliasName(annos[i]);
                    if (Strings.isEmpty(paramName)) {
                        paramValue = cmd.getParameter(operate + i);
                    } else {
                        paramValue = cmd.getParameter(paramName);
                    }
                }
                params.add(paramValue);
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
            throw Asserts.state("Failure to invoke [%s] in %s", operate,
                    getSource(), cause);
        }
    }

}
