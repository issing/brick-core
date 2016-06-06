package net.isger.brick.cache;

import net.isger.brick.core.Gate;
import net.isger.brick.core.GateModule;

public class CacheModule extends GateModule {

    private static final String CACHE = "cache";

    public Class<? extends Gate> getTargetClass() {
        return Cache.class;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Gate> getImplementClass() {
        Class<? extends Gate> implClass = (Class<? extends Gate>) getImplementClass(
                CACHE, null);
        if (implClass == null) {
            implClass = super.getImplementClass();
        }
        return implClass;
    }

    public Class<? extends Gate> getBaseClass() {
        return BaseCache.class;
    }

    /**
     * 获取缓存
     * 
     * @param name
     * @return
     */
    public Cache getCache(String name) {
        Gate gate = this.getGate(name);
        if (gate == null) {
            this.setGate(name, gate = createGate());
        }
        return (Cache) gate;
    }

}
