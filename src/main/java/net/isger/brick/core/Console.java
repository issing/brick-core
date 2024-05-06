package net.isger.brick.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.brick.Constants;
import net.isger.brick.auth.AuthCommand;
import net.isger.brick.auth.AuthModule;
import net.isger.brick.bus.BusCommand;
import net.isger.brick.bus.BusModule;
import net.isger.brick.cache.CacheCommand;
import net.isger.brick.cache.CacheModule;
import net.isger.brick.config.ModuleDescribe;
import net.isger.brick.inject.ConstantStrategy;
import net.isger.brick.inject.Container;
import net.isger.brick.stub.StubCommand;
import net.isger.brick.stub.StubModule;
import net.isger.brick.task.TaskCommand;
import net.isger.brick.task.TaskModule;
import net.isger.brick.util.Assemblers;
import net.isger.brick.util.CommandOperator;
import net.isger.raw.Artifact;
import net.isger.raw.Depository;
import net.isger.raw.Prober;
import net.isger.raw.ProberMulticaster;
import net.isger.util.Asserts;
import net.isger.util.Dependency;
import net.isger.util.Helpers;
import net.isger.util.Manageable;
import net.isger.util.Reflects;
import net.isger.util.Strings;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;
import net.isger.util.load.BaseLoader;
import net.isger.util.load.Loader;
import net.isger.util.reflect.ClassAssembler;

/**
 * 基础控制台
 * 
 * @author issing
 */
public class Console implements Manageable {

    private static final String SUFFIX_MODULE = ".module";

    private static final Logger LOG;

    /** 控制台状态 */
    private transient volatile Status status;

    /** 类装配器 */
    private transient ClassAssembler assembler;

    /** 配置加载器 */
    private transient Loader loader;

    /** 命令操作器 */
    private transient CommandOperator operator;

    /** 核心容器 */
    @Alias(Constants.SYSTEM)
    @Ignore(mode = Mode.INCLUDE, serialize = false)
    protected Container container;

    /** 资源预留占位配置器 */
    @Alias(Constants.SYSTEM)
    @Ignore(mode = Mode.INCLUDE, serialize = false)
    private PlaceholderConfigurer configurer;

    /** 控制台名称 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.BRICK_NAME)
    private String name;

    /** 资源探查器 */
    @Ignore(mode = Mode.INCLUDE)
    private Prober prober;

    /** 预处理器 */
    @Ignore(mode = Mode.INCLUDE)
    private Preparer preparer;

    /** 模块依赖 */
    private transient Dependency dependency;

    /** 注销钩子 */
    private transient Thread hook;

    /** 传声器 */
    private transient Airfone airfone;

    static {
        LOG = LoggerFactory.getLogger(Console.class);
    }

    public Console() {
        this.operator = new CommandOperator(this);
        this.preparer = new Preparer();
        this.dependency = new Dependency();
        this.status = Status.UNINITIALIZED;
    }

    /**
     * 准备就绪
     *
     * @return
     */
    public final boolean hasReady() {
        return this.status == Status.INITIALIZED;
    }

    /**
     * 控制台状态
     */
    public final Status getStatus() {
        return this.status;
    }

    /**
     * 初始
     */
    public final synchronized void initial() {
        if (!(status == Status.UNINITIALIZED || status == Status.DESTROYED)) return;
        this.status = Status.INITIALIZING;
        // 注销钩子
        Runtime.getRuntime().addShutdownHook(this.hook = new Thread(new Runnable() {
            public void run() {
                Console.this.hook = null;
                Console.this.destroy();
            }
        }));
        /* 初始内核 */
        this.assembler = Assemblers.createAssembler(this.container); // 类装配器
        Class<?> moduleDescribe = this.container.getInstance(Class.class, Constants.BRICK_DESCRIBE); // 模块描述
        Asserts.isAssignable(ModuleDescribe.class, moduleDescribe, "Invalid module describe [%s] in container", moduleDescribe);
        this.loader = new BaseLoader(moduleDescribe); // 模块加载器
        this.loadKernel();
        this.loadApp();
        // 初始模块
        Map<String, Module> modules = new HashMap<String, Module>(this.getModules());
        try {
            for (Object node : this.dependency.getNodes()) {
                this.container.inject(modules.get(node)).initial(); // 强制注入后再行初始化
            }
        } catch (Throwable e) {
            throw Asserts.state("Failure to initial module", e);
        }
        this.status = Status.INITIALIZED;
        /* 等待就绪 */
        loop: for (;;) {
            ready: {
                List<Entry<String, Module>> entries = new ArrayList<Entry<String, Module>>(modules.entrySet());
                for (Entry<String, Module> entry : entries) {
                    if (entry.getValue().hasReady()) {
                        modules.remove(entry.getKey());
                    } else {
                        Helpers.sleep(200l);
                        break ready;
                    }
                }
                break loop;
            }
        }

    }

    /**
     * 加载资源
     * 
     * @param res
     * @return
     */
    public Object loadResource(String res) {
        return this.loadResource(Depository.getArtifact(res, this.prober));
    }

    /**
     * 加载资源
     * 
     * @param artifact
     * @return
     */
    private Object loadResource(Artifact artifact) {
        Object resource = null;
        if (artifact != null) {
            resource = artifact.transform(Object.class);
            if (this.configurer != null) {
                resource = this.configurer.displace(resource);
            }
        }
        return resource;
    }

    /**
     * 加载内核
     * 
     */
    protected void loadKernel() {
        /* 默认内核 */
        this.setupModule(Constants.MOD_CACHE, new CacheModule(), CacheCommand.class);                   // 缓存模块
        this.setupModule(Constants.MOD_AUTH, new AuthModule(), AuthCommand.class, Constants.MOD_CACHE); // 认证模块
        this.setupModule(Constants.MOD_TASK, new TaskModule(), TaskCommand.class, Constants.MOD_AUTH);  // 任务模块
        this.setupModule(Constants.MOD_BUS, new BusModule(), BusCommand.class, Constants.MOD_TASK);     // 总线模块
        this.setupModule(Constants.MOD_STUB, new StubModule(), StubCommand.class, Constants.MOD_AUTH);  // 存根模块
        /* 加载内核 */
        if (!Strings.matchsIgnoreCase(this.name, Constants.BRICK)) {
            this.loadKernel(Constants.BRICK); // 加载默认配置（指定非“brick”配置时，将首先加载全局默认“brick”配置）
        }
        this.loadKernel(this.name); // 加载目标配置
    }

    /**
     * 加载内核
     * 
     * @param name
     */
    @SuppressWarnings("unchecked")
    protected final void loadKernel(String name) {
        Object config;
        /* 加载参数配置 */
        for (Artifact artifact : Depository.getArtifacts(name + "-config", this.prober)) {
            config = this.loadResource(artifact);
            if (config instanceof Collection) {
                this.loadConstants((Collection<?>) config);
            } else if (config instanceof Map) {
                this.loadConstants((Map<String, Object>) config);
            }
        }
        /* 加载内核配置 */
        for (Artifact artifact : Depository.getArtifacts(name + "-kernel", this.prober)) {
            config = this.loadResource(artifact);
            if (config instanceof Collection) {
                this.loadModule((Collection<?>) config);
            } else if (config instanceof Map) {
                this.loadModule((Map<String, Object>) config);
            }
        }
    }

    /**
     * 安装模块
     *
     * @param name
     * @param module
     */
    protected final void setupModule(String name, Module module) {
        this.setupModule(name, module, null);
    }

    /**
     * 安装模块
     *
     * @param name
     * @param module
     * @param commandClass
     * @param dependencies
     */
    protected final void setupModule(String name, Module module, Class<? extends Command> commandClass, Object... dependencies) {
        if (this.getModule(name) == null) {
            this.addModule(name, module);
        }
        this.addDependencies(name, dependencies);
        if (commandClass != null) {
            this.addCommand(name, commandClass);
        }
    }

    /**
     * 加载常量
     * 
     * @param res
     */
    @SuppressWarnings("unchecked")
    protected final void loadConstants(Collection<?> res) {
        for (Object config : res) {
            /* 加载指定常量资源 */
            if (config instanceof String) {
                config = this.loadResource(config + "-constants");
            }
            /* 跳过非键值对集合 */
            if (!(config instanceof Map)) {
                LOG.warn("(!) Skipped invalid constants config {}", config);
                continue;
            }
            /* 加载键值对集合常量 */
            this.loadConstants((Map<String, Object>) config);
        }
    }

    /**
     * 加载常量
     * 
     * @param config
     */
    protected final void loadConstants(Map<String, Object> config) {
        Class<?> type;
        Object value;
        for (Entry<String, Object> entry : config.entrySet()) {
            value = entry.getValue();
            /* 尝试加载为实例 */
            if (value instanceof Map) value = BaseLoader.toLoad(value);
            type = value.getClass();
            if (Map.class.isAssignableFrom(type)) {
                type = Map.class;
            } else if (List.class.isAssignableFrom(type)) {
                type = List.class;
            } else if (CharSequence.class.isAssignableFrom(type)) {
                Class<?> clazz = Reflects.getClass(value);
                if (clazz != null) {
                    ConstantStrategy.set(this.container, Class.class, entry.getKey(), clazz);
                }
            }
            ConstantStrategy.set(this.container, type, entry.getKey(), value);
        }
    }

    /**
     * 加载模块
     * 
     * @param res
     */
    @SuppressWarnings("unchecked")
    protected final void loadModule(Collection<?> res) {
        String name;
        Map<String, Object> params;
        for (Object config : res) {
            if (config instanceof String) {
                name = (String) config;
                config = this.loadResource(name + "-module");
                if (!(config instanceof Map)) {
                    LOG.warn("(!) Skipped invalid module config {}", config);
                    continue;
                }
                params = new HashMap<String, Object>((Map<String, Object>) config);
                if (!params.containsKey(Constants.CONF_NAME)) {
                    params.put(Constants.CONF_NAME, name);
                }
            } else if (!(config instanceof Map)) {
                LOG.warn("(!) Skipped invalid module config {}", config);
                continue;
            }
            this.loadModule((Map<String, Object>) config);
        }
    }

    /**
     * 加载模块
     * 
     * @param res
     */
    protected final void loadModule(Map<String, Object> res) {
        ModuleDescribe entity = (ModuleDescribe) this.loader.load(res, this.assembler);
        String name = entity.getName();
        Module module = entity.getModule();
        addModule: {
            if (Strings.isEmpty(name)) {
                name = Helpers.getAliasName(module.getClass(), "Module$");
            } else if (module == null) {
                this.addDependencies(name, entity.getDependencies());
                break addModule;
            }
            this.addModule(name, module, entity.getDependencies());
        }
        Class<? extends Command> type = entity.getCommand();
        if (type != null) {
            this.addCommand(name, type);
        }
    }

    /**
     * 加载应用
     */
    @SuppressWarnings("unchecked")
    protected void loadApp() {
        /* 加载指定配置 */
        Object config;
        for (Entry<String, Module> entry : this.getModules().entrySet()) {
            /* 多配置文件 */
            for (Artifact artifact : Depository.getArtifacts(this.name + "-" + entry.getKey(), this.prober)) {
                config = this.loadResource(artifact);
                if (config != null) {
                    entry.getValue().load(config, this.assembler);
                }
            }
        }
        /* 加载全局配置 */
        config = this.loadResource(this.name);
        if (config instanceof Collection) {
            this.loadConfig((Collection<?>) config);
        } else if (config instanceof Map) {
            this.loadConfig((Map<String, Object>) config);
        }
    }

    /**
     * 加载配置
     * 
     * @param res
     */
    @SuppressWarnings("unchecked")
    protected final void loadConfig(Collection<?> res) {
        for (Object config : res) {
            if (config instanceof String) {
                config = this.loadResource((String) config);
            }
            if (!(config instanceof Map)) {
                LOG.warn("(!) Skipped invalid config {}", config);
                continue;
            }
            this.loadConfig((Map<String, Object>) config);
        }
    }

    /**
     * 加载配置
     * 
     * @param res
     */
    protected final void loadConfig(Map<String, Object> res) {
        String name;
        Object value;
        Module module;
        for (Entry<String, Object> entry : res.entrySet()) {
            name = entry.getKey();
            value = entry.getValue();
            if ((module = this.getModule(name)) != null) {
                module.load(value, this.assembler);
            } else if (LOG.isDebugEnabled()) {
                LOG.warn("(!) Skipped the unexpected module configuration [{} : {}]", name, value);
            }
        }

    }

    /**
     * 获取容器
     * 
     */
    public final Container getContainer() {
        return this.container;
    }

    /**
     * 获取模块
     * 
     * @return
     */
    public final Map<String, Module> getModules() {
        return this.container.getInstances(Module.class);
    }

    /**
     * 获取模块
     * 
     * @return
     */
    public final Module getModule() {
        InternalContext context = (InternalContext) Context.getAction();
        return context == null ? null : (Module) context.getInternal(Constants.CTX_MODULE);
    }

    /**
     * 获取模块
     * 
     * @param name
     * @return
     */
    public final Module getModule(String name) {
        return this.container.getInstance(Module.class, name);
    }

    /**
     * 获取模块
     * 
     * @param command
     * @return
     */
    public final Module getModule(BaseCommand command) {
        Class<?> type = command.getClass();
        Module module;
        if (type != BaseCommand.class) {
            module = this.getModule(type);
            if (module != null) return module;
        }
        module = getModule(command.getSource().getClass());
        if (module == null) {
            String name = command.getModule();
            if (Strings.isNotEmpty(name)) {
                module = this.getModule(name);
            }
        }
        return module;
    }

    /**
     * 获取模块
     * 
     * @param commandType
     * @return
     */
    public final Module getModule(Class<?> commandType) {
        if (!Command.class.isAssignableFrom(commandType) || Command.class.equals(commandType)) {
            return null;
        }
        String name = this.container.getInstance(String.class, commandType.getName() + SUFFIX_MODULE);
        if (Strings.isEmpty(name)) {
            return this.getModule(commandType.getSuperclass());
        }
        return this.getModule(name);
    }

    /**
     * 获取模块名
     * 
     * @param command
     * @return
     */
    public final String getModuleName(BaseCommand command) {
        Class<?> type = command.getClass();
        String moduleName;
        if (type != BaseCommand.class) {
            moduleName = this.getModuleName(type);
            if (Strings.isNotEmpty(moduleName)) return moduleName;
        }
        moduleName = getModuleName(command.getSource().getClass());
        if (Strings.isEmpty(moduleName)) {
            moduleName = command.getModule();
        }
        return moduleName;
    }

    /**
     * 获取模块名
     * 
     * @param type
     * @return
     */
    public final String getModuleName(Class<?> type) {
        if (!Command.class.isAssignableFrom(type) || Command.class.equals(type)) {
            return null;
        }
        String name = this.container.getInstance(String.class, type.getName() + SUFFIX_MODULE);
        if (name == null) {
            return this.getModuleName(type.getSuperclass());
        }
        return name;
    }

    /**
     * 添加探测器
     * 
     * @param prober
     */
    public final void addProber(Prober prober) {
        this.prober = ProberMulticaster.add(this.prober, prober);
    }

    /**
     * 添加依赖
     * 
     * @param name
     * @param dependencies
     */
    public final void addDependencies(String name, Object... dependencies) {
        this.addDependencies(name, Arrays.asList(dependencies));
    }

    /**
     * 添加依赖
     * 
     * @param name
     * @param dependencies
     */
    public final void addDependencies(String name, List<Object> dependencies) {
        this.dependency.addNode(name, dependencies);
    }

    /**
     * 添加模块
     * 
     * @param name
     * @param module
     * @param dependencies
     */
    public final void addModule(String name, Module module, Object... dependencies) {
        this.addModule(name, module, Arrays.asList(dependencies));
    }

    /**
     * 添加模块
     * 
     * @param name
     * @param module
     * @param dependencies
     */
    public final void addModule(String name, Module module, List<Object> dependencies) {
        if (LOG.isDebugEnabled()) {
            LOG.info("Binding [{}] module [{}]", name, module);
        }
        module = ConstantStrategy.set(this.container, Module.class, name, module);
        if (module != null) {
            LOG.warn("(!) Discard [{}] module [{}]", name, module);
        }
        this.addDependencies(name, dependencies);
    }

    /**
     * 添加命令
     * 
     * @param name
     * @param type
     */
    public final void addCommand(String name, Class<? extends Command> type) {
        String typeName = type.getName();
        if (LOG.isDebugEnabled()) {
            LOG.info("Binding [{}] command [{}]", name, typeName);
        }
        String oldName = ConstantStrategy.set(this.container, String.class, typeName + SUFFIX_MODULE, name);
        if (name.equals(oldName)) {
            LOG.warn("(!) Discard [{}] command [{}]", oldName, typeName);
        }
    }

    /**
     * 添加传音
     * 
     * @param airfone
     */
    public void addAirfone(Airfone airfone) {
        this.airfone = AirfoneMulticaster.add(this.airfone, airfone);
    }

    /**
     * 移除传音
     * 
     * @param airfone
     */
    public void remove(Airfone airfone) {
        this.airfone = AirfoneMulticaster.remove(this.airfone, airfone);
    }

    /**
     * 控制台执行入口
     * 
     * @param command
     */
    public final void execute(Command command) {
        this.preparer.prepare(command);
        InternalContext context = (InternalContext) Context.getAction();
        try {
            BaseCommand cmd = context.getCommand();
            Module module = getModule(cmd);
            if (module == null) {
                /* 控制操作 */
                this.operator.operate(cmd);
            } else {
                /* 模块执行 */
                context.setInternal(Constants.CTX_MODULE, module);
                module.execute(cmd);
            }
        } finally {
            this.preparer.cleanup();
        }
    }

    /**
     * 注销
     */
    public final synchronized void destroy() {
        if (this.status == Status.UNINITIALIZED || this.status == Status.DESTROYED) return;
        /* 传音确认 */
        if (this.airfone != null) {
            while (!this.airfone.ack(Airfone.ACTION_DESTROY)) Helpers.sleep(200l);
        }
        /* 模块注销 */
        Map<String, Module> modules = this.getModules();
        List<Object> nodes = new LinkedList<Object>(this.dependency.getNodes());
        Collections.reverse(nodes); // 倒置依赖关系
        for (Object node : nodes) {
            modules.get(node).destroy();
        }
        /* 容器注销 */
        this.container.destroy();
        if (this.hook != null) {
            Runtime.getRuntime().removeShutdownHook(this.hook);
            this.hook = null;
        }
        this.status = Status.DESTROYED;
    }

}
