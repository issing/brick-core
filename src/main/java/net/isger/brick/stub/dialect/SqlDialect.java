package net.isger.brick.stub.dialect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.isger.brick.stub.model.Meta;
import net.isger.brick.stub.model.Metas;
import net.isger.brick.stub.model.Model;
import net.isger.brick.stub.model.Option;
import net.isger.brick.stub.model.Options;
import net.isger.brick.stub.model.OptionsConversion;
import net.isger.util.Dates;
import net.isger.util.Helpers;
import net.isger.util.Numbers;
import net.isger.util.Reflects;
import net.isger.util.Strings;
import net.isger.util.reflect.BoundField;
import net.isger.util.sql.PageSql;
import net.isger.util.sql.Pager;
import net.isger.util.sql.SqlEntry;

/**
 * 标准SQL方言
 * 
 * @author issing
 *
 */
public class SqlDialect implements Dialect {

    private final static Map<String, Model> MODELS;

    private final static Map<String, String> TYPES;

    private final Describer PRIMARY_DESCRIBER;

    private final Describer NOTNULL_DESCRIBER;

    private final Describer UNIQUE_DESCRIBER;

    private final Describer DEFAULT_DESCRIBER;

    /** 方言名称 */
    private String name;

    protected final Map<Object, Describer> describers;

    static {
        TYPES = new HashMap<String, String>();
        TYPES.put(DOUBLE.toUpperCase(), NUMBER.toUpperCase());
        TYPES.put(FLOAT.toUpperCase(), NUMBER.toUpperCase());
        TYPES.put(LONG.toUpperCase(), NUMBER.toUpperCase());
        TYPES.put(INTEGER.toUpperCase(), INT.toUpperCase());
        TYPES.put(SHORT.toUpperCase(), INT.toUpperCase());
        TYPES.put(BOOLEAN.toUpperCase(), INT.toUpperCase());
        MODELS = new HashMap<String, Model>();
    }

    {
        DEFAULT_DESCRIBER = new DescriberAdapter() {
            public String describe(Option option, Object... extents) {
                Object value = option.getValue();
                if (extents != null && extents[0] != null) {
                    value = extents[0].toString();
                }
                return "DEFAULT " + value;
            }
        };
        PRIMARY_DESCRIBER = new DescriberAdapter() {
            public String describe(Option option, Object... extents) {
                return "PRIMARY KEY";
            }
        };
        NOTNULL_DESCRIBER = new DescriberAdapter() {
            public String describe(Option option, Object... extents) {
                Object value = option.getValue();
                return Strings.isEmpty(value) || Helpers.toBoolean(value) ? "NOT NULL" : "NULL";
            }
        };
        UNIQUE_DESCRIBER = new DescriberAdapter() {
            public String describe(Option option, Object... extents) {
                StringBuffer buffer = new StringBuffer(64);
                buffer.append("UNIQUE");
                String name = option.getName();
                Object value = option.getValue();
                if (Strings.isEmpty(name) || Strings.isEmpty(value)) {
                    return buffer.toString();
                }
                String seal = seal();
                buffer.append(" KEY ").append(seal).append("UK_").append(name).append(seal);
                if (value instanceof String) {
                    value = Arrays.asList(value);
                }
                buffer.append("(").append(Strings.join(true, ",", seal, (List<?>) value)).append(")");
                return buffer.toString();
            }
        };
    }

    public SqlDialect() {
        describers = new HashMap<Object, Describer>();
        /* 类型描述器 */
        addDescriber(REFERENCE, getReferenceDescriber(REFERENCE));
        addDescriber(STRING, getStringDescriber(STRING));
        addDescriber(NUMBER, getNumberDescriber(NUMBER));
        addDescriber(INT, getNumberDescriber(INT));
        addDescriber(DATE, getDateDescriber(DATE));
        addDescriber(TIME, getDateDescriber(TIME));
        addDescriber(DATETIME, getDateDescriber(DATETIME));
        addDescriber(TIMESTAMP, getDateDescriber(TIMESTAMP));
        /* 选项描述器 */
        addDescriber(OPTION_DEFAULT, DEFAULT_DESCRIBER);
        addDescriber(OPTION_PRIMARY, PRIMARY_DESCRIBER);
        addDescriber(OPTION_NOTNULL, NOTNULL_DESCRIBER);
        addDescriber(OPTION_UNIQUE, UNIQUE_DESCRIBER);
    }

    public static Model getModel(String name) {
        synchronized (MODELS) {
            return MODELS.get(name);
        }
    }

    public String name() {
        if (Strings.isEmpty(name)) {
            name = Helpers.getAliasName(this.getClass(), "Dialect$");
        }
        return name;
    }

    public boolean isSupport(String name) {
        return name().equalsIgnoreCase(name);
    }

    protected String type(Meta meta, String name) {
        String key = name.toUpperCase();
        return Strings.empty(TYPES.get(key), key);
    }

    protected String seal() {
        return "\"";
    }

    /**
     * 获取创建实例
     * 
     * @param clazz
     * @return
     */
    public SqlEntry getCreateEntry(Object table) {
        Model model = Model.create(table);
        List<String[]> describes = getColumnDescribes(model.metas());
        Object optionsSchema = model.schemaValue("options");
        if (Strings.isNotEmpty(optionsSchema)) {
            Options options = (Options) OptionsConversion.getInstance().convert(optionsSchema);
            if (options != null) {
                describes.add(new String[] { null, getOptionDescribe(null, options, true) });
            }
        }
        /* 收录模型 */
        synchronized (MODELS) {
            if (!MODELS.containsKey(model.modelName())) {
                MODELS.put(model.modelName(), model.clone());
            }
        }
        return getCreateEntry(model.modelName(), describes.toArray(new String[describes.size()][]));
    }

    public SqlEntry getCreateEntry(String table, String[][] describes) {
        String seal = seal();
        StringBuffer sql = new StringBuffer(512);
        sql.append("CREATE TABLE ").append(table);
        if (describes.length > 0) {
            sql.append(" (");
            int count;
            for (String[] describe : describes) {
                count = describe.length;
                if (Strings.isNotEmpty(describe[0])) {
                    sql.append(seal).append(describe[0]).append(seal).append(" ");
                }
                for (int i = 1; i < count; i++) {
                    sql.append(describe[i]).append(" ");
                }
                sql.setLength(sql.length() - 1);
                sql.append(", ");
            }
            sql.setLength(sql.length() - 2);
            sql.append(")");
        }
        return new SqlEntry(sql.toString());
    }

    /**
     * 获取插入实例
     * 
     * @param table
     * @return
     */
    public SqlEntry getInsertEntry(Object table) {
        return getInsertEntry(getTableName(table), getTableData(table));
    }

    public SqlEntry getInsertEntry(String tableName, Object[] gridData) {
        String seal = seal();
        StringBuffer sql = new StringBuffer(512);
        StringBuffer params = new StringBuffer(128);
        sql.append("INSERT INTO ").append(tableName).append("(");
        Object[] columns = (Object[]) gridData[0];
        int count = columns.length;
        for (int i = 0; i < count; i++) {
            sql.append(seal).append(columns[i]).append(seal).append(", ");
            params.append("?, ");
        }
        sql.setLength(sql.length() - 2);
        sql.append(") VALUES (");
        params.setLength(params.length() - 2);
        sql.append(params);
        sql.append(")");
        return new SqlEntry(sql.toString(), (Object[]) gridData[1]);
    }

    /**
     * 获取删除实例
     * 
     * @param table
     * @return
     */
    public SqlEntry getDeleteEntry(Object table) {
        return getDeleteEntry(getTableName(table), getTableData(table));
    }

    public SqlEntry getDeleteEntry(String tableName, Object[] gridData) {
        String seal = seal();
        StringBuffer sql = new StringBuffer(512);
        sql.append("DELETE FROM ").append(tableName).append(" WHERE 1 = 1");
        Object[] columns = (Object[]) gridData[0];
        int count = columns.length;
        if (gridData.length == 3 && Strings.isNotEmpty((String) gridData[2])) {
            throw new IllegalStateException("Unsupported feature in the current version");
        } else {
            for (int i = 0; i < count; i++) {
                sql.append(" AND ").append(seal).append(columns[i]).append(seal).append(" = ?");
            }
        }
        return new SqlEntry(sql.toString(), (Object[]) gridData[1]);
    }

    /**
     * 获取修改实例
     * 
     * @param newTable
     * @param oldTable
     * @return
     */
    public SqlEntry getUpdateEntry(Object newTable, Object oldTable) {
        return getUpdateEntry(getTableName(newTable), getTableData(newTable), getTableData(oldTable));
    }

    public SqlEntry getUpdateEntry(String tableName, Object[] newGridData, Object[] oldGridData) {
        String seal = seal();
        StringBuffer sql = new StringBuffer(512);
        sql.append("UPDATE ").append(tableName).append(" SET ");
        Object[] columns = (Object[]) newGridData[0];
        int count = columns.length;
        for (int i = 0; i < count; i++) {
            sql.append(seal).append(columns[i]).append(seal).append(" = ?, ");
        }
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE 1 = 1");
        columns = (String[]) oldGridData[0];
        count = columns.length;
        if (oldGridData.length == 3 && Strings.isNotEmpty((String) oldGridData[2])) {
            throw new IllegalStateException("Unsupported feature in the current version");
        } else {
            for (int i = 0; i < count; i++) {
                sql.append(" AND ").append(seal).append(columns[i]).append(seal).append(" = ?");
            }
        }
        return new SqlEntry(sql.toString(), (Object[]) Helpers.newArray(newGridData[1], oldGridData[1]));
    }

    /**
     * 获取查询实例
     * 
     * @param table
     * @return
     */
    public SqlEntry getSearchEntry(Object table) {
        return getSearchEntry(getTableName(table), getColumnNames(table), getTableData(table));
    }

    public SqlEntry getSearchEntry(String tableName, Object[] columns, Object[] gridData) {
        String seal = seal();
        StringBuffer sql = new StringBuffer(512);
        StringBuffer restrict = new StringBuffer(128);
        sql.append("SELECT ");
        int count = columns.length;
        for (int i = 0; i < count; i++) {
            sql.append(seal).append(columns[i]).append(seal).append(", ");
        }
        sql.setLength(sql.length() - 2);
        sql.append(" FROM ").append(tableName).append(" WHERE 1 = 1");
        columns = (String[]) gridData[0];
        count = columns.length;
        if (gridData.length == 3 && Strings.isNotEmpty((String) gridData[2])) {
            throw new IllegalStateException("Unsupported feature in the current version");
        } else {
            for (int i = 0; i < count; i++) {
                restrict.append(" AND ").append(seal).append(columns[i]).append(seal).append(" = ?");
            }
            sql.append(restrict);
        }
        return getSearchEntry(sql.toString(), (Object[]) gridData[1]);
    }

    public SqlEntry getSearchEntry(String sql, Object[] values) {
        Pager pager = getPager(values);
        // if (pager != null) {
        // int size = values.length;
        // List<Object> pending = new ArrayList<Object>(size - 1);
        // if (size == 1) {
        // values = new Object[0];
        // } else {
        // for (Object value : values) {
        // if (value instanceof Pager) {
        // continue;
        // }
        // pending.add(value);
        // }
        // values = pending.toArray();
        // }
        // }
        return getSearchEntry(pager, sql, values);
    }

    private SqlEntry getSearchEntry(Pager pager, String sql, Object[] values) {
        if (pager == null) {
            return new SqlEntry(sql, values);
        }
        return createPageSql(pager, sql, values);
    }

    protected PageSql createPageSql(Pager pager, String sql, Object[] values) {
        return new PageSql(pager, sql, values);
    }

    protected Pager getPager(Object[] values) {
        return Helpers.getElement(values, Pager.class);
    }

    public SqlEntry getExistsEntry(Object table) {
        Model model = Model.create(table);
        /* 收录模型 */
        synchronized (MODELS) {
            if (!MODELS.containsKey(model.modelName())) {
                MODELS.put(model.modelName(), model.clone());
            }
        }
        return getExistsEntry(getTableName(model));
    }

    public SqlEntry getExistsEntry(String tableName) {
        StringBuffer sql = new StringBuffer(128);
        sql.append("SELECT count(1) FROM ").append(tableName).append(" WHERE 1 <> 1");
        return new SqlEntry(sql.toString());
    }

    public SqlEntry getRemoveEntry(Object table) {
        return new SqlEntry(new StringBuffer("DROP TABLE ").append(getTableName(table)).toString());
    }

    protected String getTableName(Object table) {
        return Model.getName(table);
    }

    protected String[] getColumnNames(Object table) {
        Metas metas = Metas.getMetas(table);
        List<String> columns = new ArrayList<String>();
        for (Meta meta : metas.values()) {
            if (meta.isReference()) {
                // TODO 暂不处理引用类型
                continue;
            }
            columns.add(meta.getName());
        }
        return columns.toArray(new String[columns.size()]);
    }

    protected List<String[]> getColumnDescribes(Metas metas) {
        List<String[]> describes = new ArrayList<String[]>(metas.size());
        String[] describe;
        for (Meta meta : metas.values()) {
            describe = getColumnDescribe(meta);
            if (describe != null) {
                describes.add(describe);
            }
        }
        return describes;
    }

    protected String[] getColumnDescribe(Meta meta) {
        String describe;
        switch (meta.getMode()) {
        case Meta.MODE_VALUE_META:
            Options options = meta.options();
            String description = meta.getDescription();
            meta = Meta.createMeta(Meta.ID, meta.getLabel(), meta.getName());
            meta.setDescription(description);
            meta.options().set(options);
        }
        describe = meta.getType().toUpperCase();
        String typeName = type(meta, describe);
        Describer describer = describers.get(typeName);
        describe: {
            describer: if (describer == null) {
                if (!describe.equalsIgnoreCase(typeName)) {
                    describer = describers.get(describe);
                    if (describer != null) {
                        break describer;
                    }
                }
                break describe;
            }
            describe = describer.describe(meta);
            /* 跳过非从属列描述 */
            if (Strings.isEmpty(describe)) {
                return null;
            }
        }
        describe += getOptionDescribe(describer, meta);
        return new String[] { meta.getName(), describe };
    }

    protected String getOptionDescribe(final Describer describer, final Meta meta) {
        SqlDialect self = this;
        return getOptionDescribe(describer == null ? null : new Describer() {
            public String describe(Option option, Object... extents) {
                return describer.describe(option, meta, self);
            }

            public String describe(Meta field) {
                return describer.describe(field);
            }
        }, meta.options(), false);
    }

    protected String getOptionDescribe(Describer describer, Options options, boolean hasComma) {
        StringBuffer sql = new StringBuffer(128);
        Describer optionDescriber;
        String describe;
        Number type;
        for (Option option : options.values()) {
            if ((type = option.getType()) == null) {
                continue;
            }
            optionDescriber = describers.get(type.intValue());
            if (optionDescriber != null && ((describe = optionDescriber.describe(option, describer == null ? null : describer.describe(option))) != null)) {
                sql.append(" ").append(describe);
                if (hasComma) sql.append(", ");
            }
        }
        if (hasComma && sql.length() > 0) sql.setLength(sql.length() - 2);
        return sql.toString();
    }

    protected String getDateDescribe(Object value) {
        if (value instanceof String) {
            try {
                value = Double.parseDouble((String) value);
            } catch (Exception e) {
                value = Dates.toString(value, Dates.PATTERN_COMMON);
            }
        } else if (value == null) {
            return "CURRENT_TIMESTAMP";
        }
        if (value instanceof Number) {
            Number pending = (Number) value;
            if (pending.intValue() == 0) {
                return "CURRENT_TIMESTAMP";
            }
            value = Dates.toString(new Date(System.currentTimeMillis() + pending.longValue()), Dates.PATTERN_COMMON);
        }
        return "TO_DATE('" + value + "', 'YYYY-MM-DD HH:mm:ss')";
    }

    protected Object[] getTableData(Object table) {
        String column;
        Object value;
        Metas metas = Metas.getMetas(table);
        List<String> columns = new ArrayList<String>();
        List<Object> row = new ArrayList<Object>();
        for (Meta meta : metas.values()) {
            if (meta.isReference()) {
                // TODO 暂不处理引用类型
                continue;
            }
            if ((value = meta.getValue(table)) != null) {
                column = meta.getName();
                if ((value = getColumnValue(column, value)) != null) {
                    columns.add(column);
                    row.add(value);
                }
            }
        }
        return new Object[] { columns.toArray(new String[columns.size()]), row.toArray() };
    }

    protected Object[] getTableData(Model model) {
        String column;
        Object value;
        Metas metas = model.metas();
        List<String> names = metas.names();
        int metaCount = names.size();
        List<String> columns = new ArrayList<String>();
        List<Object> row = new ArrayList<Object>();
        for (int i = 0; i < metaCount; i++) {
            column = names.get(i);
            value = metas.get(column).getValue();
            if (value != null) {
                columns.add(column);
                row.add(getColumnValue(column, value));
            }
        }
        return new Object[] { columns.toArray(new String[columns.size()]), row.toArray() };
    }

    protected Object getColumnValue(String name, Object value) {
        if (value instanceof Number || value instanceof Boolean || value instanceof CharSequence) {
            return value;
        } else if (value instanceof Class<?>) {
            return ((Class<?>) value).getName();
        }
        // TODO 根据名称规则获取对象属性（临时解决方案）
        String[] pending = name.split("[_]", 2);
        if (pending.length == 1) {
            return value;
        }
        BoundField field = Reflects.getBoundField(value.getClass(), pending[1]);
        if (field == null) {
            return value;
        }
        return getColumnValue(pending[1], field.getValue(value));
    }

    protected void addDescriber(Object type, Describer describer) {
        if (type instanceof String) {
            type = ((String) type).toUpperCase();
        }
        describers.put(type, describer);
    }

    protected Describer getReferenceDescriber(String name) {
        return new DescriberAdapter(name);
    }

    protected Describer getStringDescriber(String name) {
        return new StringDescriber();
    }

    protected Describer getNumberDescriber(String name) {
        return getNumberDescriber(name, true);
    }

    protected Describer getNumberDescriber(String name, boolean hasScale) {
        return new NumberDescriber(name, hasScale);
    }

    protected Describer getDateDescriber(String name) {
        return new DateDescriber(name);
    }

    protected class NumberDescriber extends DescriberAdapter {

        private boolean hasScale;

        public NumberDescriber(String name, boolean hasScale) {
            super(name);
            this.hasScale = hasScale;
        }

        public String describe(Meta field) {
            StringBuffer describe = new StringBuffer(type(field, this.getName()));
            int length = field.getLength();
            if (length > 0 && hasScale) {
                describe.append("(").append(length);
                if ((length = field.getScale()) > 0) {
                    describe.append(", ").append(length);
                }
                describe.append(")");
            }
            return describe.toString();
        }

        public String describe(Option option, Object... extents) {
            String value = null;
            switch (option.getType().intValue()) {
            case OPTION_DEFAULT:
                Object optionValue = option.getValue();
                if (optionValue == null) {
                    value = "0";
                } else {
                    value = String.valueOf(Numbers.parseInt(optionValue));
                }
                break;
            }
            return value;
        }
    }

    protected class StringDescriber extends DescriberAdapter {

        public String describe(Meta field) {
            int length = field.getLength();
            return length > 0 ? type(field, "VARCHAR") + "(" + length + ")" : type(field, "TEXT");
        }

        public String describe(Option option, Object... extents) {
            String value = null;
            switch (option.getType().intValue()) {
            case OPTION_DEFAULT:
                Object optionValue = option.getValue();
                if (optionValue != null) {
                    value = "'" + optionValue.toString().replaceAll("[']", "''") + "'";
                }
                break;
            }
            return value;
        }
    }

    protected class DateDescriber extends DescriberAdapter {

        public DateDescriber(String name) {
            super(name);
        }

        public String describe(Meta field) {
            StringBuffer describe = new StringBuffer(64);
            switch (field.getScale()) {
            case -1:
                describe.append(type(field, this.getName()));
                break;
            case SCALE_TIME:
                describe.append(type(field, TIME));
                break;
            case SCALE_DATETIME:
                describe.append(type(field, DATETIME));
                break;
            case SCALE_TIMESTAMP:
                describe.append(type(field, TIMESTAMP));
                break;
            default:
                describe.append(type(field, DATE));
            }
            return describe.toString();
        }

        public String describe(Option option, Object... extents) {
            String value;
            switch (option.getType().intValue()) {
            case OPTION_DEFAULT:
                value = ((SqlDialect) extents[1]).getDateDescribe(option.getValue());
                break;
            default:
                value = null;
            }
            return value;
        }
    }

}
