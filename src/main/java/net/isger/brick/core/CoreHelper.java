package net.isger.brick.core;

import net.isger.brick.Constants;
import net.isger.brick.cache.Cache;
import net.isger.brick.cache.CacheModule;
import net.isger.util.Callable;

/**
 * 核心助手
 * 
 * @author issing
 *
 */
public class CoreHelper {

    private static final Callable<Object> CALLABLE_CONSOLE;

    private static final Callable<Object> CALLABLE_MODULE;

    private static final Callable<Object> CALLABLE_GATE;

    static {
        CALLABLE_CONSOLE = new Callable<Object>() {
            public Object call(Object... args) {
                BaseCommand cmd = (BaseCommand) args[0];
                getConsole().execute(cmd);
                return cmd.getResult();
            }
        };
        CALLABLE_MODULE = new Callable<Object>() {
            public Object call(Object... args) {
                BaseCommand cmd = (BaseCommand) args[0];
                getModule().execute(cmd);
                return cmd.getResult();
            }
        };
        CALLABLE_GATE = new Callable<Object>() {
            public Object call(Object... args) {
                GateCommand cmd = (GateCommand) args[0];
                getGate().operate(cmd);
                return cmd.getResult();
            }
        };
    }

    protected CoreHelper() {
    }

    public static Object to(BaseCommand cmd, Callable<Object> callable) {
        return callable.call(cmd);
    }

    public static Object toConsole(BaseCommand cmd) {
        return to(cmd, CALLABLE_CONSOLE);
    }

    public static Object toModule(BaseCommand cmd) {
        return to(cmd, CALLABLE_MODULE);
    }

    public static Object toGate(GateCommand cmd) {
        return to(cmd, CALLABLE_GATE);
    }

    public static Console getConsole() {
        return Context.getAction().getConsole();
    }

    public static Module getModule() {
        return getConsole().getModule();
    }

    public static Gate getGate() {
        return ((GateModule) getModule()).getGate();
    }

    public static CacheModule getCacheModule() {
        return (CacheModule) getConsole().getModule(Constants.MOD_CACHE);
    }

    public static Cache getCache(String name) {
        return getCacheModule().getCache(name);
    }

}
