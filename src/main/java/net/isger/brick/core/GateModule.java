package net.isger.brick.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.brick.inject.ConstantStrategy;
import net.isger.brick.stub.StubCommand;
import net.isger.util.Asserts;
import net.isger.util.Helpers;
import net.isger.util.Strings;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;
import net.isger.util.reflect.ClassAssembler;

/**
 * 关卡模块
 * 
 * @author issing
 */
public class GateModule extends AbstractModule {

    private static final String GATE = "gate";

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(GateModule.class);
    }

    /**
     * 获取门目标类型
     */
    public Class<? extends Gate> getTargetClass() {
        return Gate.class;
    }

    /**
     * 获取门实现类型
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Gate> getImplementClass() {
        Class<? extends Gate> implClass = (Class<? extends Gate>) getImplementClass(GATE, null);
        if (implClass == null) {
            implClass = (Class<? extends Gate>) super.getImplementClass();
        }
        return implClass;
    }

    /**
     * 获取门基本实现
     * 
     * @return
     */
    protected Class<? extends Gate> getBaseClass() {
        return BaseGate.class;
    }

    /**
     * 创建目标实例（键值对实例集合）
     */
    protected List<Gate> create(Class<?> clazz, Map<String, Object> res, ClassAssembler assembler) {
        Map<String, Gate> gates = createGates(res, assembler);
        setGates(gates);
        return new ArrayList<Gate>(gates.values());
    }

    /**
     * 创建门
     */
    protected Gate create() {
        return (Gate) super.create();
    }

    /**
     * 创建门
     * 
     * @param res
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Gate> createGates(Map<String, Object> res, ClassAssembler assembler) {
        Map<String, Gate> result = new HashMap<String, Gate>();
        String name;
        Object config;
        /* 键值对配置方式 */
        for (Entry<String, Object> entry : res.entrySet()) {
            name = entry.getKey();
            config = entry.getValue();
            // 支持路径配置方式
            if (config instanceof String) {
                Object resource = console.loadResource((String) config);
                if (resource != null) {
                    config = resource;
                }
            }
            // 跳过键值对以外配置方式
            if (!(config instanceof Map)) {
                LOG.warn("(!) Skipped the unexpected gate configuration [{}]", config);
                continue;
            }
            result.put(name, createGate((Map<String, Object>) config, assembler));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    protected Gate createGate(Map<String, Object> res, ClassAssembler assembler) {
        return createGate((Class<? extends Gate>) getImplementClass(res), res, assembler);
    }

    protected Gate createGate(Class<? extends Gate> clazz, Map<String, Object> config, ClassAssembler assembler) {
        return (Gate) super.create(clazz, config, assembler);
    }

    /**
     * 添加门
     * 
     * @param gates
     * @return
     */
    protected Map<String, Gate> setGates(Map<String, Gate> gates) {
        Map<String, Gate> result = new HashMap<String, Gate>();
        String name;
        Gate gate;
        for (Entry<String, Gate> entry : gates.entrySet()) {
            gate = setGate(name = entry.getKey(), entry.getValue());
            if (gate != null) {
                result.put(name, gate);
            }
        }
        return result;
    }

    protected Gate setGate(String name, Gate gate) {
        Asserts.throwArgument(Strings.isNotEmpty(name) && gate != null, "The gate cannot be null or empty");
        if (LOG.isDebugEnabled()) {
            LOG.info("Binding [{}] gate [{}] for the module {}", name, gate, this);
        }
        return set(name, gate.getClass(), gate);
    }

    private Gate set(String name, Class<? extends Gate> gateType, Gate gate) {
        Class<? extends Gate> type = getTargetClass();
        if (type != gateType) {
            ConstantStrategy.set(container, gateType, name, gate);
        }
        gate = ConstantStrategy.set(container, type, name, gate);
        if (gate != null) {
            LOG.warn("(!) Discard [{}] gate [{}] in the module {}", name, gate, this);
        }
        return gate;
    }

    /**
     * 获取门
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public final Map<String, Gate> getGates() {
        return container.getInstances((Class<Gate>) getTargetClass());
    }

    public final Gate getGate() {
        return (Gate) getInternal(Gate.KEY_GATE);
    }

    @SuppressWarnings("unchecked")
    public Gate getGate(String name) {
        return container.getInstance((Class<Gate>) getTargetClass(), name);
    }

    /**
     * 删除门
     * 
     * @param name
     */
    protected Gate delGate(String name) {
        Gate gate = getGate(name);
        if (gate == null) {
            return null;
        }
        return set(name, gate.getClass(), null);
    }

    /**
     * 初始门模块
     */
    public void initial() {
        super.initial();
        /* 初始所有门 */
        for (Entry<String, Gate> entry : Helpers.sortByValue(getGates().entrySet())) {
            initial(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 初始指定门
     * 
     * @param domain
     * @param gate
     */
    protected void initial(String domain, Gate gate) {
        gate.initial();
    }

    /**
     * 执行门命令
     */
    public final void execute(BaseCommand cmd) {
        GateCommand gcmd = cmd instanceof GateCommand ? (GateCommand) cmd : GateCommand.cast(cmd);
        String domain = gcmd.getDomain(); // 获取域
        if (domain == null) {
            /* 模块操作 */
            super.execute(gcmd);
        } else {
            /* 关卡操作 */
            Gate gate = getGate(domain);
            Asserts.isNotNull(gate, "Unfound the specified domain [%s] in the module [%s], Check whether it is configured in the brick configuration file", domain, this.getClass().getName());
            setInternal(Gate.KEY_GATE, gate);
            gate.operate(gcmd);
        }
    }

    /**
     * 创建门
     * 
     * @param cmd
     */
    @Ignore(mode = Mode.INCLUDE)
    public void create(GateCommand cmd) {
        Map<String, Gate> gates = createGates(cmd.getParameter(), null);
        if (!cmd.getTransient()) {
            /* 容器托管门 */
            setGates(gates);
        }
        Gate gate;
        for (Entry<String, Gate> entry : gates.entrySet()) {
            container.inject(gate = entry.getValue());
            StubCommand.mockAction();
            try {
                gate.initial();
            } finally {
                StubCommand.realAction();
            }
        }
        cmd.setResult(gates);
    }

    /**
     * 移除门
     * 
     * @param cmd
     */
    @Ignore(mode = Mode.INCLUDE)
    public void remove(GateCommand cmd) {
        Map<String, Object> params = cmd.getParameter();
        String name;
        Gate gate;
        Map<String, Gate> result = new HashMap<String, Gate>();
        for (Entry<String, Object> entry : params.entrySet()) {
            gate = delGate(name = entry.getKey());
            if (gate != null) {
                result.put(name, gate);
            }
        }
        cmd.setResult(result);
    }

    /**
     * 注销门模块
     */
    public void destroy() {
        /* 注销所有门 */
        for (Entry<String, Gate> entry : getGates().entrySet()) {
            destroy(entry.getKey(), entry.getValue());
        }
        super.destroy();
    }

    /**
     * 注销指定门
     * 
     * @param key
     * @param gate
     */
    protected void destroy(String key, Gate gate) {
        gate.destroy();
    }

}
