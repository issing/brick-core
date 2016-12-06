package net.isger.brick.stub.dialect;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.isger.brick.stub.model.Meta;
import net.isger.brick.stub.model.Metas;
import net.isger.brick.stub.model.Model;
import net.isger.brick.stub.model.Option;
import net.isger.brick.stub.model.Options;
import net.isger.util.Dates;
import net.isger.util.Helpers;
import net.isger.util.Reflects;
import net.isger.util.Sqls;
import net.isger.util.Strings;
import net.isger.util.reflect.BoundField;
import net.isger.util.sql.SqlEntry;

/**
 * 标准SQL方言
 * 
 * @author issing
 *
 */
public class SqlDialect implements Dialect {

    private static final Describer STRING_DESCRIBER;

    private static final Describer NUMBER_DESCRIBER;

    private static final Describer DATE_DESCRIBER;

    private static final Describer PRIMARY_DESCRIBER;

    private static final Describer NOTNULL_DESCRIBER;

    private static final Describer UNIQUE_DESCRIBER;

    private static final Describer DEFAULT_DESCRIBER;

    /** 方言名称 */
    private String name;

    private Map<Object, Describer> describers;

    static {
        STRING_DESCRIBER = new DescriberAdapter() {
            public String describe(Meta field) {
                return "VARCHAR(" + field.getLength() + ")";
            }

            public String describe(Option option, Object... extents) {
                String value = null;
                switch (option.getType().intValue()) {
                case OPTION_DEFAULT:
                    Object optionValue = option.getValue();
                    if (optionValue != null) {
                        value = "'"
                                + optionValue.toString()
                                        .replaceAll("[']", "''") + "'";
                    }
                    break;
                }
                return value;
            }
        };
        NUMBER_DESCRIBER = new DescriberAdapter() {
            public String describe(Meta field) {
                StringBuffer describe = new StringBuffer("NUMBER");
                int length = field.getLength();
                if (length > 0) {
                    describe.append("(").append(length);
                    if ((length = field.getScale()) > 0) {
                        describe.append(", ").append(length);
                    }
                    describe.append(")");
                }
                return describe.toString();
            }
        };
        DATE_DESCRIBER = new DescriberAdapter() {
            public String describe(Meta field) {
                StringBuffer describe = new StringBuffer(64);
                switch (field.getScale()) {
                case SCALE_TIME:
                    describe.append("TIME");
                    break;
                case SCALE_TIMESTAMP:
                    describe.append("TIMESTAMP");
                    break;
                default:
                    describe.append("DATE");
                }
                return describe.toString();
            }

            public String describe(Option option, Object... extents) {
                String value;
                switch (option.getType().intValue()) {
                case OPTION_DEFAULT:
                    value = ((SqlDialect) extents[1]).getDateDescribe(option
                            .getValue());
                    break;
                default:
                    value = null;
                }
                return value;
            }
        };
        DEFAULT_DESCRIBER = new DescriberAdapter() {
            public String describe(Option option, Object... extents) {
                Object value = option.getValue();
                if (extents != null) {
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
                String value = option.getValue();
                return Strings.isEmpty(value) || Boolean.parseBoolean(value) ? "NOT NULL"
                        : "NULL";
            }
        };
        UNIQUE_DESCRIBER = new DescriberAdapter() {
            public String describe(Option option, Object... extents) {
                return "UNIQUE";
            }
        };
    }

    public SqlDialect() {
        describers = new HashMap<Object, Describer>();
        addDescriber(REFERENCE, new DescriberAdapter());
        addDescriber(STRING, STRING_DESCRIBER);
        addDescriber(NUMBER, NUMBER_DESCRIBER);
        addDescriber(DATE, DATE_DESCRIBER);
        addDescriber(OPTION_DEFAULT, DEFAULT_DESCRIBER);
        addDescriber(OPTION_PRIMARY, PRIMARY_DESCRIBER);
        addDescriber(OPTION_NOTNULL, NOTNULL_DESCRIBER);
        addDescriber(OPTION_UNIQUE, UNIQUE_DESCRIBER);
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

    /**
     * 获取创建实例
     * 
     * @param clazz
     * @return
     */
    public SqlEntry getCreateEntry(Object table) {
        return getCreateEntry(getTableName(table), getColumnDescribes(table));
    }

    public SqlEntry getCreateEntry(String table, String[][] describes) {
        StringBuffer sql = new StringBuffer(512);
        sql.append("CREATE TABLE ").append(table).append(" (");
        int count;
        for (String[] describe : describes) {
            count = describe.length;
            for (int i = 0; i < count; i++) {
                sql.append(describe[i]).append(" ");
            }
            sql.setLength(sql.length() - 1);
            sql.append(", ");
        }
        sql.setLength(sql.length() - 2);
        sql.append(")");
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
        StringBuffer sql = new StringBuffer(512);
        StringBuffer params = new StringBuffer(128);
        sql.append("INSERT INTO ").append(tableName).append("(");
        Object[] columns = (Object[]) gridData[0];
        int count = columns.length;
        for (int i = 0; i < count; i++) {
            sql.append(columns[i]).append(", ");
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
        StringBuffer sql = new StringBuffer(512);
        sql.append("DELETE FROM ").append(tableName).append(" WHERE 1 = 1");
        Object[] columns = (Object[]) gridData[0];
        int count = columns.length;
        if (gridData.length == 3 && Strings.isNotEmpty((String) gridData[2])) {
            throw new IllegalStateException(
                    "Unsupported feature in the current version");
        } else {
            for (int i = 0; i < count; i++) {
                sql.append(" AND ").append(columns[i]).append(" = ?");
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
        return getUpdateEntry(getTableName(newTable), getTableData(newTable),
                getTableData(oldTable));
    }

    public SqlEntry getUpdateEntry(String tableName, Object[] newGridData,
            Object[] oldGridData) {
        StringBuffer sql = new StringBuffer(512);
        sql.append("UPDATE ").append(tableName).append(" SET ");
        Object[] columns = (Object[]) newGridData[0];
        int count = columns.length;
        for (int i = 0; i < count; i++) {
            sql.append(columns[i]).append(" = ?, ");
        }
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE 1 = 1");
        columns = (String[]) oldGridData[0];
        count = columns.length;
        if (oldGridData.length == 3
                && Strings.isNotEmpty((String) oldGridData[2])) {
            throw new IllegalStateException(
                    "Unsupported feature in the current version");
        } else {
            for (int i = 0; i < count; i++) {
                sql.append(" AND ").append(columns[i]).append(" = ?");
            }
        }
        return new SqlEntry(sql.toString(), (Object[]) Helpers.getArray(
                newGridData[1], oldGridData[1]));
    }

    /**
     * 获取查询实例
     * 
     * @param table
     * @return
     */
    public SqlEntry getSearchEntry(Object table) {
        return getSearchEntry(getTableName(table), getColumnNames(table),
                getTableData(table));
    }

    public SqlEntry getSearchEntry(String tableName, Object[] columns,
            Object[] gridData) {
        StringBuffer sql = new StringBuffer(512);
        StringBuffer restrict = new StringBuffer(128);
        sql.append("SELECT ");
        int count = columns.length;
        for (int i = 0; i < count; i++) {
            sql.append(columns[i]).append(", ");
        }
        sql.setLength(sql.length() - 2);
        sql.append(" FROM ").append(tableName).append(" WHERE 1 = 1");
        columns = (String[]) gridData[0];
        count = columns.length;
        if (gridData.length == 3 && Strings.isNotEmpty((String) gridData[2])) {
            throw new IllegalStateException(
                    "Unsupported feature in the current version");
        } else {
            for (int i = 0; i < count; i++) {
                restrict.append(" AND ").append(columns[i]).append(" = ?");
            }
            sql.append(restrict);
        }
        return getSearchEntry(sql.toString(), (Object[]) gridData[1]);
    }

    public SqlEntry getSearchEntry(String sql, Object[] values) {
        Page page = getPage(values);
        if (page == null) {
            return new SqlEntry(sql, values);
        }
        Object[] target = new Object[values.length - 1];
        System.arraycopy(values, 0, target, 0, target.length);
        return getSearchEntry(page, sql, target);
    }

    protected SqlEntry getSearchEntry(Page page, String sql, Object[] values) {
        return new PageSql(page, sql, values);
    }

    protected Page getPage(Object[] values) {
        if (values != null && values.length > 0
                && values[values.length - 1] instanceof Page) {
            return (Page) values[values.length - 1];
        }
        return null;
    }

    public SqlEntry getRemoveEntry(Object table) {
        return new SqlEntry(new StringBuffer("DROP TABLE ").append(
                getTableName(table)).toString());
    }

    protected String getTableName(Object table) {
        if (table instanceof Model) {
            return ((Model) table).modelName();
        } else if (table instanceof String) {
            return (String) table;
        }
        return Sqls.getTableName(table instanceof Class ? (Class<?>) table
                : table.getClass());
    }

    protected String[] getColumnNames(Object table) {
        if (table instanceof Model && ((Model) table).isModel()) {
            return getColumnNames((Model) table);
        }
        String column;
        List<String> columns = new ArrayList<String>();
        Meta meta;
        BoundField field;
        for (List<BoundField> fields : Reflects
                .getBoundFields(table.getClass()).values()) {
            meta = Meta.createMeta(field = fields.get(0));
            if (meta.isReference()) {
                continue;
            }
            column = field.getAlias();
            if (column == null) {
                column = Sqls.toColumnName(field.getName());
            }
            columns.add(column);
        }
        return columns.toArray(new String[columns.size()]);
    }

    protected String[] getColumnNames(Model model) {
        List<String> names = model.metas().names();
        return names.toArray(new String[names.size()]);
    }

    protected String[][] getColumnDescribes(Object table) {
        Metas metas = Metas.createMetas(table);
        List<String[]> describes = new ArrayList<String[]>(metas.size());
        String[] decribe;
        for (Meta meta : metas.values()) {
            decribe = getColumnDescribe(meta);
            if (decribe != null) {
                describes.add(decribe);
            }
        }
        return describes.toArray(new String[describes.size()][]);
    }

    protected String[][] getColumnDescribes(Model model) {
        Metas metas = model.metas();
        List<String> names = metas.names();
        int fieldCount = names.size();
        String[][] describes = new String[fieldCount][];
        for (int i = 0; i < fieldCount; i++) {
            describes[i] = getColumnDescribe(metas.get(names.get(i)));
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
        Describer describer = describers.get(describe = meta.getType()
                .toUpperCase());
        if (describer != null) {
            describe = describer.describe(meta);
            if (Strings.isEmpty(describe)) {
                return null;
            }
        }
        describe += getOptionDescribe(describer, meta);
        return new String[] { meta.getName(), describe };
    }

    protected String getOptionDescribe(Describer describer, Meta meta) {
        StringBuffer buffer = new StringBuffer(128);
        Describer optionDescriber;
        for (Option option : meta.options().values()) {
            optionDescriber = describers.get(option.getType().intValue());
            if (optionDescriber != null) {
                buffer.append(" ").append(
                        optionDescriber.describe(
                                option,
                                describer == null ? null : describer.describe(
                                        option, meta, this)));
            }
        }
        return buffer.toString();
    }

    protected String getDateDescribe(Object value) {
        if (value == null) {
            return "CURRENT_TIMESTAMP";
        } else if (value instanceof Number) {
            Number number = (Number) value;
            if (number.intValue() == 0) {
                return "CURRENT_TIMESTAMP";
            }
            value = Dates.toString(
                    new Date(System.currentTimeMillis() + number.longValue()),
                    Dates.PATTERN_COMMON);
        }
        return "TO_DATE('" + value + "', 'YYYY-MM-DD HH:mm:ss')";
    }

    protected Object[] getTableData(Object table) {
        if (table instanceof Model && ((Model) table).isModel()) {
            return getTableData((Model) table);
        }
        String column;
        Object value;
        List<String> columns = new ArrayList<String>();
        List<Object> row = new ArrayList<Object>();
        Meta meta;
        BoundField field;
        for (List<BoundField> fields : Reflects
                .getBoundFields(table.getClass()).values()) {
            meta = Meta.createMeta(field = fields.get(0));
            if (REFERENCE.equals(meta.getType())) {
                // TODO 暂不处理引用类型
                continue;
            }
            column = field.getAlias();
            if (column == null) {
                column = Sqls.toColumnName(field.getName());
            }
            value = field.getValue(table);
            if (value != null) {
                columns.add(column);
                row.add(value);
            }
        }
        return new Object[] { columns.toArray(new String[columns.size()]),
                row.toArray() };
    }

    protected Object[] getTableData(Model model) {
        Metas metas = model.metas();
        List<String> names = metas.names();
        int metaCount = names.size();
        String column;
        Object value;
        List<String> columns = new ArrayList<String>();
        List<Object> row = new ArrayList<Object>();
        for (int i = 0; i < metaCount; i++) {
            column = names.get(i);
            value = metas.get(column).getValue();
            if (value != null) {
                columns.add(column);
                row.add(value);
            }
        }
        return new Object[] { columns.toArray(new String[columns.size()]),
                row.toArray() };
    }

    protected void addDescriber(Object type, Describer describer) {
        if (type instanceof String) {
            type = ((String) type).toUpperCase();
        }
        describers.put(type, describer);
    }

}
