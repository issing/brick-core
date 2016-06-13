package net.isger.brick.test;

import java.util.List;

import net.isger.brick.Constants;
import net.isger.brick.anno.Collect;
import net.isger.brick.cache.Cache;
import net.isger.brick.cache.CacheModule;
import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Context;
import net.isger.brick.core.GateModule;
import net.isger.brick.core.Module;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

public class TestModule extends GateModule {

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.MOD_CACHE)
    private Module caches;

    private String something;

    @Collect
    private List<Module> modules;

    public void operate() {
        try {
            super.operate();
        } catch (Exception e) {
            BaseCommand.getAction().setResult(
                    "TestModule does not implement the operate ["
                            + Context.getAction().getCommand().getOperate()
                            + "] - cache.get(\"say\"): "
                            + getCache().get("say"));
        }
    }

    public void say() {
        getCache().set("say", something + " in the cache");
        BaseCommand.getAction().setResult(
                "TestModule say [" + something + "] - (Modules: " + modules
                        + ")");
    }

    private Cache getCache() {
        return ((CacheModule) caches).getCache(Constants.SYSTEM);
    }

}
