package net.isger.brick.stub.model;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.isger.util.Asserts;
import net.isger.util.Reflects;
import net.isger.util.reflect.ClassAssembler;
import net.isger.util.reflect.conversion.Conversion;

public class MetasConversion implements Conversion {

    public static final MetasConversion CONVERSION = new MetasConversion();

    private MetasConversion() {
    }

    public boolean isSupport(Type type) {
        return Metas.class.equals(Reflects.getRawClass(type));
    }

    public Object convert(Object value) {
        return convert(Metas.class, value, null);
    }

    public Object convert(Type type, Object value, ClassAssembler assembler) {
        Metas metas = new Metas();
        if (value instanceof Object[]) {
            value = Arrays.asList((Object[]) value);
        }
        if (value instanceof List) {
            for (Object i : (List<?>) value) {
                metas.add(createMeta(i, assembler));
            }
        } else {
            metas.add(createMeta(value, assembler));
        }
        return metas;
    }

    @SuppressWarnings("unchecked")
    private Meta createMeta(Object value, ClassAssembler assembler) {
        Meta meta;
        if (value instanceof Object[]) {
            meta = new Meta((Object[]) value);
        } else if (value instanceof Map) {
            meta = Reflects.newInstance(Meta.class, (Map<String, Object>) value, assembler);
        } else if (value instanceof Meta) {
            meta = (Meta) value;
        } else {
            throw Asserts.state("Unexpected class conversion for %s", value);
        }
        return meta;
    }

    public String toString() {
        return Metas.class.getName();
    }

}
