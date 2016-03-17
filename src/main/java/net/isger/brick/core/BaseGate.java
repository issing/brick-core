package net.isger.brick.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.isger.brick.util.DynamicOperator;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

/**
 * 基础门
 * 
 * @author issing
 *
 */
@Ignore
public class BaseGate extends DynamicOperator implements Gate {

    @Ignore(mode = Mode.INCLUDE)
    private Map<String, Object> parameters;

    public void initial() {
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }
    }

    protected final Object getParameter(String name) {
        return parameters.get(name);
    }

    protected final Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public void destroy() {
        parameters.clear();
        parameters = null;
    }

}
