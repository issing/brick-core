package net.isger.brick.bus.protocol;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.util.Helpers;
import net.isger.util.Strings;

public class Protocols {

    private static final Logger LOG;

    private Map<String, Protocol> protocols;

    static {
        LOG = LoggerFactory.getLogger(Protocols.class);
    }

    public Protocols() {
        this(null);
    }

    @SuppressWarnings("unchecked")
    public Protocols(List<Object> protocols) {
        this.protocols = new HashMap<String, Protocol>();
        if (protocols != null) {
            for (Object instance : protocols) {
                if (instance instanceof Protocol) {
                    add((Protocol) instance);
                } else if (instance instanceof Map) {
                    for (Entry<String, Object> entry : ((Map<String, Object>) instance).entrySet()) {
                        instance = entry.getValue();
                        if (instance instanceof Protocol) {
                            put(entry.getKey(), (Protocol) instance);
                        }
                    }
                }
            }
        }
    }

    public void add(Protocol protocol) {
        put("", protocol);
    }

    public void put(String name, Protocol protocol) {
        int index = name.lastIndexOf(".");
        String key;
        if (index++ > 0) {
            key = name.substring(0, index);
            name = name.substring(index);
        } else {
            key = "";
        }
        key += getName(protocol.getClass(), name);
        if (LOG.isDebugEnabled()) {
            LOG.info("Binding [{}] protocol [{}]", key, protocol);
        }
        protocol = protocols.put(key, protocol);
        if (protocol != null) {
            LOG.warn("(!) Discard [{}] protocol [{}]", key, protocol);
        }
    }

    public Protocol get(String name) {
        return protocols.get(name);
    }

    public Map<String, Protocol> gets() {
        return Collections.unmodifiableMap(protocols);
    }

    public static final String getName(Class<? extends Protocol> clazz) {
        return getName(clazz, "");
    }

    public static final String getName(Class<? extends Protocol> clazz, String name) {
        return Helpers.getAliasName(clazz, "Protocol$", Strings.toLower(name));
    }

}
