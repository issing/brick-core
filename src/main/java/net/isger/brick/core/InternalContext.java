package net.isger.brick.core;

import java.util.Map;

/**
 * 内部上下文
 * 
 * @author issing
 *
 */
final class InternalContext extends Context {

    private Context context;

    BaseCommand command;

    InternalContext(Context context) {
        this.context = context;
        this.command = context.getCommand();
    }

    public Map<String, Object> getContextMap() {
        return context.getContextMap();
    }

    /**
     * 获取实例
     * 
     */
    public Object get(String key) {
        return context.get(key);
    }

    /**
     * 存放实例
     * 
     */
    public void set(String key, Object value) {
        context.set(key, value);
    }

    /**
     * 清除实例
     * 
     */
    public void clear() {
        super.clear();
        context.clear();
    }

    public final Context getContext() {
        return context;
    }

    public final Console getConsole() {
        return context.getConsole();
    }

    public final BaseCommand getCommand() {
        return command;
    }

    /**
     * 仿制命令
     * 
     */
    public final BaseCommand mockCommand() {
        return this.command = super.mockCommand();
    }

    /**
     * 原生命令
     * 
     */
    public final BaseCommand realCommand() {
        return this.command = super.realCommand();
    }

    /**
     * 获取内部实例
     * 
     * @param key
     * @return
     */
    Object getInternal(String key) {
        return super.get(key);
    }

    /**
     * 存放内部实例
     * 
     * @param key
     * @param value
     */
    void setInternal(String key, Object value) {
        super.set(key, value);
    }

}
