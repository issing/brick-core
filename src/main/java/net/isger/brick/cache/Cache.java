package net.isger.brick.cache;

import java.util.Collection;
import java.util.Set;

import net.isger.brick.core.Gate;

public interface Cache extends Gate {

    public Object get(String key);

    public void set(String key, Object value);

    public Object remove(String key);

    public Set<String> keySet();

    public Collection<Object> values();

    public void clear();

}
