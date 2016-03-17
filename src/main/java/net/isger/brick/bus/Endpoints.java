package net.isger.brick.bus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.isger.util.Helpers;

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
        name = Helpers.getAliasName(endpoint.getClass(), "Endpoint$", name)
                .toLowerCase();
        if (LOG.isDebugEnabled()) {
            LOG.info("Binding [{}] endpoint [{}]", name, endpoint);
        }
        endpoint = endpoints.put(name, endpoint);
        if (endpoint != null) {
            LOG.warn("(!) Discard [{}] endpoint [{}]", name, endpoint);
        }

    }

    public Map<String, Endpoint> get() {
        return Collections.unmodifiableMap(endpoints);
    }

    public Endpoint get(String name) {
        return endpoints.get(name);
    }

}
