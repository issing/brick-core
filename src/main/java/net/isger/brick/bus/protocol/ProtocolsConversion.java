package net.isger.brick.bus.protocol;

import java.io.File;
import java.lang.reflect.Type;

import net.isger.brick.util.ScanLoader;
import net.isger.util.Reflects;
import net.isger.util.Strings;
import net.isger.util.reflect.ClassAssembler;
import net.isger.util.reflect.conversion.Conversion;
import net.isger.util.scan.ScanFilter;

public class ProtocolsConversion extends ScanLoader implements Conversion {

    private static final ScanFilter FILTER;

    private static ProtocolsConversion INSTANCE;

    static {
        FILTER = new ScanFilter() {
            public boolean isDeep(File root, File path) {
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

    public boolean isSupport(Type type) {
        return Protocols.class.isAssignableFrom(Reflects.getRawClass(type));
    }

    public Object convert(Type type, Object res, ClassAssembler assembler) {
        return new Protocols(toList(load(res, assembler)));
    }

    public String toString() {
        return Protocols.class.getName();
    }

}
