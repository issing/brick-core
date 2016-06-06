package net.isger.brick.stub.dialect;

import net.isger.util.Named;
import net.isger.util.sql.SqlEntry;

/**
 * 方言接口
 * 
 * @author issing
 * 
 */
public interface Dialect extends Named {

    public static final String BOOLEAN = "boolean";

    public static final String INTEGER = "integer";

    public static final String NUMBER = "number";

    public static final String DATE = "date";

    public static final String STRING = "string";

    public static final int DEFAULT = 0;

    public static final int PRIMARY = 1;

    public static final int FOREIGN = 2;

    public static final int NOTNULL = 3;

    public static final int UNIQUE = 4;

    public static final int CHECK = 5;

    public static final int INDEX = 6;

    public static final int SCALE_DATE = 0;

    public static final int SCALE_TIME = 1;

    public static final int SCALE_TIMESTAMP = 2;

    public boolean isSupport(String name);

    public SqlEntry getCreateEntry(Object table);

    public SqlEntry getCreateEntry(String table, String[][] describes);

    public SqlEntry getInsertEntry(Object table);

    public SqlEntry getInsertEntry(String table, Object[] gridData);

    public SqlEntry getDeleteEntry(Object table);

    public SqlEntry getDeleteEntry(String table, Object[] gridData);

    public SqlEntry getUpdateEntry(Object newTable, Object oldTable);

    public SqlEntry getUpdateEntry(String table, Object[] newGridData,
            Object[] oldGridData);

    public SqlEntry getSearchEntry(Object table);

    public SqlEntry getSearchEntry(String tableName, Object[] columns,
            Object[] gridData);

    public SqlEntry getSearchEntry(String sql, Object[] values);

    public SqlEntry getRemoveEntry(Object table);

}
