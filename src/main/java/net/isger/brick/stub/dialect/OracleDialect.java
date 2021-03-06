package net.isger.brick.stub.dialect;

import net.isger.util.sql.Page;
import net.isger.util.sql.PageSql;

public class OracleDialect extends SqlDialect {

    private static final String DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";

    public boolean isSupport(String name) {
        return super.isSupport(name) || DRIVER_NAME.equals(name);
    }

    public PageSql getSearchEntry(Page page, String sql, Object[] values) {
        return new PageSql(page, sql, values) {
            public String getWrapSql(String sql) {
                return "select * from (select t1.*, rownum rn from (" + sql
                        + ") t1 where rownum <= ?) t2 where rn > ?";
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
                wrapValues[valCount - 2] = page.getStart() * page.getLimit();
                return wrapValues;
            }
        };
    }

}
