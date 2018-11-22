package net.isger.brick.stub.dialect;

/**
 * MySQL
 * 
 * @author issing
 *
 */
public class MysqlDialect extends SqlDialect {

    private static final String DRIVER_NAME = "com.mysql.jdbc.Driver";

    public static final String NUMERIC = "numeric";

    public boolean isSupport(String name) {
        return super.isSupport(name) || DRIVER_NAME.equals(name);
    }

    protected Describer getNumberDescriber(String name) {
        if (NUMBER.equalsIgnoreCase(name)) {
            name = NUMERIC;
        }
        return new NumberDescriber(name);
    }

}
