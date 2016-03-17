package net.isger.brick.util;

import java.util.List;
import java.util.Map;

import net.isger.brick.core.BaseCommand;
import net.isger.util.Asserts;
import net.isger.util.Operator;
import net.isger.util.Reflects;
import net.isger.util.anno.Ignore;
import net.isger.util.reflect.BoundMethod;

/**
 * 动态操作器
 * 
 * @author issing
 *
 */
@Ignore
public class DynamicOperator implements Operator, Cloneable {

    /** 操作源 */
    private Object source;

    public DynamicOperator() {
        this.source = this;
    }

    public DynamicOperator(Object source) {
        Asserts.isNotNull(source, "The operator source not be null");
        this.source = source;
    }

    /**
     * 获取当前实例所有绑定方法
     * 
     * @return
     */
    private Map<String, List<BoundMethod>> getMethods() {
        return Reflects.getBoundMethods(getSource().getClass());
    }

    /**
     * 获取当前实例指定绑定方法
     * 
     * @param name
     * @return
     */
    private BoundMethod getMethod(String name) {
        Map<String, List<BoundMethod>> methods = getMethods();
        List<BoundMethod> method = methods.get(name);
        Asserts.isNotNull(method, "Not found the method [" + name + "] in "
                + getSource().getClass());
        return method.get(0);
    }

    /**
     * 操作检测
     * 
     * @param name
     * @return
     */
    protected boolean hasOperate(String name) {
        return getMethods().get(name) != null;
    }

    /**
     * 本实例动态绑定方法操作
     * 
     */
    public void operate() {
        operate(BaseCommand.getAction().getOperate());
    }

    /**
     * 本实例指定绑定方法操作
     * 
     * @param operate
     */
    protected void operate(String operate) {
        getMethod(operate).invoke(getSource());
    }

    /**
     * 获取操作源（子类操进行克隆操作需考虑重写本方法）
     * 
     * @return
     */
    protected Object getSource() {
        return source;
    }

    /**
     * 克隆实例
     */
    public Object clone() {
        DynamicOperator operator;
        try {
            operator = (DynamicOperator) super.clone();
            if (this == getSource()) {
                operator.source = operator;
            }
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e.getMessage(), e.getCause());
        }
        return operator;
    }
}
