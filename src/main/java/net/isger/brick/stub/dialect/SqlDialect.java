package net.isger.brick.stub.dialect;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.isger.brick.stub.model.Field;
import net.isger.brick.stub.model.Model;
import net.isger.brick.stub.model.Option;
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
            public String describe(Field field) {
                return "VARCHAR(" + field.getLength() + ")";
            }

            public String describe(Option option, Object... extents) {
                String value = null;
                switch (option.getCode()) {
                case DEFAULT:
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
            public String describe(Field field) {
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
            public String describe(Field field) {
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
                switch (option.getCode()) {
                case DEFAULT:
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
                return "NOT NULL";
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
        addDescriber(STRING, STRING_DESCRIBER);
        addDescriber(NUMBER, NUMBER_DESCRIBER);
        addDescriber(DATE, DATE_DESCRIBER);
        addDescriber(DEFAULT, DEFAULT_DESCRIBER);
        addDescriber(PRIMARY, PRIMARY_DESCRIBER);
        addDescriber(NOTNULL, NOTNULL_DESCRIBER);
        addDescriber(UNIQUE, UNIQUE_DESCRIBER);
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
            return ((Model) table).getModelName();
        } else if (table instanceof String) {
            return (String) table;
        }
        return Sqls.getTableName(table instanceof Class ? (Class<?>) table
                : table.getClass());
    }

    protected String[] getColumnNames(Object table) {
        if (table instanceof Model) {
            return getColumnNames((Model) table);
        }
        String column;
        List<String> columns = new ArrayList<String>();
        BoundField field;
        for (List<BoundField> fields : Reflects
                .getBoundFields(table.getClass()).values()) {
            field = fields.get(0);
            column = field.getAlias();
            if (column == null) {
                column = Sqls.toColumnName(field.getName());
            }
            columns.add(column);
        }
        return columns.toArray(new String[columns.size()]);
    }

    protected String[] getColumnNames(Model table) {
        List<String> fieldNames = table.getFieldNames();
        return fieldNames.toArray(new String[fieldNames.size()]);
    }

    protected String[][] getColumnDescribes(Object table) {
        if (table instanceof Model) {
            return getColumnDescribes((Model) table);
        } else if (!(table instanceof Class)) {
            table = table.getClass();
        }
        List<List<BoundField>> fields = new ArrayList<List<BoundField>>(
                Reflects.getBoundFields((Class<?>) table).values());
        int size = fields.size();
        String[][] describes = new String[size][];
        for (int i = 0; i < size; i++) {
            describes[i] = getColumnDescribe(Field.create(fields.get(i).get(0)));
        }
        return describes;
    }

    protected String[][] getColumnDescribes(Model table) {
        List<String> fieldNames = table.getFieldNames();
        int fieldCount = fieldNames.size();
        String[][] describes = new String[fieldCount][];
        for (int i = 0; i < fieldCount; i++) {
            describes[i] = getColumnDescribe(table.getField(fieldNames.get(i)));
        }
        return describes;
    }

    protected String[] getColumnDescribe(Field field) {
        String describe = field.getType().toUpperCase();
        Describer describer = describers.get(describe);
        if (describer != null) {
            describe = describer.describe(field);
        }
        describe += getOptionDescribe(describer, field);
        return new String[] { field.getName(), describe };
    }

    protected String getOptionDescribe(Describer describer, Field field) {
        StringBuffer buffer = new StringBuffer(128);
        Describer optionDescriber;
        for (Option option : field.getOptions()) {
            optionDescriber = describers.get(option.getCode());
            if (optionDescriber != null) {
                buffer.append(" ").append(
                        optionDescriber.describe(option,
                                describer.describe(option, field, this)));
            }
        }
        return buffer.toString();
    }

    protected String getDateDescribe(Object value) {
        if (value instanceof Number) {
            Number number = (Number) value;
            if (number.intValue() == 0) {
                return "CURRENT_TIMESTAMP";
            }
            value = Dates.toString(new Date(number.longValue()),
                    Dates.PATTERN_COMMON);
        }
        return "TO_DATE('" + value + "', 'YYYY-MM-DD HH:mm:ss')";
    }

    protected Object[] getTableData(Object table) {
        if (table instanceof Model) {
            return getTableData((Model) table);
        }
        String column;
        Object value;
        List<String> columns = new ArrayList<String>();
        List<Object> row = new ArrayList<Object>();
        BoundField field;
        for (List<BoundField> fields : Reflects
                .getBoundFields(table.getClass()).values()) {
            field = fields.get(0);
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

    protected Object[] getTableData(Model table) {
        List<String> fieldNames = table.getFieldNames();
        int fieldCount = fieldNames.size();
        String column;
        Object value;
        List<String> columns = new ArrayList<String>();
        List<Object> row = new ArrayList<Object>();
        for (int i = 0; i < fieldCount; i++) {
            column = fieldNames.get(i);
            value = table.getField(column).getValue();
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
