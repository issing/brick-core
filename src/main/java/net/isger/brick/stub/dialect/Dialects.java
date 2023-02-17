package net.isger.brick.stub.dialect;

import java.util.Hashtable;
import java.util.Map;

import net.isger.util.hitch.Director;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dialects {

    private static final String KEY_DIALECTS = "brick.core.dialects";

    private static final String DIALECT_PATH = "net/isger/brick/stub/dialect";

    private static final Logger LOG;

    private static final Dialects INSTANCE;

    private Map<String, Dialect> dialects;

    static {
        LOG = LoggerFactory.getLogger(Dialects.class);
        INSTANCE = new Dialects();
        new Director() {
            protected String directPath() {
                return directPath(KEY_DIALECTS, DIALECT_PATH);
            }
        }.direct(INSTANCE);
    }

    private Dialects() {
        dialects = new Hashtable<String, Dialect>();
    }

    public static void addDialect(Dialect dialect) {
        String name = dialect.getClass().getName();
        if (LOG.isDebugEnabled()) {
            LOG.info("Achieve dialect [{}]", name);
        }
        dialect = INSTANCE.dialects.put(name, dialect);
        if (dialect != null && LOG.isDebugEnabled()) {
            LOG.warn("(!) Discard dialect [{}]", dialect);
        }
    }

    public static Dialect getDialect(String driverName) {
        Dialect result = null;
        for (Dialect dialect : INSTANCE.dialects.values()) {
            if (dialect.isSupport(driverName)) {
                result = dialect;
                break;
            }
        }
        return result;
    }

}
