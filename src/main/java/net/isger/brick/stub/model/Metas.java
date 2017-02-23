package net.isger.brick.stub.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.isger.util.Helpers;
import net.isger.util.Reflects;
import net.isger.util.Strings;
import net.isger.util.anno.Alias;
import net.isger.util.reflect.BoundField;

/**
 * 元数据容器
 * 
 * @author issing
 *
 */
public class Metas implements Cloneable {

    private Map<String, Object> metas;

    public Metas() {
        this.metas = new HashMap<String, Object>();
    }

    public Metas(List<Object> metas) {
        this();
        if (metas != null) {
            for (Object meta : metas) {
                if (meta instanceof Meta) {
                    add((Meta) meta);
                } else if (meta instanceof Object[]) {
                    add((Object[]) meta);
                }
            }
        }
    }

    void add(Object... args) {
        put((String) args[1], args);
    }

    void put(String name, Object... args) {
        this.metas.put(name, args);
    }

    public void add(Meta meta) {
        put(null, meta);
    }

    public void put(String name, Meta meta) {
        if (Strings.isEmpty(name)) {
            Class<?> clazz = meta.getClass();
            Alias alias = clazz.getAnnotation(Alias.class);
            name = alias == null || clazz == Meta.class ? meta.getName()
                    : alias.value();
            if (Strings.isEmpty(name)) {
                name = Strings.replaceIgnoreCase(clazz.getSimpleName(),
                        "Meta$", "");
            }
        }
        if (name.indexOf('.') != -1) {
            throw new IllegalArgumentException("Invalid meta alias " + name);
        }
        this.metas.put(name, meta);
    }

    public void set(Metas metas) {
        this.metas.clear();
        if (metas != null) {
            this.metas.putAll(metas.metas);
        }
    }

    public Meta get(String name) {
        Object meta = metas.get(name);
        if (meta instanceof Object[]) {
            meta = new Meta((Object[]) meta);
        }
        return (Meta) meta;
    }

    public List<String> names() {
        return new ArrayList<String>(metas.keySet());
    }

    public List<Meta> values() {
        List<Meta> metas = new ArrayList<Meta>();
        for (String name : this.metas.keySet()) {
            metas.add(get(name));
        }
        return metas;
    }

    public int size() {
        return this.metas.size();
    }

    public static Metas createMetas(Object table) {
        if (table instanceof Model && ((Model) table).isModel()) {
            return ((Model) table).metas();
        } else if (!(table instanceof Class)) {
            table = table.getClass();
        }
        List<List<BoundField>> fields = new ArrayList<List<BoundField>>(
                Reflects.getBoundFields((Class<?>) table).values());
        Metas metas = new Metas();
        for (List<BoundField> field : fields) {
            metas.add(Meta.createMeta(field.get(0)));
        }
        return metas;
    }

    public Metas clone() {
        Metas metas;
        try {
            metas = (Metas) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Failure to clone metas", e);
        }
        metas.metas = new HashMap<String, Object>();
        Object value;
        for (Entry<String, Object> entry : this.metas.entrySet()) {
            value = entry.getValue();
            if (value instanceof Meta) {
                value = ((Meta) value).clone();
            } else if (value instanceof Object[]) {
                value = Helpers.newArray(value);
            }
            metas.metas.put(entry.getKey(), value);
        }
        return metas;
    }

}
