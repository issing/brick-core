package net.isger.brick.test;

import java.util.List;

import net.isger.brick.Constants;
import net.isger.brick.cache.Cache;
import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Context;
import net.isger.brick.core.GateModule;
import net.isger.brick.core.Module;
import net.isger.brick.util.anno.Collect;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

public class TestModule extends GateModule {

    @Alias(Constants.SYSTEM)
    @Ignore(mode = Mode.INCLUDE, serialize = false)
    private Cache cache;

    private String something;

    @Collect
    private List<Module> modules;

    public void operate() {
        BaseCommand.getAction().setResult("TestModule does not implement the operate [" + Context.getAction().getCommand().getOperate() + "] - cache.get(\"say\"): " + cache.get("say"));
    }

    public void say() {
        cache.set("say", something + " in the cache");
        BaseCommand.getAction().setResult("TestModule say [" + something + "] - (Modules: " + modules + ")");
    }

}
