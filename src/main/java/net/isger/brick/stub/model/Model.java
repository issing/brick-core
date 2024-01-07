package net.isger.brick.stub.model;

import java.util.HashMap;
import java.util.Map;

import net.isger.raw.Artifact;
import net.isger.raw.Depository;
import net.isger.raw.TextRaw;
import net.isger.util.Sqls;
import net.isger.util.Strings;
import net.isger.util.anno.Affix;
import net.isger.util.anno.Alias;
import net.isger.util.reflect.Converter;

/**
 * 数据模型
 * 
 * @author issing
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

    /** 纲要（表整体描述） */
    private transient Map<String, Object> schema;

    /** 字段（桥接多对多引用） */
    @Affix("{name : 't_brick_stub_item', type : 'reference', mode : 1, scale : 3, value : {" // 映射关系
            + "source : {name : 'model_id', type : 'string', length : 20, options : 3, value : 't_brick_stub_model.id'}, " // 源外键
            + "target : {name : 'meta_id', type : 'string', length : 20, options : 3, value : 't_brick_stub_meta.id'}, " // 目标外键
            + "metas : [{name : 'id', type : 'string', length : 20, options : [1, 3]}, " // 目标元：唯一标识
            + "{name : 'label', type : 'string', length : 50}, " // 目标元：标签
            + "{name : 'name', type : 'string', length : 50}, " // 目标元：名称
            + "{name : 'description', type : 'text'}]" // 目标元：描述
            + "}}")
    private Metas metas;

    static {
        Converter.addConversion(MetasConversion.getInstance());
    }

    public Model() {
        this.name = Sqls.getTableName(this.getClass(), "Model$");
        this.metas = Metas.getMetas(this);
        if (this.metas == null) {
            this.metas = new Metas();
        }
        this.schema = new HashMap<String, Object>();
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

    protected Model(Map<String, Object> schema, Metas metas) {
        this();
        if (metas != null) {
            this.metas = metas;
        }
        if (schema != null) {
            this.schema.putAll(schema);
        }
        this.name = Strings.empty(schemaValue("name"), this.name);
    }

    public boolean isModel() {
        return Model.class != this.getClass() || !modelName().equalsIgnoreCase(Sqls.getTableName(Model.class));
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

    public Object schemaValue(String name) {
        return schema.get(name);
    }

    public void schemaValue(String name, Object value) {
        schema.put(name, value);
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
            this.metaValue(name, values.get(values.containsKey(name) ? name : Strings.toFieldName(name)));
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
        return create(instance).modelName();
    }

    @SuppressWarnings("unchecked")
    public static Model create(Object instance) {
        if (instance instanceof Model) {
            return (Model) instance;
        } else if (instance instanceof String) {
            return new Model((String) instance);
        }
        Class<?> table = instance instanceof Class ? (Class<?>) instance : instance.getClass();
        Map<String, Object> schema = new HashMap<String, Object>();
        Affix affix = table.getAnnotation(Affix.class);
        if (affix != null) {
            Artifact artifact = Depository.getArtifact(new TextRaw(Strings.empty(affix.value())));
            if (artifact != null) {
                schema.putAll(artifact.transform(Map.class));
            }
        }
        if (Strings.isEmpty(schema.get("name"))) {
            schema.put("name", Sqls.getTableName(table));
        }
        return new Model(schema, Metas.getMetas(table));
    }

}
