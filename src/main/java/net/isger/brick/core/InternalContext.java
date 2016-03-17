package net.isger.brick.core;

import java.util.Map;

import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

/**
 * 内部上下文
 * 
 * @author issing
 *
 */
final class InternalContext extends Context {

    @Ignore(mode = Mode.INCLUDE)
    private Console console;

    @Ignore(mode = Mode.INCLUDE)
    private Context context;

    @Ignore(mode = Mode.INCLUDE)
    private BaseCommand command;

    InternalContext(Console console, Context context, BaseCommand command) {
        this.console = console;
        this.context = context;
        this.command = command;
    }

    public Map<String, Object> getContextMap() {
        return context.getContextMap();
    }

    public final Console getConsole() {
        return console;
    }

    public final Context getContext() {
        return context;
    }

    public BaseCommand getCommand() {
        return command;
    }

    /**
     * 仿制命令
     * 
     */
    public final BaseCommand mockCommand() {
        BaseCommand cmd = super.mockCommand();
        this.command = cmd;
        return cmd;
    }

    /**
     * 原生命令
     * 
     */
    public final BaseCommand realCommand() {
        BaseCommand cmd = super.realCommand();
        this.command = cmd;
        return cmd;
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

}
