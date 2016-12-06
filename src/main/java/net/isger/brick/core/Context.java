package net.isger.brick.core;

import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import net.isger.util.anno.Ignore;

/**
 * 上下文
 * 
 * @author issing
 *
 */
@Ignore
public abstract class Context {

    /** 线程级别上下文 */
    static final ThreadLocal<Context> ACTION;

    /** 实例集合 */
    private Map<String, Object> context;

    /** 原生命令队列 */
    private Deque<BaseCommand> reals;

    static {
        ACTION = new ThreadLocal<Context>();
    }

    protected Context() {
        this(null);
    }

    protected Context(Map<String, Object> contextMap) {
        this.context = new ConcurrentHashMap<String, Object>();
        this.reals = new LinkedBlockingDeque<BaseCommand>();
        if (contextMap != null) {
            this.context.putAll(contextMap);
        }
    }

    /**
     * 获取所有实例
     * 
     * @return
     */
    public Map<String, Object> getContextMap() {
        return Collections.unmodifiableMap(this.context);
    }

    /**
     * 获取实例
     * 
     * @param key
     * @return
     */
    public Object get(String key) {
        return context.get(key);
    }

    /**
     * 存放实例
     * 
     * @param key
     * @param value
     */
    public void set(String key, Object value) {
        if (value == null) {
            context.remove(key);
        } else {
            context.put(key, value);
        }
    }

    /**
     * 清除实例
     * 
     */
    public void clear() {
        context.clear();
    }

    /**
     * 获取命令
     * 
     * @return
     */
    public abstract Console getConsole();
    
    /**
     * 获取命令
     * 
     * @return
     */
    public abstract BaseCommand getCommand();

    /**
     * 拷贝命令
     * 
     * @return
     */
    public BaseCommand newCommand() {
        BaseCommand cmd = getCommand();
        if (cmd != null) {
            cmd = (BaseCommand) cmd.clone();
        }
        return cmd;
    }

    /**
     * 仿制命令
     * 
     * @return
     */
    public BaseCommand mockCommand() {
        BaseCommand cmd = getCommand();
        if (cmd != null) {
            reals.push(cmd);
            cmd = (BaseCommand) cmd.clone();
        }
        return cmd;
    }

    /**
     * 原生命令
     * 
     * @return
     */
    public BaseCommand realCommand() {
        BaseCommand cmd;
        if (reals.size() > 0) {
            cmd = reals.pop();
        } else {
            cmd = getCommand();
        }
        return cmd;
    }

    /**
     * 获取活动上下文
     * 
     * @return
     */
    public static Context getAction() {
        return ACTION.get();
    }

    /**
     * 设置活动上下文
     * 
     * @param context
     */
    static void setAction(Context context) {
        ACTION.set(context);
    }

    /**
     * 获取活动实例
     * 
     * @param key
     * @return
     */
    public static Object getAction(String key) {
        return getAction().get(key);
    }

    /**
     * 设置活动实例
     * 
     * @param key
     * @param value
     */
    public static void setAction(String key, Object value) {
        getAction().set(key, value);
    }

}
