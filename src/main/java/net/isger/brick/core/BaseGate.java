package net.isger.brick.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.isger.brick.Constants;
import net.isger.brick.inject.Container;
import net.isger.brick.util.CommandOperator;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

/**
 * 基础门
 * 
 * @author issing
 *
 */
@Ignore
public class BaseGate implements Gate {

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    protected Container container;

    /** 操作器 */
    @Ignore(mode = Mode.INCLUDE)
    private CommandOperator operator;

    @Ignore(mode = Mode.INCLUDE)
    private Map<String, Object> parameters;

    public BaseGate() {
        operator = new CommandOperator(this);
        parameters = new HashMap<String, Object>();
    }

    public void initial() {
    }

    protected final Object getParameter(String name) {
        return parameters.get(name);
    }

    protected final Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public void operate(GateCommand cmd) {
        operator.operate(cmd);
    }

    public void destroy() {
        parameters.clear();
    }

}
