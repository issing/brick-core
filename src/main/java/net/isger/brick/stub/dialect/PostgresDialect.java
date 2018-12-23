package net.isger.brick.stub.dialect;

import java.util.HashMap;
import java.util.Map;

import net.isger.brick.stub.model.Meta;
import net.isger.brick.stub.model.Option;
import net.isger.util.Helpers;
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

    private static final String BIGINT = "bigint";

    private static final String BOOL = "bool";

    private static final Map<String, String> TYPES;

    static {
        TYPES = new HashMap<String, String>();
        TYPES.put(NUMBER.toUpperCase(), NUMERIC.toUpperCase());
        TYPES.put(DATETIME.toUpperCase(), TIMESTAMP.toUpperCase());
    }

    public PostgresDialect() {
        addDescriber(LONG, getNumberDescriber(BIGINT, false));
        addDescriber(INT, getNumberDescriber(INT, false));
        addDescriber(BOOLEAN,
                new DescriberDelegate(getNumberDescriber(BOOL, false)) {
                    public String describe(Option option, Object... extents) {
                        String value = super.describe(option, extents);
                        switch (option.getType().intValue()) {
                        case OPTION_DEFAULT:
                            value = String.valueOf(Helpers.toBoolean(value));
                            break;
                        }
                        return value;
                    }
                });
    }

    public boolean isSupport(String name) {
        return super.isSupport(name) || DRIVER_NAME.equals(name);
    }

    protected String type(Meta meta, String name) {
        String pending = name.toUpperCase();
        String type = TYPES.get(pending);
        if (Strings.isEmpty(type)) {
            if (describers.containsKey(pending)) {
                return pending;
            }
            type = super.type(meta, pending);
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
