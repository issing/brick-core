package net.isger.brick.bus;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.brick.util.DesignLoader;
import net.isger.util.Asserts;
import net.isger.util.Reflects;
import net.isger.util.reflect.ClassAssembler;
import net.isger.util.reflect.conversion.Conversion;

public class EndpointsConversion extends DesignLoader implements Conversion {

    private static final String KEY_NAME = "name";

    private static final Logger LOG;

    private static EndpointsConversion INSTANCE;

    static {
        LOG = LoggerFactory.getLogger(EndpointsConversion.class);
    }

    private EndpointsConversion() {
        super(Endpoint.class);
    }

    public static Conversion getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EndpointsConversion();
        }
        return INSTANCE;
    }

    public boolean isSupport(Type type) {
        return Endpoints.class.isAssignableFrom(Reflects.getRawClass(type));
    }

    @SuppressWarnings("unchecked")
    public Object convert(Type type, Object res, ClassAssembler assembler) {
        Map<String, Endpoint> result;
        Object instance = load(res, assembler);
        if (instance instanceof List) {
            result = new HashMap<String, Endpoint>();
            for (Map<String, Endpoint> o : (List<Map<String, Endpoint>>) instance) {
                result.putAll(o);
            }
        } else {
            result = (Map<String, Endpoint>) instance;
        }
        return new Endpoints(result);
    }

    @SuppressWarnings("unchecked")
    protected final Object create(Class<?> clazz, Map<String, Object> res, ClassAssembler assembler) {
        Map<String, Endpoint> result = new HashMap<String, Endpoint>();
        String name;
        Object config;
        /* 键值对配置方式 */
        for (Entry<String, Object> entry : res.entrySet()) {
            name = entry.getKey();
            config = entry.getValue();
            // 支持路径配置方式
            if (config instanceof String) {
                Object resource = console.loadResource((String) config);
                if (resource != null) {
                    config = resource;
                }
            }
            // 跳过键值对以外配置方式
            if (!(config instanceof Map)) {
                LOG.debug("Skipped unexpected config [{}]", config);
                continue;
            }
            res = (Map<String, Object>) config;
            res.put(KEY_NAME, name);
            result.put(name, createEndpoint((Class<? extends Endpoint>) super.getImplementClass(res), res, assembler));
        }
        return result;
    }

    /**
     * 创建目标实例（暂不支持键值对以外配置方式）
     */
    protected Object create(Object res) {
        throw Asserts.argument("Unexpected config " + res);
    }

    protected Endpoint createEndpoint(Class<? extends Endpoint> clazz, Map<String, Object> res, ClassAssembler assembler) {
        return (Endpoint) super.create(clazz, res, assembler);
    }

    public String toString() {
        return Endpoints.class.getName();
    }

}
