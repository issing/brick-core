package net.isger.brick.bus;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.brick.Constants;
import net.isger.brick.core.AbstractModule;
import net.isger.brick.core.BaseCommand;
import net.isger.brick.inject.ConstantStrategy;
import net.isger.util.Asserts;
import net.isger.util.reflect.ClassAssembler;

/**
 * 总线模块
 * 
 * @author issing
 *
 */
public class BusModule extends AbstractModule {

    private static final String BUS = "bus";

    private static final Logger LOG;

    private transient volatile Status status;

    static {
        LOG = LoggerFactory.getLogger(BusModule.class);
    }

    public BusModule() {
        this.status = Status.UNINITIALIZED;
    }

    /**
     * 总线目标类型
     */
    public Class<?> getTargetClass() {
        return Bus.class;
    }

    /**
     * 总线实现类型
     */
    public Class<?> getImplementsClass() {
        return getImplementClass(BUS);
    }

    /**
     * 总线基本实现
     */
    public Class<?> getBaseClass() {
        return BaseBus.class;
    }

    /**
     * 创建总线
     */
    protected Object create(Class<?> clazz, Map<String, Object> res, ClassAssembler assembler) {
        Bus bus = (Bus) super.create(clazz, res, assembler);
        setBus(bus);
        return bus;
    }

    /**
     * 获取总线
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    protected final Bus getBus() {
        return this.container.getInstance((Class<Bus>) getTargetClass(), Constants.SYSTEM);
    }

    /**
     * 设置总线
     * 
     * @param bus
     */
    protected final void setBus(Bus bus) {
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
            LOG.info("(!) Discard bus [{}]", bus);
        }
    }

    /**
     * 创建默认总线
     */
    protected Bus create() {
        return (Bus) super.create();
    }

    public final Endpoint getEndpoint() {
        return (Endpoint) getInternal(Endpoint.BRICK_ENDPOINT);
    }

    public boolean hasReady() {
        return this.getBus().hasReady() && this.status == Status.INITIALIZED;
    }

    public Status getStatus() {
        return this.status;
    }

    public synchronized void initial() {
        if (!(status == Status.UNINITIALIZED || status == Status.DESTROYED)) return;
        this.status = Status.INITIALIZING;
        super.initial();
        /* 初始总线 */
        Bus bus = this.getBus();
        if (bus == null) {
            setBus(this.create());
            bus = this.getBus();
        }
        bus.initial();
        this.status = Status.INITIALIZED;
    }

    public final void execute(BaseCommand cmd) {
        BusCommand bcmd = (BusCommand) cmd;
        String name = bcmd.getEndpoint(); // 获取端点名
        if (name == null) {
            /* 模块操作 */
            super.execute(bcmd);
        } else {
            /* 端点操作 */
            Endpoint endpoint = this.getBus().getEndpoint(name);
            setInternal(Endpoint.BRICK_ENDPOINT, endpoint);
            endpoint.operate(bcmd);
        }
    }

    public synchronized void destroy() {
        if (this.status == Status.UNINITIALIZED || this.status == Status.DESTROYED) return;
        /* 注销总线 */
        Bus bus = this.getBus();
        if (bus != null) {
            bus.destroy();
        }
        super.destroy();
        this.status = Status.DESTROYED;
    }

}
