package net.isger.brick.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.isger.util.Reflects;
import net.isger.util.Scans;
import net.isger.util.Strings;
import net.isger.util.scan.ScanFilter;

/**
 * 扫描加载器
 * 
 * @author issing
 *
 */
public class ScanLoader extends ConsoleLoader {

    public static final String PARAM_PATH = "path";

    /** 扫描过滤器 */
    private ScanFilter filter;

    public ScanLoader(ScanFilter filter) {
        this(null, filter);
    }

    public ScanLoader(Class<?> targetClass, ScanFilter filter) {
        super(targetClass);
        this.filter = filter;
    }

    protected Object create(Class<?> clazz, Map<String, Object> res) {
        Object result = null;
        if (hasScan(clazz, res)) {
            Map<String, Object> container = new HashMap<String, Object>();
            Object config = res.get(PARAM_PATH);
            if (config != null) {
                res.remove(PARAM_PATH);
                if (config instanceof String) {
                    container.putAll(scan((String) config, res));
                } else if (config instanceof Collection) {
                    for (Object path : (Collection<?>) config) {
                        if (!(path instanceof String)) {
                            throw new IllegalArgumentException("Invalid config");
                        }
                        container.putAll(scan((String) path, res));
                    }
                } else {
                    throw new IllegalArgumentException("Invalid config");
                }
            }
            result = container;
        } else {
            result = make(clazz, res);
        }
        return result;
    }

    protected Object create(Object res) {
        if (res instanceof String) {
            return scan((String) res, null);
        }
        return super.create(res);
    }

    protected boolean hasScan(Class<?> clazz, Map<String, Object> res) {
        return (clazz == getTargetClass() || !res.containsKey(PARAM_CLASS))
                && res.containsKey(PARAM_PATH);
    }

    protected Map<String, Object> scan(String path, Map<String, Object> res) {
        Map<String, Object> result = new HashMap<String, Object>();
        // 扫描实例
        Object instance;
        String className;
        for (String name : Scans.scan(path.replaceAll("[.\\\\]", "/"), filter)) {
            name = Strings.replaceIgnoreCase(name, "[.]class$").replaceAll(
                    "[\\\\/]", ".");
            className = path.replaceAll("[\\\\/]", ".") + name;
            instance = make(Reflects.getClass(className), res);
            if (instance != null) {
                result.put(name.replaceFirst("^[.]", ""), instance);
            }
        }
        return result;
    }

    /**
     * 产生扫描实例
     * 
     * @param clazz
     * @param res
     * @return
     */
    protected Object make(Class<?> clazz, Map<String, Object> res) {
        if (Reflects.isAbstract(clazz)) {
            return null; // 不支持抽象类或接口
        }
        return super.create(clazz, res);
    }

    @SuppressWarnings("unchecked")
    protected List<Object> toList(Object instance) {
        List<Object> result;
        if (instance instanceof List) {
            result = (List<Object>) instance;
        } else {
            result = new ArrayList<Object>();
            result.add(instance);
        }
        return result;
    }

}
