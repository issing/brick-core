package net.isger.brick.bus.protocol;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.isger.util.Helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                    for (Entry<String, Object> entry : ((Map<String, Object>) instance)
                            .entrySet()) {
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
        put(null, protocol);
    }

    public void put(String name, Protocol protocol) {
        name = Helpers.getAliasName(protocol.getClass(), "Protocol$", name)
                .toLowerCase();
        if (LOG.isDebugEnabled()) {
            LOG.info("Binding [{}] protocol [{}]", name, protocol);
        }
        protocol = protocols.put(name, protocol);
        if (protocol != null) {
            LOG.warn("(!) Discard [{}] protocol [{}]", name, protocol);
        }
    }

    public Map<String, Protocol> get() {
        return Collections.unmodifiableMap(protocols);
    }

    public Protocol get(String name) {
        return protocols.get(name);
    }

}
