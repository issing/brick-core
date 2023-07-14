package net.isger.brick.stub.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.isger.brick.stub.dialect.Dialect;
import net.isger.brick.stub.dialect.SqlDialect;
import net.isger.raw.Artifact;
import net.isger.raw.Depository;
import net.isger.raw.TextRaw;
import net.isger.util.Asserts;
import net.isger.util.Reflects;
import net.isger.util.Strings;
import net.isger.util.anno.Affix;
import net.isger.util.anno.Alias;
import net.isger.util.reflect.BoundField;
import net.isger.util.reflect.Converter;

/**
 * 元数据结构
 * 
 * @author issing
 */
@Alias("t_brick_stub_meta")
public final class Meta implements Cloneable {

    /** 唯一标识 */
    public static final String ID = "id";

    /** 数据值 */
    public static final String VALUE = "value";

    /** 引用类型 */
    public static final String TYPE_REFERENCE = SqlDialect.REFERENCE;

    /** 数值模式 */
    public static final int MODE_VALUE_DATA = 0;

    /** 字典模式 */
    public static final int MODE_VALUE_DICT = 1;

    /** 数据元模式 */
    public static final int MODE_VALUE_META = 2;

    /** 嵌入 */
    public static final int MODE_REFERENCE_EMBED = 0;

    /** 桥接 */
    public static final int MODE_REFERENCE_BRIDGE = 1;

    /** 钩子 */
    public static final int MODE_REFERENCE_HOOK = 2;

    /** 一对一 */
    public static final int SCALE_REFERENCE_O2O = 0;

    /** 一对多 */
    public static final int SCALE_REFERENCE_O2M = 1;

    /** 多对一 */
    public static final int SCALE_REFERENCE_M2O = 2;

    /** 多对多 */
    public static final int SCALE_REFERENCE_M2M = 3;

    /** 标识 */
    @Affix("{length : 20, options : [1, 3]}")
    private String id;

    /** 编码 */
    @Affix("{length : 128, options : [3, 4]}")
    private String code;

    /** 标签 */
    @Affix("{length : 50}")
    private String label;

    /** 名称 */
    @Affix("{length : 50, options : 3}")
    private String name;

    /** 类型（value：值类型；reference：引用类型） */
    @Affix("{length : 30, options : 3}")
    private String type;

    /** 模式（值类型【0：数据值；1：字典值；2：数据元】 / 引用类型【0：嵌入；1：桥接；2：钩子】） */
    @Affix("{length : 2, options : 3}")
    private Number mode;

    /** 长度 */
    @Affix("{length : 5}")
    private Number length;

    /** 精度 （引用类型【0：一对一；1：一对多；2：多对一；3：多对多】） */
    @Affix("{length : 2}")
    private Number scale;

    /** 描述 */
    @Affix("{type : 'text'}")
    private String description;

    /** 选项 */
    @Affix("{type : 'reference'}")
    private Options options;

    /** 元值 */
    @Affix("{type : 'reference'}")
    private Object value;

    /** 绑定字段 */
    private transient BoundField field;

    static {
        Converter.addConversion(OptionsConversion.getInstance());
    }

    public Meta() {
    }

    public Meta(Object... args) {
        this.set(args);
    }

    protected void set(Object... args) {
        if (args == null) {
            return;
        }
        switch (args.length) {
        case 8:
            this.description = (String) args[7];
        case 7:
            this.mode = (Integer) args[6];
        case 6:
            this.code = (String) args[5];
        case 5:
            this.scale = (Integer) args[4];
        case 4:
            this.length = (Integer) args[3];
        case 3:
            this.type = (String) args[2];
        case 2:
            this.name = (String) args[1];
        case 1:
            this.label = (String) args[0];
        }
    }

    public boolean isReference() {
        return SqlDialect.REFERENCE.equalsIgnoreCase(type);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMode() {
        return mode == null ? 0 : mode.intValue();
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getLength() {
        return length == null ? 0 : length.intValue();
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getScale() {
        return scale == null ? -1 : scale.intValue();
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Options options() {
        if (options == null) {
            options = new Options();
        }
        return options;
    }

    public boolean hasOption() {
        return options().size() > 0;
    }

    public Object getValue() {
        // if (value == null) {
        // BaseCommand cmd = BaseCommand.getAction();
        // if (cmd != null) {
        // value = cmd.getParameter(name);
        // }
        // }
        return value;
    }

    public Object getValue(Object instance) {
        if (field != null && field.getField().getDeclaringClass().isInstance(instance)) {
            return field.getValue(instance);
        }
        return getValue();
    }

    public void setValue(Object value) {
        Class<?> type = Dialect.TYPES.get(getType());
        if (type != null) {
            value = Converter.convert(type, value);
        }
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public Model toModel() {
        if (isReference()) {
            switch (getMode()) {
            case MODE_REFERENCE_BRIDGE:
                if (value instanceof Map) {
                    Model model = new Model(name);
                    model.modelLabel(label);
                    model.modelDescription(description);
                    Map<String, Object> params = (Map<String, Object>) value;
                    List<Object> paramMetas = new ArrayList<Object>((List<Object>) params.get("metas"));
                    Object source = params.get("source");
                    if (source != null) {
                        paramMetas.add(source);
                    }
                    Object target = params.get("target");
                    if (target != null) {
                        paramMetas.add(target);
                    }
                    model.metas().set((Metas) MetasConversion.getInstance().convert(paramMetas));
                    return model;
                }
                break;
            }
        } else {
            switch (getMode()) {
            case MODE_VALUE_META:
                Model model = new Model(code);
                model.modelLabel(label);
                model.modelDescription(description);
                model.meta(true, MetaAssembler.createMeta(MetaAssembler.ID, Dialect.OPTION_NOTNULL, Dialect.OPTION_PRIMARY));
                Meta meta = new Meta(label, VALUE, type, length, scale, code, Meta.MODE_VALUE_DATA, description);
                meta.options().set(Dialect.OPTION_NOTNULL, Dialect.OPTION_UNIQUE);
                model.meta(true, meta);
                return model;
            }
        }
        return null;
    }

    public Meta clone() {
        Meta meta;
        try {
            meta = (Meta) super.clone();
        } catch (CloneNotSupportedException e) {
            throw Asserts.state("Failure to clone meta", e);
        }
        meta.options = options().clone();
        return meta;
    }

    public static Meta create(String affix) {
        Meta meta = new Meta();
        if (Strings.isNotEmpty(affix)) {
            Artifact artifact = Depository.getArtifact(new TextRaw(affix));
            if (artifact != null) {
                meta = artifact.transform(Meta.class);
            }
        }
        return meta;
    }

    public static Meta createMeta(String fieldName, Object... args) {
        return createMeta(Meta.class, fieldName, args);
    }

    public static Meta createMeta(Class<?> clazz, String fieldName, Object... args) {
        Meta meta = createMeta(Reflects.getBoundField(clazz, fieldName));
        meta.set(args);
        return meta;
    }

    public static Meta createMeta(BoundField field) {
        Meta meta = create(field.getAffix());
        if (Strings.isEmpty(meta.name)) {
            meta.name = field.getAlias();
            if (Strings.isEmpty(meta.name)) {
                meta.name = Strings.toColumnName(field.getName());
            }
        }
        if (Strings.isEmpty(meta.type)) {
            meta.type = field.getField().getType().getSimpleName();
        }
        meta.field = field;
        return meta;
    }
}
