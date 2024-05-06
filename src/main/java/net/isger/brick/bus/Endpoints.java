package net.isger.brick.bus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.isger.util.Helpers;
import net.isger.util.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Endpoints {

    private static final Logger LOG;

    private Map<String, Endpoint> endpoints;

    static {
        LOG = LoggerFactory.getLogger(Endpoints.class);
    }

    public Endpoints() {
        this(null);
    }

    public Endpoints(Map<String, Endpoint> endpoints) {
        this.endpoints = new HashMap<String, Endpoint>();
        if (endpoints != null) {
            for (Entry<String, Endpoint> endpoint : endpoints.entrySet()) {
                put(endpoint.getKey(), endpoint.getValue());
            }
        }
    }

    public void put(String name, Endpoint endpoint) {
        int index = name.lastIndexOf(".");
        String key;
        if (index++ > 0) {
            key = name.substring(0, index);
            name = name.substring(index);
        } else {
            key = "";
        }
        key += getName(endpoint.getClass(), name);
        if (LOG.isDebugEnabled()) {
            LOG.info("Binding [{}] endpoint [{}]", key, endpoint);
        }
        endpoint = endpoints.put(key, endpoint);
        if (endpoint != null) {
            LOG.warn("(!) Discard [{}] endpoint [{}]", key, endpoint);
        }

    }

    public Map<String, Endpoint> gets() {
        return Collections.unmodifiableMap(this.endpoints);
    }

    public Endpoint get(String name) {
        return this.endpoints.get(name);
    }

    public static final String getName(Class<? extends Endpoint> clazz) {
        return getName(clazz, "");
    }

    public static final String getName(Class<? extends Endpoint> clazz, String name) {
        return Helpers.getAliasName(clazz, "Endpoint$", Strings.toLower(name));
    }

}
