package net.isger.brick.bus.protocol;

import net.isger.brick.util.ScanLoader;
import net.isger.util.Strings;
import net.isger.util.reflect.conversion.Conversion;
import net.isger.util.scan.ScanFilter;

public class ProtocolsConversion extends ScanLoader implements Conversion {

    private static final ScanFilter FILTER;

    private static ProtocolsConversion INSTANCE;

    static {
        FILTER = new ScanFilter() {
            public boolean isDeep() {
                return true;
            }

            public boolean accept(String name) {
                return Strings.endWithIgnoreCase(name, "Protocol[.]class$");
            }
        };
    }

    private ProtocolsConversion() {
        super(Protocol.class, FILTER);
    }

    public static Conversion getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProtocolsConversion();
        }
        return INSTANCE;
    }

    public boolean isSupport(Class<?> type) {
        return Protocols.class.isAssignableFrom(type);
    }

    public Object convert(Class<?> type, Object res) {
        return new Protocols(toList(load(res)));
    }

    public String toString() {
        return Protocols.class.getName();
    }

}
