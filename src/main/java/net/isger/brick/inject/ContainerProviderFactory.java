package net.isger.brick.inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.brick.bind.BrickCoreBinder;
import net.isger.util.Asserts;

/**
 * 供应容器工厂
 * 
 * @author issing
 *
 */
public class ContainerProviderFactory {

    private static final String BINDER = "net.isger.brick.bind.BrickCoreBinder";

    private static final int SUCCESS = 1;

    private static final int UNKNOWN = 2;

    private static final int FAILURE = 3;

    private static final Logger LOG;

    private static final ContainerProvider NOP_PROVIDER;

    private static int initialized;

    static {
        LOG = LoggerFactory.getLogger(ContainerProviderFactory.class);
        NOP_PROVIDER = new ContainerProvider() {
            public boolean isReload() {
                return false;
            }

            public void register(ContainerBuilder builder) {
            }
        };
        bind();
    }

    private ContainerProviderFactory() {
    }

    /**
     * 核心供应容器绑定
     */
    private static void bind() {
        try {
            LOG.info("Complete static binder [{}]", BrickCoreBinder.getBinder());
            initialized = SUCCESS;
            return;
        } catch (NoClassDefFoundError e) {
            String msg = e.getMessage();
            if (msg == null || msg.indexOf(BINDER) == -1 && msg.indexOf(BINDER.replaceAll("[.]", "/")) == -1) {
                initialized = FAILURE;
                throw e;
            }
            if (LOG.isDebugEnabled()) {
                LOG.warn("(!) Failed to load class [{}]", BINDER);
                LOG.warn("(!) Defaulting to no-operation (NOP) provider implementation");
            }
            initialized = UNKNOWN;
        } catch (Exception e) {
            initialized = FAILURE;
            throw Asserts.state("Unexpected bind failure", e.getCause());
        }
    }

    /**
     * 获取供应容器
     * 
     * @return
     */
    public static ContainerProvider getProvider() {
        ContainerProvider provider;
        switch (initialized) {
        case SUCCESS:
            provider = BrickCoreBinder.getBinder().getProvider();
            if (provider == null) {
                throw Asserts.state("The bound provider cannot be null");
            }
            break;
        default:
            provider = NOP_PROVIDER;
        }
        return provider;
    }

}
