package net.isger.brick.stub.dialect;

import java.util.HashMap;
import java.util.Map;

import net.isger.util.Strings;
import net.isger.util.sql.Page;
import net.isger.util.sql.PageSql;

/**
 * PostgreSQL
 * 
 * @author issing
 *
 */
public class PostgresDialect extends SqlDialect {

    private static final String DRIVER_NAME = "org.postgresql.Driver";

    private static final String NUMERIC = "numeric";

    private static final Map<String, String> TYPES;

    static {
        TYPES = new HashMap<String, String>();
        TYPES.put(NUMBER.toUpperCase(), NUMERIC.toUpperCase());
        TYPES.put(DOUBLE.toUpperCase(), NUMERIC.toUpperCase());
        TYPES.put(DATETIME.toUpperCase(), TIMESTAMP.toUpperCase());
    }

    public boolean isSupport(String name) {
        return super.isSupport(name) || DRIVER_NAME.equals(name);
    }

    protected String type(String name) {
        String type = TYPES.get(name.toUpperCase());
        if (Strings.isEmpty(type)) {
            type = super.type(name);
        }
        return type;
    }

    public PageSql getSearchEntry(Page page, String sql, Object[] values) {
        return new PageSql(page, sql, values) {
            public String getWrapSql(String sql) {
                return sql + " limit ? offset ?";
            }

            public Object[] getWrapValues(Object[] values) {
                Page page = super.getPage();
                int valCount = 2;
                Object[] wrapValues = null;
                if (values != null) {
                    valCount += values.length;
                    wrapValues = new Object[valCount];
                    System.arraycopy(values, 0, wrapValues, 0, values.length);
                } else {
                    wrapValues = new Object[valCount];
                }
                wrapValues[valCount - 1] = (page.getStart() - 1)
                        * page.getLimit();
                wrapValues[valCount - 2] = page.getLimit();
                return wrapValues;
            }

        };
    }

}
