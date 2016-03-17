package net.isger.brick.bus;

import java.util.Map;

import net.isger.brick.Constants;
import net.isger.brick.core.AbstractModule;
import net.isger.brick.inject.ConstantStrategy;
import net.isger.util.Asserts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusModule extends AbstractModule {

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(BusModule.class);
    }

    /**
     * 总线目标类型
     */
    public Class<?> getTargetClass() {
        Class<?> targetClass = super.getTargetClass();
        if (targetClass == null) {
            targetClass = Bus.class;
        } else {
            Asserts.argument(Bus.class.isAssignableFrom(targetClass),
                    "The bus " + targetClass + " must implement the "
                            + Bus.class);
        }
        return targetClass;
    }

    /**
     * 总线实现类型
     */
    public Class<?> getImplementClass() {
        return BaseBus.class;
    }

    /**
     * 创建总线
     */
    protected Object create(Class<?> clazz, Map<String, Object> res) {
        Bus bus = (Bus) super.create(clazz, res);
        setBus(bus);
        return bus;
    }

    /**
     * 获取总线
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Bus getBus() {
        return container.getInstance((Class<Bus>) getTargetClass(),
                Constants.SYSTEM);
    }

    /**
     * 设置总线
     * 
     * @param bus
     */
    protected void setBus(Bus bus) {
        Asserts.isNotNull(bus, "The bus cannot be null");
        if (LOG.isDebugEnabled()) {
            LOG.info("Achieve bus [{}]", bus);
        }
        Class<?> type = getTargetClass();
        setBus(type, Constants.SYSTEM, bus);
        if (type != bus.getClass()) {
            setBus(bus.getClass(), Constants.SYSTEM, bus);
        }
    }

    /**
     * 设置总线
     * 
     * @param type
     * @param name
     * @param bus
     */
    private void setBus(Class<?> type, String name, Object bus) {
        bus = ConstantStrategy.set(container, type, name, bus);
        if (LOG.isDebugEnabled() && bus != null) {
            LOG.info("Discard bus [{}]", bus);
        }
    }

    /**
     * 创建总线实例（暂不支持键值对以外配置方式）
     */
    protected Object create(Object res) {
        throw new IllegalArgumentException("Unexpected config " + res);
    }

    public void initial() {
        super.initial();
        /* 初始总线 */
        Bus bus = getBus();
        if (bus == null) {
            setBus(bus = new BaseBus());
            container.inject(bus);
        }
        bus.initial();
    }

    public final void execute() {
        String name = BusCommand.getAction().getEndpoint(); // 获取端点名
        if (name == null) {
            /* 模块操作 */
            operate();
        } else {
            /* 端点操作 */
            Endpoint endpoint = getBus().getEndpoint(name);
            setInternal(Endpoint.BRICK_ENDPOINT, endpoint);
            endpoint.operate();
        }
    }

    public void destroy() {
        /* 注销总线 */
        Bus bus = getBus();
        if (bus != null) {
            bus.destroy();
        }
        super.destroy();
    }

}
