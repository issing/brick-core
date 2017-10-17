package net.isger.brick.stub.model;

import java.util.Map;

import net.isger.raw.Artifact;
import net.isger.raw.Depository;
import net.isger.raw.StringRaw;
import net.isger.util.Sqls;
import net.isger.util.Strings;
import net.isger.util.anno.Affix;
import net.isger.util.anno.Alias;
import net.isger.util.reflect.Converter;

/**
 * 数据模型
 * 
 * @author issing
 *
 */
@Alias("t_brick_stub_model")
public class Model implements Cloneable {

    /** 标识 */
    @Affix("{length : 20, options : [1, 3]}")
    private String id;

    /** 标签 */
    @Affix("{length : 50}")
    private String label;

    /** 名称 */
    @Affix("{length : 50, options : [3, 4]}")
    private String name;

    /** 描述 */
    @Affix("{type : 'text'}")
    private String description;

    /** 纲要 */
    private transient Object schema;

    /** 字段（桥接多对多引用） */
    @Affix("{name : 't_brick_stub_item', type : 'reference', mode : 1, scale : 3, "
            + "value : {"
            + "source : {name : 'model_id', type : 'string', length : 20, options : 3, value : 't_brick_stub_model.id'}, "
            + "target : {name : 'meta_id', type : 'string', length : 20, options : 3, value : 't_brick_stub_meta.id'}, "
            + "metas : [{name : 'id', type : 'string', length : 20, options : [1, 3]}, "
            + "{name : 'label', type : 'string', length : 50}, "
            + "{name : 'name', type : 'string', length : 50}, "
            + "{name : 'description', type : 'text'}]}}")
    private Metas metas;

    static {
        Converter.addConversion(MetasConversion.CONVERSION);
    }

    public Model() {
        this.name = Sqls.getTableName(this.getClass(), "Model$");
        this.metas = new Metas();
    }

    public Model(Object[] metas) {
        this();
        this.metas(metas);
    }

    public Model(String name, Object... metas) {
        this(metas);
        if (Strings.isNotEmpty(name)) {
            this.name = name;
        }
    }

    public Model(String name, Metas metas) {
        this.name = name;
        this.metas = metas;
    }

    public boolean isModel() {
        return Model.class != this.getClass() || !modelName()
                .equalsIgnoreCase(Sqls.getTableName(Model.class));
    }

    public String modelId() {
        return id;
    }

    public void modelId(String id) {
        this.id = id;
    }

    public String modelLabel() {
        return label;
    }

    public void modelLabel(String label) {
        this.label = label;
    }

    public String modelName() {
        return name;
    }

    public void modelName(String name) {
        this.name = name;
    }

    public String modelDescription() {
        return description;
    }

    public void modelDescription(String description) {
        this.description = description;
    }

    public Object modelSchema() {
        return schema;
    }

    public void modelSchema(Object schema) {
        if (schema instanceof String) {
            Artifact artifact = Depository
                    .getArtifact(new StringRaw((String) schema));
            if (artifact != null) {
                schema = artifact.transform(Map.class);
            }
        }
        this.schema = schema;
    }

    public Metas metas() {
        return metas;
    }

    public void metas(Object[] metas) {
        Meta meta;
        boolean isBelong;
        for (Object instance : metas) {
            if (instance instanceof Object[]) {
                meta = new Meta((Object[]) instance);
                isBelong = true;
            } else if (instance instanceof Meta) {
                meta = ((Meta) instance).clone();
                isBelong = false;
            } else {
                throw new IllegalArgumentException(String.valueOf(instance));
            }
            meta(isBelong, meta);
        }
    }

    public void meta(Meta meta) {
        meta(false, null, meta);
    }

    public void meta(boolean isBelong, Meta meta) {
        meta(isBelong, null, meta);
    }

    public void meta(String name, Meta meta) {
        meta(false, name, meta);
    }

    public void meta(boolean isBelong, String name, Meta meta) {
        if (Strings.isEmpty(name)) {
            name = meta.getName();
        }
        if (isBelong) {
            meta.setCode(modelName() + "$" + name);
        }
        metas.put(name, meta);
    }

    public Meta meta(String name) {
        return metas.get(name);
    }

    public Object metaValue(String name) {
        return meta(name).getValue();
    }

    public void metaValue(String name, Object value) {
        meta(name).setValue(value);
    }

    public void metaValue(Map<?, ?> values) {
        for (String name : this.metas().names()) {
            this.metaValue(name, values.get(
                    values.containsKey(name) ? name : Sqls.toFieldName(name)));
        }
    }

    public void metaEmpty() {
        for (Meta meta : this.metas().values()) {
            meta.setValue(null);
        }
    }

    public Model clone() {
        Model model;
        try {
            model = (Model) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Failure to clone model", e);
        }
        model.metas = metas.clone();
        return model;
    }

    public static String getName(Object instance) {
        if (instance instanceof Model) {
            return ((Model) instance).modelName();
        } else if (instance instanceof String) {
            return (String) instance;
        }
        return Sqls.getTableName(instance instanceof Class ? (Class<?>) instance
                : instance.getClass());
    }

    public static Model create(Object instance) {
        if (instance instanceof Model) {
            return (Model) instance;
        } else if (instance instanceof String) {
            return new Model((String) instance);
        }
        return new Model(
                Sqls.getTableName(instance instanceof Class
                        ? (Class<?>) instance : instance.getClass()),
                Metas.getMetas(instance));
    }

}
