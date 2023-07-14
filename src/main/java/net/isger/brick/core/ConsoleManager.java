package net.isger.brick.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.brick.Constants;
import net.isger.brick.auth.AuthPreparer;
import net.isger.brick.config.ModuleDescribe;
import net.isger.brick.inject.Container;
import net.isger.brick.inject.ContainerBuilder;
import net.isger.brick.inject.ContainerProvider;
import net.isger.brick.inject.ContainerProviderFactory;
import net.isger.brick.inject.InjectConductor;
import net.isger.brick.inject.InjectReserver;
import net.isger.brick.inject.Key;
import net.isger.raw.Prober;
import net.isger.raw.SuffixProber;
import net.isger.util.Asserts;
import net.isger.util.Callable;
import net.isger.util.Files;
import net.isger.util.Helpers;
import net.isger.util.Reflects;
import net.isger.util.Strings;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

/**
 * 控制台管理器
 * 
 * @author issing
 * 
 */
public class ConsoleManager {

    private static final Logger LOG;

    /** 实例手工锁 */
    private final Lock lock;

    /** 控制台名称 */
    private final String name;

    /** 控制台路径 */
    private final String path;

    /** 供应容器集合 */
    private final List<ContainerProvider> providers;

    /** 变更状态 */
    private volatile transient boolean isChanged;

    /** 引导容器 */
    private volatile transient Container bootstrap;

    /** 加载状态 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.BRICK_RELOAD)
    private boolean isReload;

    /** 控制台 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Console console;

    static {
        LOG = LoggerFactory.getLogger(ConsoleManager.class);
    }

    public ConsoleManager() {
        this(Constants.BRICK);
    }

    public ConsoleManager(String name) {
        this(name, null);
    }

    public ConsoleManager(String name, String path) {
        this.lock = new ReentrantLock();
        this.name = Strings.empty(name, Constants.BRICK);
        this.path = Strings.empty(path, Files.getBasePath());
        this.providers = new ArrayList<ContainerProvider>();
        loadContainerProviders();
    }

    /**
     * 加载供应容器
     */
    private void loadContainerProviders() {
        clearContainerProviders();
        // 服务形式加载（/META-INF/services/net.isger.brick.inject.ContainerProvider）
        ServiceLoader<ContainerProvider> loader = ServiceLoader.load(ContainerProvider.class, Reflects.getClassLoader(this));
        Iterator<ContainerProvider> iterator = loader.iterator();
        while (iterator.hasNext()) {
            addContainerProvider(iterator.next());
        }
    }

    /**
     * 获取供应容器
     * 
     * @return
     */
    public final List<ContainerProvider> getContainerProviders() {
        return new ArrayList<ContainerProvider>(providers);
    }

    /**
     * 设置供应容器
     * 
     * @param providers
     */
    public final void setContainerProviders(List<ContainerProvider> providers) {
        loadContainerProviders();
        if (providers != null) {
            for (ContainerProvider provider : providers) {
                addContainerProvider(provider);
            }
        }
    }

    /**
     * 新增供应容器
     * 
     * @param provider
     */
    public final void addContainerProvider(ContainerProvider provider) {
        lock.lock();
        try {
            if (!(provider == null || providers.contains(provider))) {
                providers.add(provider);
                isChanged = true;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 清空供应容器
     * 
     */
    public final void clearContainerProviders() {
        lock.lock();
        try {
            providers.clear();
            isChanged = true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 加载控制台
     */
    public final synchronized void load() {
        List<ContainerProvider> providers = getContainerProviders();
        if (console == null) {
            load(providers);
        } else if (isChanged || isReload) {
            if (isReload(providers)) {
                // 注销控制台及容器
                console.destroy();
                console = null;
                // 重新加载
                load(providers);
            }
            isChanged = false;
        }
    }

    /**
     * 检测加载
     * 
     * @return
     */
    public boolean isReload() {
        return isReload(getContainerProviders());
    }

    /**
     * 检测加载
     * 
     * @param providers
     * @return
     */
    private boolean isReload(List<ContainerProvider> providers) {
        boolean isReload = false;
        for (ContainerProvider provider : providers) {
            if (provider.isReload()) {
                if (LOG.isDebugEnabled()) {
                    LOG.info("Detected module provider [%s] needs to be reloaded", provider);
                }
                isReload = true;
                break;
            }
        }
        return isReload;
    }

    /**
     * 加载配置
     * 
     * @param providers
     */
    private void load(List<ContainerProvider> providers) {
        // 创建引导容器并初始化
        if (bootstrap != null) {
            bootstrap.destroy();
            bootstrap = null;
        }
        bootstrap = createBootstrap();
        bootstrap.initial();
        // 创建应用容器并初始化
        Container container = createContainer(Helpers.sort(new ArrayList<ContainerProvider>(providers)));
        container.initial();
        // 控制台管理器注入实例
        container.inject(this);
        Asserts.throwState(console != null, "The container does not provide effective supply for console");
        console.initial();
    }

    /**
     * 创建引导容器
     * 
     * @return
     */
    protected Container createBootstrap() {
        ContainerBuilder builder = new ContainerBuilder();
        builder.constant(Constants.BRICK_NAME, name);
        builder.constant(Constants.BRICK_PATH, path);
        builder.constant(Constants.BRICK_RELOAD, Boolean.FALSE);
        ContainerProviderFactory.getProvider().register(builder); // 引导供应器
        return builder.create(Constants.BOOTSTRAP);
    }

    /**
     * 创建应用容器
     * 
     * @param providers
     * @return
     */
    private Container createContainer(List<ContainerProvider> providers) {
        ContainerBuilder builder = createBuilder();
        // 注入引导实例至供应容器，然后向应用容器构建器注册供应资源
        for (ContainerProvider provider : providers) {
            bootstrap.inject(provider);
            provider.register(builder);
        }
        // 注入引导后备器
        builder.constant(InjectReserver.class, Constants.BOOTSTRAP, new InjectReserver() {
            public boolean contains(Key<?> key) {
                return bootstrap.contains(key.getType(), key.getName());
            }

            public <T> T alternate(Key<T> key, InjectConductor conductor) {
                return bootstrap.getInstance(key.getType(), key.getName(), conductor);
            }
        });
        return builder.create(Constants.SYSTEM);
    }

    /**
     * 创建应用容器构建器
     * 
     * @return
     */
    protected ContainerBuilder createBuilder() {
        ContainerBuilder builder = new ContainerBuilder();
        builder.factory(Prober.class, new Callable<Prober>() {
            public Prober call(Object... args) {
                return SuffixProber.create(((Container) args[0]).getInstance(String.class, Constants.BRICK_RAW));
            }
        });
        builder.factory(Preparer.class, AuthPreparer.class);
        builder.constant(Constants.BRICK_DESCRIBE, ModuleDescribe.class);
        return builder;
    }

    /**
     * 获取控制台
     * 
     * @return
     */
    public final Console getConsole() {
        synchronized (this) {
            if (console == null) {
                load(getContainerProviders());
            }
        }
        return console;
    }
}
