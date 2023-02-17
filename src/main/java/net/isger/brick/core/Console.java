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

/**
 * 基础控制台
 * 
 * @author issing
 * 
 */
public class Console implements Constants, Manageable {

    private static final String SUFFIX_MODULE = ".module";

    private static final Logger LOG;

    /** 初始化状态 */
    private volatile transient boolean initialized;

    /** 配置加载器 */
    private transient Loader loader;

    /** 命令操作器 */
    private transient CommandOperator operator;

    @Ignore(mode = Mode.INCLUDE)
    @Alias(SYSTEM)
    private Container container;

    @Ignore(mode = Mode.INCLUDE)
    @Alias(SYSTEM)
    private PlaceholderConfigurer configurer;

    @Ignore(mode = Mode.INCLUDE)
    @Alias(BRICK_NAME)
    private String name;

    @Ignore(mode = Mode.INCLUDE)
    private Prober prober;

    @Ignore(mode = Mode.INCLUDE)
    private Preparer preparer;

    /** 模块依赖 */
    private transient Dependency dependency;

    /** 注销钩子 */
    private transient Thread hook;

    private transient List<Airfone> airfones;

    static {
        LOG = LoggerFactory.getLogger(Console.class);
    }

    public Console() {
        operator = new CommandOperator(this);
        preparer = new Preparer();
        dependency = new Dependency();
        airfones = new ArrayList<Airfone>();
    }

    /**
     * 初始
     */
    public final synchronized void initial() {
        if (initialized) {
            return;
        }
        // 注销钩子
        Runtime.getRuntime().addShutdownHook(hook = new Thread(new Runnable() {
            public void run() {
                hook = null;
                destroy();
            }
        }));
        /* 初始内核 */
        Class<?> describeClass = container.getInstance(Class.class, BRICK_DESCRIBE);
        Asserts.isAssignable(ModuleDescribe.class, describeClass, "Invalid module describe [%s] in container", describeClass);
        loader = new BaseLoader(describeClass);
        loadKernel();
        loadApp();
        // 初始模块
        try {
            Map<String, Module> modules = getModules();
            for (Object node : dependency.getNodes()) {
                modules.get(node).initial();
            }
        } catch (Throwable e) {
            throw Asserts.state("Failure to initial module", e);
        }
        initialized = true;
    }

    /**
     * 加载内核
     * 
     */
    protected void loadKernel() {
        /* 默认内核 */
        setupModule(MOD_CACHE, new CacheModule(), CacheCommand.class); // 缓存模块
        setupModule(MOD_AUTH, new AuthModule(), AuthCommand.class, MOD_CACHE); // 认证模块
        setupModule(MOD_TASK, new TaskModule(), TaskCommand.class, MOD_AUTH); // 任务模块
        setupModule(MOD_BUS, new BusModule(), BusCommand.class, MOD_TASK); // 总线模块
        setupModule(MOD_STUB, new StubModule(), StubCommand.class, MOD_AUTH); // 存根模块
        /* 加载内核 */
        if (!Strings.matchsIgnoreCase(name, BRICK)) {
            loadKernel(BRICK); // 加载默认配置
        }
        loadKernel(name);
    }

    /**
     * 安装模块
     *
     * @param name
     * @param module
     */
    protected final void setupModule(String name, Module module) {
        setupModule(name, module, null);
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
        if (getModule(name) == null) {
            addModule(name, module);
        }
        addDependencies(name, dependencies);
        if (commandClass != null) {
            addCommand(name, commandClass);
        }
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
        for (Artifact artifact : Depository.getArtifacts(name + "-config", prober)) {
            config = loadResource(artifact);
            if (config instanceof Collection) {
                loadConstants((Collection<?>) config);
            } else if (config instanceof Map) {
                loadConstants((Map<String, Object>) config);
            }
        }
        /* 加载内核配置 */
        for (Artifact artifact : Depository.getArtifacts(name + "-kernel", prober)) {
            config = loadResource(artifact);
            if (config instanceof Collection) {
                loadModule((Collection<?>) config);
            } else if (config instanceof Map) {
                loadModule((Map<String, Object>) config);
            }
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
                config = loadResource(config + "-constants");
            }
            /* 跳过非键值对集合 */
            if (!(config instanceof Map)) {
                LOG.warn("(!) Skipped invalid constants config {}", config);
                continue;
            }
            /* 加载键值对集合常量 */
            loadConstants((Map<String, Object>) config);
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
            if (value instanceof Map) {
                value = BaseLoader.toLoad(value);
            }
            type = value.getClass();
            if (Map.class.isAssignableFrom(type)) {
                type = Map.class;
            } else if (List.class.isAssignableFrom(type)) {
                type = List.class;
            } else if (CharSequence.class.isAssignableFrom(type)) {
                Class<?> clazz = Reflects.getClass(value);
                if (clazz != null) {
                    ConstantStrategy.set(container, Class.class, entry.getKey(), clazz);
                }
            }
            ConstantStrategy.set(container, type, entry.getKey(), value);
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
                config = loadResource(name + "-module");
                if (!(config instanceof Map)) {
                    LOG.warn("(!) Skipped invalid module config {}", config);
                    continue;
                }
                params = new HashMap<String, Object>((Map<String, Object>) config);
                if (!params.containsKey(CONF_NAME)) {
                    params.put(CONF_NAME, name);
                }
            } else if (!(config instanceof Map)) {
                LOG.warn("(!) Skipped invalid module config {}", config);
                continue;
            }
            loadModule((Map<String, Object>) config);
        }
    }

    /**
     * 加载模块
     * 
     * @param res
     */
    protected final void loadModule(Map<String, Object> res) {
        ModuleDescribe entity = (ModuleDescribe) loader.load(res, null);
        String name = entity.getName();
        Module module = entity.getModule();
        addModule: {
            if (Strings.isEmpty(name)) {
                name = Helpers.getAliasName(module.getClass(), "Module$");
            } else if (module == null) {
                addDependencies(name, entity.getDependencies());
                break addModule;
            }
            addModule(name, module, entity.getDependencies());
        }
        Class<? extends Command> type = entity.getCommand();
        if (type != null) {
            addCommand(name, type);
        }
    }

    /**
     * 加载应用
     */
    @SuppressWarnings("unchecked")
    protected void loadApp() {
        /* 加载指定配置 */
        Object config;
        for (Entry<String, Module> entry : getModules().entrySet()) {
            /* 多配置文件 */
            for (Artifact artifact : Depository.getArtifacts(name + "-" + entry.getKey(), prober)) {
                config = loadResource(artifact);
                if (config != null) {
                    entry.getValue().load(config, null);
                }
            }
        }
        /* 加载全局配置 */
        config = loadResource(name);
        if (config instanceof Collection) {
            loadConfig((Collection<?>) config);
        } else if (config instanceof Map) {
            loadConfig((Map<String, Object>) config);
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
                config = loadResource((String) config);
            }
            if (!(config instanceof Map)) {
                LOG.warn("(!) Skipped invalid config {}", config);
                continue;
            }
            loadConfig((Map<String, Object>) config);
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
            if ((module = getModule(name)) != null) {
                module.load(value, null);
            } else if (LOG.isDebugEnabled()) {
                LOG.warn("(!) Skipped the unexpected module configuration [{} : {}]", name, value);
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
        return loadResource(Depository.getArtifact(res, prober));
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
            if (configurer != null) {
                resource = configurer.displace(resource);
            }
        }
        return resource;
    }

    /**
     * 准备就绪
     *
     * @return
     */
    public final boolean hasReady() {
        return initialized;
    }

    /**
     * 获取容器
     * 
     */
    public final Container getContainer() {
        return container;
    }

    /**
     * 获取模块
     * 
     * @return
     */
    public final Map<String, Module> getModules() {
        return container.getInstances(Module.class);
    }

    /**
     * 获取模块
     * 
     * @return
     */
    public final Module getModule() {
        return (Module) ((InternalContext) Context.getAction()).getInternal(Constants.CTX_MODULE);
    }

    /**
     * 获取模块
     * 
     * @param name
     * @return
     */
    public final Module getModule(String name) {
        return container.getInstance(Module.class, name);
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
            module = getModule(type);
            if (module != null) {
                return module;
            }
        }
        module = getModule(command.getSource().getClass());
        if (module == null) {
            String name = command.getModule();
            if (Strings.isNotEmpty(name)) {
                module = getModule(name);
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
        String name = container.getInstance(String.class, commandType.getName() + SUFFIX_MODULE);
        if (Strings.isEmpty(name)) {
            return getModule(commandType.getSuperclass());
        }
        return getModule(name);
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
            moduleName = getModuleName(type);
            if (moduleName != null) {
                return moduleName;
            }
        }
        moduleName = getModuleName(command.getSource().getClass());
        if (moduleName == null) {
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
        String name = container.getInstance(String.class, type.getName() + SUFFIX_MODULE);
        if (name == null) {
            return getModuleName(type.getSuperclass());
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

    public final void addDependencies(String name, Object... dependencies) {
        addDependencies(name, Arrays.asList(dependencies));
    }

    public final void addDependencies(String name, List<Object> dependencies) {
        dependency.addNode(name, dependencies);
    }

    /**
     * 添加模块
     * 
     * @param name
     * @param module
     * @param dependencies
     */
    public final void addModule(String name, Module module, Object... dependencies) {
        addModule(name, module, Arrays.asList(dependencies));
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
        module = ConstantStrategy.set(container, Module.class, name, module);
        if (module != null) {
            LOG.warn("(!) Discard [{}] module [{}]", name, module);
        }
        addDependencies(name, dependencies);
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
        String oldName = ConstantStrategy.set(container, String.class, typeName + SUFFIX_MODULE, name);
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
        if (!(airfone == null || airfones.contains(airfone))) {
            airfones.add(airfone);
        }
    }

    /**
     * 移除传音
     * 
     * @param airfone
     */
    public void remove(Airfone airfone) {
        if (airfone != null) {
            airfones.remove(airfone);
        }
    }

    /**
     * 控制台执行入口
     * 
     * @param command
     */
    public final void execute(Command command) {
        preparer.prepare(command);
        InternalContext context = (InternalContext) Context.getAction();
        try {
            BaseCommand cmd = context.getCommand();
            Module module = getModule(cmd);
            if (module == null) {
                /* 控制操作 */
                operator.operate(cmd);
            } else {
                /* 模块执行 */
                context.setInternal(Constants.CTX_MODULE, module);
                module.execute(cmd);
            }
        } finally {
            preparer.cleanup();
        }
    }

    /**
     * 注销
     */
    public final synchronized void destroy() {
        if (!initialized) {
            return;
        }
        /* 传音确认 */
        for (Airfone airfone : airfones) {
            airfone.ack(Airfone.ACTION_DESTROY);
        }
        /* 模块注销 */
        Map<String, Module> modules = getModules();
        List<Object> nodes = new LinkedList<Object>(dependency.getNodes());
        Collections.reverse(nodes);
        for (Object node : nodes) {
            modules.get(node).destroy();
        }
        /* 容器注销 */
        container.destroy();
        if (hook != null) {
            Runtime.getRuntime().removeShutdownHook(hook);
            hook = null;
        }
        initialized = false;
    }

}
