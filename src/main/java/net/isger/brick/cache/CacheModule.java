package net.isger.brick.cache;

import java.util.ArrayList;
import java.util.List;

import net.isger.brick.Constants;
import net.isger.brick.core.Gate;
import net.isger.brick.core.GateModule;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

public class CacheModule extends GateModule {

    private static final String CACHE = "cache";

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.BRICK_CACHES)
    private List<String> caches;

    public CacheModule() {
        caches = new ArrayList<String>();
    }

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

    public void initial() {
        for (String cache : caches) {
            makeCache(cache);
        }
        super.initial();
    }

    /**
     * 获取缓存
     * 
     * @param name
     * @return
     */
    private void makeCache(String name) {
        if (this.getGate(name) == null) {
            this.setGate(name, create());
        }
    }

}
