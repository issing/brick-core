package net.isger.brick.config;

import java.util.List;

import net.isger.brick.core.Command;
import net.isger.brick.core.Module;

/**
 * 模块描述
 * 
 * @author issing
 * 
 */
public class ModuleDescribe {

    /** 模块名称 */
    private String name;

    /** 模块实例 */
    private Module module;

    /** 模块命令 */
    private Class<? extends Command> command;

    /** 模块依赖 */
    private List<Object> dependencies;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public Class<? extends Command> getCommand() {
        return command;
    }

    public void setCommand(Class<? extends Command> command) {
        this.command = command;
    }

    public List<Object> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Object> dependencies) {
        this.dependencies = dependencies;
    }

}
