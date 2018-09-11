package net.isger.brick.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.isger.brick.core.BaseGate;

public class BaseCache extends BaseGate implements Cache {

    private Map<String, Object> values;

    public BaseCache() {
        values = new ConcurrentHashMap<String, Object>();
    }

    public Object get(String key) {
        return values.get(key);
    }

    public void set(String key, Object value) {
        values.put(key, value);
    }

    public Object remove(String key) {
        return values.remove(key);
    }

    public Set<String> keySet() {
        return values.keySet();
    }

    public Collection<Object> values() {
        return values.values();
    }

    public void clear() {
        values.clear();
    }

    public void destroy() {
        this.clear();
        super.destroy();
    }

}
