package net.isger.brick.stub.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.isger.util.Reflects;
import net.isger.util.reflect.conversion.Conversion;

public class MetasConversion implements Conversion {

    public static final MetasConversion CONVERSION = new MetasConversion();

    private MetasConversion() {
    }

    public boolean isSupport(Class<?> type) {
        return Metas.class.equals(type);
    }

    public Object convert(Object value) {
        return convert(Metas.class, value);
    }

    public Object convert(Class<?> type, Object value) {
        Metas metas = new Metas();
        if (value instanceof Object[]) {
            value = Arrays.asList((Object[]) value);
        }
        if (value instanceof List) {
            for (Object i : (List<?>) value) {
                metas.add(createMeta(i));
            }
        } else {
            metas.add(createMeta(value));
        }
        return metas;
    }

    @SuppressWarnings("unchecked")
    private Meta createMeta(Object value) {
        Meta meta;
        if (value instanceof Object[]) {
            meta = new Meta((Object[]) value);
        } else if (value instanceof Map) {
            meta = Reflects
                    .newInstance(Meta.class, (Map<String, Object>) value);
        } else if (value instanceof Meta) {
            meta = (Meta) value;
        } else {
            throw new IllegalStateException("Unexpected class conversion for "
                    + value);
        }
        return meta;
    }

    public String toString() {
        return Metas.class.getName();
    }

}
