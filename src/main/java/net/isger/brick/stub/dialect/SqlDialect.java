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
import net.isger.util.Strings;
import net.isger.util.reflect.BoundField;
import net.isger.util.sql.Page;
import net.isger.util.sql.PageSql;
import net.isger.util.sql.SqlEntry;

/**
 * 标准SQL方言
 * 
 * @author issing
 *
 */
public class SqlDialect implements Dialect {

    private final Describer PRIMARY_DESCRIBER;

    private final Describer NOTNULL_DESCRIBER;

    private final Describer UNIQUE_DESCRIBER;

    private final Describer DEFAULT_DESCRIBER;

    /** 方言名称 */
    private String name;

    private Map<Object, Describer> describers;

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
                String value = option.getValue();
                return Strings.isEmpty(value) || Boolean.parseBoolean(value)
                        ? "NOT NULL"
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
        addDescriber(REFERENCE, new DescriberAdapter(REFERENCE));
        addDescriber(STRING, new StringDescriber());
        addDescriber(NUMBER, new NumberDescriber(NUMBER));
        addDescriber(DOUBLE, new NumberDescriber(DOUBLE));
        addDescriber(DATE, new DateDescriber(DATE));
        addDescriber(TIME, new DateDescriber(TIME));
        addDescriber(DATETIME, new DateDescriber(DATETIME));
        addDescriber(TIMESTAMP, new DateDescriber(TIMESTAMP));
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

    protected String type(String name) {
        return name.toUpperCase();
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
            sql.append("\"").append(describe[0]).append("\" ");
            for (int i = 1; i < count; i++) {
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
            sql.append("\"").append(columns[i]).append("\", ");
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
                sql.append(" AND ").append("\"").append(columns[i])
                        .append("\" = ?");
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
            sql.append("\"").append(columns[i]).append("\" = ?, ");
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
                sql.append(" AND ").append("\"").append(columns[i])
                        .append("\" = ?");
            }
        }
        return new SqlEntry(sql.toString(),
                (Object[]) Helpers.newArray(newGridData[1], oldGridData[1]));
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
            sql.append("\"").append(columns[i]).append("\"").append(", ");
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

    public SqlEntry getExistsEntry(Object table) {
        return getExistsEntry(getTableName(table));
    }

    public SqlEntry getExistsEntry(String tableName) {
        StringBuffer sql = new StringBuffer(128);
        sql.append("SELECT count(1) FROM ").append(tableName)
                .append(" WHERE 1 <> 1");
        return new SqlEntry(sql.toString());
    }

    public SqlEntry getRemoveEntry(Object table) {
        return new SqlEntry(new StringBuffer("DROP TABLE ")
                .append(getTableName(table)).toString());
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

    protected String[][] getColumnDescribes(Object table) {
        Metas metas = Metas.getMetas(table);
        List<String[]> describes = new ArrayList<String[]>(metas.size());
        String[] describe;
        for (Meta meta : metas.values()) {
            describe = getColumnDescribe(meta);
            if (describe != null) {
                describes.add(describe);
            }
        }
        return describes.toArray(new String[describes.size()][]);
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
        Describer describer = describers.get(describe);
        describe: {
            if (describer == null) {
                describer = describers.get(describe = type(describe));
                if (describer == null) {
                    break describe;
                }
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

    protected String getOptionDescribe(Describer describer, Meta meta) {
        StringBuffer buffer = new StringBuffer(128);
        Describer optionDescriber;
        String describe;
        Number type;
        for (Option option : meta.options().values()) {
            if ((type = option.getType()) == null) {
                continue;
            }
            optionDescriber = describers.get(type.intValue());
            if (optionDescriber != null
                    && ((describe = optionDescriber.describe(option,
                            describer == null ? null
                                    : describer.describe(option, meta,
                                            this))) != null)) {
                buffer.append(" ").append(describe);
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
        return new Object[] { columns.toArray(new String[columns.size()]),
                row.toArray() };
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
        return new Object[] { columns.toArray(new String[columns.size()]),
                row.toArray() };
    }

    protected Object getColumnValue(String name, Object value) {
        if (value instanceof Number || value instanceof Boolean
                || value instanceof CharSequence) {
            return value;
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

    protected class NumberDescriber extends DescriberAdapter {

        public NumberDescriber(String name) {
            super(name);
        }

        public String describe(Meta field) {
            StringBuffer describe = new StringBuffer(type(this.getName()));
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

        public String describe(Option option, Object... extents) {
            String value = null;
            switch (option.getType().intValue()) {
            case OPTION_DEFAULT:
                Object optionValue = option.getValue();
                if (optionValue == null) {
                    value = "0";
                }
                break;
            }
            return value;
        }
    }

    protected class StringDescriber extends DescriberAdapter {

        public String describe(Meta field) {
            int length = field.getLength();
            return length > 0 ? type("VARCHAR") + "(" + length + ")"
                    : type("TEXT");
        }

        public String describe(Option option, Object... extents) {
            String value = null;
            switch (option.getType().intValue()) {
            case OPTION_DEFAULT:
                Object optionValue = option.getValue();
                if (optionValue != null) {
                    value = "'" + optionValue.toString().replaceAll("[']", "''")
                            + "'";
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
                describe.append(type(this.getName()));
                break;
            case SCALE_TIME:
                describe.append(type(TIME));
                break;
            case SCALE_DATETIME:
                describe.append(type(DATETIME));
                break;
            case SCALE_TIMESTAMP:
                describe.append(type(TIMESTAMP));
                break;
            default:
                describe.append(type(DATE));
            }
            return describe.toString();
        }

        public String describe(Option option, Object... extents) {
            String value;
            switch (option.getType().intValue()) {
            case OPTION_DEFAULT:
                value = ((SqlDialect) extents[1])
                        .getDateDescribe(option.getValue());
                break;
            default:
                value = null;
            }
            return value;
        }
    }

}
