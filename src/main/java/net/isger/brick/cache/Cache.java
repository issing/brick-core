package net.isger.brick.cache;

import net.isger.brick.core.Gate;

public interface Cache extends Gate {

    public Object get(String key);

    public void set(String key, Object value);

}
