package net.isger.brick.stub;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import net.isger.brick.stub.dialect.Dialect;
import net.isger.brick.stub.dialect.Dialects;
import net.isger.brick.stub.dialect.SqlDialect;
import net.isger.brick.stub.model.Meta;
import net.isger.brick.stub.model.Metas;
import net.isger.brick.stub.model.Model;
import net.isger.brick.stub.model.Option;
import net.isger.util.Callable;
import net.isger.util.Helpers;
import net.isger.util.Sqls;
import net.isger.util.Strings;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;
import net.isger.util.sql.Page;
import net.isger.util.sql.PageSql;
import net.isger.util.sql.SqlEntry;
import net.isger.util.sql.SqlTransformer;
import net.isger.util.sql.SqlTransformerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 结构化查询语言存根
 * 
 * @author issing
 *
 */
@Ignore
public class SqlStub extends AbstractStub {

    public static final String PARAM_JNDINAME = "jndiName";

    public static final String PARAM_DRIVERNAME = "driverName";

    public static final String PARAM_URL = "url";

    public static final String PARAM_USER = "user";

    public static final String PARAM_PASSWORD = "password";

    private static final Logger LOG;

    @Ignore(mode = Mode.INCLUDE)
    private DataSource dataSource;

    @Ignore(mode = Mode.INCLUDE)
    private Dialect dialect;

    @Ignore(mode = Mode.INCLUDE)
    private SqlTransformer transformer;

    static {
        LOG = LoggerFactory.getLogger(SqlStub.class);
    }

    @Ignore(mode = Mode.INCLUDE)
    public void initial() {
        /* 初始数据源 */
        if (dataSource == null) {
            String value;
            // JNDI
            if (Strings.isNotEmpty(value = getJndiName())) {
                try {
                    dataSource = (DataSource) new InitialContext().lookup(value
                            .trim());
                } catch (Exception e) {
                    throw new IllegalStateException(
                            "Couldn't lookup DataSource from " + value + " - "
                                    + e.getMessage(), e);
                }
            }
            // JDBC
            else if (Strings.isNotEmpty(value = getUrl())
                    && Strings.isNotEmpty(getDriverName())) {
                if (LOG.isDebugEnabled()) {
                    LOG.info("Initializing DataSource url {}", value);
                }
                dataSource = new BaseDataSource(getDriverName(), value,
                        getUser(), getPassword());
            } else {
                throw new IllegalStateException(
                        "Unexpected JNDI or JDBC configuration. Make sure you want to use the sql stub");
            }
        }
        /* 初始方言 */
        Dialect dialect = getDialect();
        if (dialect == null) {
            dialect = Dialects.getDialect(getDriverName());
            if (dialect == null) {
                dialect = new SqlDialect();
            }
        }
        this.dialect = dialect;
        /* 转换器 */
        if (transformer == null) {
            transformer = new SqlTransformerAdapter();
        } else {
            transformer.initial();
        }
        /* 标准化 */
        initialStandard(StubCommand.getAction());
    }

    /**
     * 初始标准化
     * 
     * @param cmd
     */
    protected void initialStandard(StubCommand cmd) {
        cmd.setCondition(new Page());
        /* 模型 */
        initialModel(cmd, new Model());
        /* 元数据 */
        initialModel(cmd, new Meta());
        /* 元数据选项 */
        initialModel(cmd, new Option());
    }

    /**
     * 初始模型
     * 
     * @param cmd
     * @param table
     */
    private void initialModel(StubCommand cmd, Object table) {
        cmd.setTable(table);
        try {
            exists(cmd);
        } catch (Exception e) {
            create(cmd);
            Model model;
            for (Meta meta : Metas.getMetas(table).values()) {
                if ((model = meta.toModel()) != null) {
                    initialModel(cmd, model);
                }
            }
        }
    }

    protected String getJndiName() {
        return (String) getParameter(PARAM_JNDINAME);
    }

    protected String getDriverName() {
        return (String) getParameter(PARAM_DRIVERNAME);
    }

    protected String getUrl() {
        return (String) getParameter(PARAM_URL);
    }

    protected String getUser() {
        return (String) getParameter(PARAM_USER);
    }

    protected String getPassword() {
        return (String) getParameter(PARAM_PASSWORD);
    }

    protected DataSource getDataSource() {
        return dataSource;
    }

    protected Dialect getDialect() {
        return dialect;
    }

    /**
     * 获取库连接
     * 
     * @return
     */
    protected Connection getConnection(StubCommand cmd) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException("Failure to connect database",
                    e.getCause());
        }
    }

    /**
     * 创建数据表
     */
    @Ignore(mode = Mode.INCLUDE)
    public void create(StubCommand cmd) {
        Object table = cmd.getTable();
        final Connection conn = getConnection(cmd);
        Object[] condition = getCondition(cmd, 3);
        Object result;
        try {
            if (table instanceof String) {
                result = Sqls.modify(transformer.transform(dialect
                        .getCreateEntry((String) table,
                                (String[][]) condition[0])), conn);
            } else if (table instanceof Class) {
                if (condition == null) {
                    result = Sqls.modify(transformer.transform(dialect
                            .getCreateEntry((Class<?>) table)), conn);
                } else {
                    result = modify(cmd.getOperate(), (Class<?>) table,
                            condition, conn);
                }
            } else {
                result = Helpers.each(table, new Callable<Object>() {
                    public Object call(Object... args) {
                        return Sqls.modify(transformer.transform(dialect
                                .getCreateEntry(args[1])), conn);
                    }
                });
            }
        } finally {
            close(conn);
        }
        cmd.setResult(result);
    }

    /**
     * 插入表数据
     */
    @Ignore(mode = Mode.INCLUDE)
    public void insert(StubCommand cmd) {
        Object table = cmd.getTable();
        Object[] condition = getCondition(cmd, 3);
        final Connection conn = getConnection(cmd);
        Object result;
        try {
            if (table instanceof String) {
                result = Sqls
                        .modify(dialect.getInsertEntry((String) table,
                                condition), conn);
            } else if (table instanceof Class) {
                result = modify(cmd.getOperate(), (Class<?>) table, condition,
                        conn);
            } else {
                result = Helpers.each(table, new Callable<Object>() {
                    public Object call(Object... args) {
                        return Sqls.modify(dialect.getInsertEntry(args[1]),
                                conn);
                    }
                });
            }
        } finally {
            close(conn);
        }
        cmd.setResult(result);
    }

    /**
     * 删除表数据
     */
    @Ignore(mode = Mode.INCLUDE)
    public void delete(StubCommand cmd) {
        Object table = cmd.getTable();
        Object[] condition = getCondition(cmd, 3);
        final Connection conn = getConnection(cmd);
        Object result;
        try {
            if (table instanceof String) {
                result = Sqls.modify(dialect.getDeleteEntry((String) table,
                        (Object[]) condition[0]), conn);
            } else if (table instanceof Class) {
                result = modify(cmd.getOperate(), (Class<?>) table, condition,
                        conn);
            } else {
                result = Helpers.each(table, new Callable<Object>() {
                    public Object call(Object... args) {
                        return Sqls.modify(dialect.getDeleteEntry(args[1]),
                                conn);
                    }
                });
            }
        } finally {
            close(conn);
        }
        cmd.setResult(result);
    }

    /**
     * 修改表数据
     */
    @Ignore(mode = Mode.INCLUDE)
    public void update(StubCommand cmd) {
        Object table = cmd.getTable();
        Object[] condition = getCondition(cmd, 3);
        final Connection conn = getConnection(cmd);
        Object result;
        try {
            if (table instanceof String) {
                Object[][] values = (Object[][]) condition[0];
                result = Sqls.modify(dialect.getUpdateEntry((String) table,
                        values[0], values[1]), conn);
            } else if (table instanceof Class) {
                result = modify(cmd.getOperate(), (Class<?>) table, condition,
                        conn);
            } else {
                result = Helpers.each(table, new Callable<Object>() {
                    public Object call(Object... args) {
                        Object[] values = (Object[]) args[1];
                        return Sqls.modify(
                                dialect.getUpdateEntry(values[0], values[1]),
                                conn);
                    }
                });
            }
        } finally {
            close(conn);
        }
        cmd.setResult(result);
    }

    /**
     * 查询表数据
     */
    @Ignore(mode = Mode.INCLUDE)
    public void select(StubCommand cmd) {
        Object table = cmd.getTable();
        Object[] condition = getCondition(cmd, 3);
        final Connection conn = getConnection(cmd);
        Object[] result;
        search: try {
            SqlEntry sqlEntry;
            if (table instanceof String) {
                sqlEntry = dialect.getSearchEntry((String) table,
                        (String[]) condition[0], (Object[]) condition[1]);
                result = Sqls.query(sqlEntry, conn);
            } else if (table instanceof Class) {
                canonicalize(cmd.getOperate(), condition);
                String sql = Sqls.getSQL((Class<?>) table, dialect.name(),
                        (String) condition[0], (Object[]) condition[2]);
                sqlEntry = transformer.transform(dialect.getSearchEntry(sql,
                        (Object[]) condition[1]));
                result = Sqls.query(sqlEntry, conn);
            } else {
                result = (Object[]) Helpers.each(table, new Callable<Object>() {
                    public Object call(Object... args) {
                        return Sqls.query(dialect.getSearchEntry(args[1]), conn);
                    }
                });
                break search;
            }
            /* 获取数据总数（分页） */
            if (sqlEntry instanceof PageSql) {
                String countSql = ((PageSql) sqlEntry).getCountSql();
                if (countSql != null) {
                    Object[] target = new Object[result.length + 1];
                    System.arraycopy(result, 0, target, 0, result.length);
                    target[result.length] = ((Number) ((Object[][]) Sqls.query(
                            countSql, sqlEntry.getValues(), conn)[1])[0][0])
                            .longValue();
                    result = (Object[]) target;
                }
            }
        } finally {
            close(conn);
        }
        cmd.setResult(result);
    }

    /**
     * 检查表是否存在
     * 
     * @param cmd
     */
    @Ignore(mode = Mode.INCLUDE)
    public void exists(StubCommand cmd) {
        Object table = cmd.getTable();
        Object[] condition = getCondition(cmd, 3);
        final Connection conn = getConnection(cmd);
        Object[] result;
        search: try {
            SqlEntry sqlEntry;
            if (table instanceof String) {
                sqlEntry = dialect.getExistsEntry((String) table);
                result = Sqls.query(sqlEntry, conn);
            } else if (table instanceof Class) {
                canonicalize(cmd.getOperate(), condition);
                String sql = Sqls.getSQL((Class<?>) table, dialect.name(),
                        Strings.empty(condition[0], "exists"),
                        (Object[]) condition[2]);
                sqlEntry = transformer.transform(dialect.getSearchEntry(sql,
                        (Object[]) condition[1]));
                result = Sqls.query(sqlEntry, conn);
            } else {
                result = (Object[]) Helpers.each(table, new Callable<Object>() {
                    public Object call(Object... args) {
                        return Sqls.query(dialect.getExistsEntry(args[1]), conn);
                    }
                });
                break search;
            }
        } finally {
            close(conn);
        }
        cmd.setResult(result);
    }

    /**
     * 删除数据表
     */
    @Ignore(mode = Mode.INCLUDE)
    public void remove(StubCommand cmd) {
        Object table = cmd.getTable();
        final Connection conn = getConnection(cmd);
        Object result;
        try {
            result = Helpers.each(table, new Callable<Object>() {
                public Object call(Object... args) {
                    return Sqls.modify(dialect.getRemoveEntry(args[1]), conn);
                }
            });
        } finally {
            close(conn);
        }
        cmd.setResult(result);
    }

    /**
     * 获取条件
     * 
     * @param cmd
     * @param length
     * @return
     */
    protected Object[] getCondition(StubCommand cmd, int length) {
        Object config = cmd.getCondition();
        if (config instanceof Condition) {
            throw new IllegalStateException(
                    "Unsupported feature in the current version");
        }
        return (Object[]) Helpers.newArray(config, length);
    }

    /**
     * 修改操作
     * 
     * @param operate
     * @param table
     * @param condition
     * @param conn
     * @return
     */
    protected Object modify(String operate, Class<?> table, Object[] condition,
            Connection conn) {
        canonicalize(operate, condition);
        SqlEntry entry = transformer.transform(
                Sqls.getSQL(table, dialect.name(), (String) condition[0],
                        (Object[]) condition[2]), (Object[]) condition[1]);
        String sql = entry.getSql();
        Object value = entry.getValues();
        return value instanceof Object[][] ? Sqls.modify(sql,
                (Object[][]) value, conn) : Sqls.modify(sql, (Object[]) value,
                conn);
    }

    /**
     * 规范操作条件
     * 
     * @param operate
     * @param condition
     */
    private void canonicalize(String operate, Object[] condition) {
        if (!(condition[0] == null || condition[0] instanceof String)) {
            int index = condition.length - 1;
            while (index > 0) {
                condition[index] = condition[--index];
            }
            condition[index] = operate;
        }
    }

    /**
     * 关闭库连接
     * 
     * @param conn
     */
    public void close(Connection conn) {
        try {
            if (conn != null && conn.getAutoCommit()) {
                conn.close();
            }
        } catch (SQLException e) {
        }
    }

    @Ignore(mode = Mode.INCLUDE)
    public void destroy() {
    }

}
