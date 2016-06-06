package net.isger.brick.core;

import net.isger.brick.Constants;
import net.isger.brick.cache.Cache;
import net.isger.brick.cache.CacheModule;
import net.isger.brick.util.CommandOperator;

public class GateTarget extends CommandOperator {

    protected Console getConsole() {
        return Context.getAction().getConsole();
    }

    protected Cache getCache(String name) {
        CacheModule caches = (CacheModule) getConsole().getModule(
                Constants.MOD_CACHE);
        return caches.getCache(name);
    }

    protected Module getModule() {
        return (Module) ((InternalContext) Context.getAction())
                .getInternal(Module.KEY_MODULE);
    }

    protected Gate getGate() {
        return (Gate) ((InternalContext) Context.getAction())
                .getInternal(Gate.KEY_GATE);
    }

    protected BaseCommand getCommand() {
        return BaseCommand.getAction();
    }

    protected BaseCommand mockCommand() {
        return Context.getAction().mockCommand();
    }

    protected BaseCommand realCommand() {
        return Context.getAction().realCommand();
    }

}
