package net.isger.brick.stub.model;

import java.util.HashMap;
import java.util.Map;

import net.isger.brick.stub.dialect.Dialect;

public class MetaAssembler {

    public static final String ID = "id";

    public static final String INPUT_TIME = "input_time";

    private static final Map<Class<?>, MetaAssembler> ASSEMBLERS;

    public static final MetaAssembler base;

    private Metas metas;

    static {
        ASSEMBLERS = new HashMap<Class<?>, MetaAssembler>();
        base = new MetaAssembler();
        base.add("标识", ID, Dialect.STRING, 20, 0);
        base.add("入库时间", INPUT_TIME, Dialect.DATE);
    }

    protected MetaAssembler() {
        metas = new Metas();
        Class<?> clazz = this.getClass();
        if (getAssembler(clazz) != null) {
            throw new IllegalStateException("Exists meta assembler " + clazz);
        }
        ASSEMBLERS.put(clazz, this);
    }

    public void add(Object... args) {
        metas.add(args);
    }

    public void put(String name, Object... args) {
        metas.put(name, args);
    }

    public static final MetaAssembler getAssembler(Class<?> clazz) {
        return ASSEMBLERS.get(clazz);
    }

    public static Meta createMeta(String name, Object... options) {
        return createMeta(MetaAssembler.class, name, options);
    }

    public static final Meta createMeta(Class<?> clazz, String name,
            Object... options) {
        MetaAssembler assembler = getAssembler(clazz);
        if (assembler == null) {
            if (!ASSEMBLERS.containsKey(clazz)) {
                try {
                    Class.forName(clazz.getName());
                } catch (Exception e) {
                    ASSEMBLERS.put(clazz, null);
                }
                return createMeta(clazz, name, options);
            }
            return null;
        }
        Meta meta = assembler.metas.get(name);
        if (meta != null) {
            meta.options().set(options);
        }
        return meta;
    }

}
