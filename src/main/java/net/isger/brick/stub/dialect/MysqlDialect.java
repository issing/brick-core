package net.isger.brick.stub.dialect;

import java.util.HashMap;
import java.util.Map;

import net.isger.brick.stub.model.Meta;
import net.isger.util.Strings;

/**
 * MySQL
 * 
 * @author issing
 *
 */
public class MysqlDialect extends SqlDialect {

    private static final String DRIVER_NAME = "com.mysql.jdbc.Driver";

    public static final String NUMERIC = "numeric";

    private static final Map<String, String> TYPES;

    static {
        TYPES = new HashMap<String, String>();
        TYPES.put(NUMBER.toUpperCase(), NUMERIC.toUpperCase());
    }

    public boolean isSupport(String name) {
        return super.isSupport(name) || DRIVER_NAME.equals(name);
    }

    protected String type(Meta meta, String name) {
        String type = TYPES.get(name.toUpperCase());
        if (Strings.isEmpty(type)) {
            type = super.type(meta, name);
        }
        return type;
    }

    protected String seal() {
        return "`";
    }

}
