package net.isger.brick.auth;

import java.util.HashMap;
import java.util.Map;

public class AuthIdentity {

    private Object token;

    private Map<String, Object> attributes;

    public AuthIdentity() {
        this.attributes = new HashMap<String, Object>();
    }

    public AuthIdentity(Object token) {
        this();
        this.token = token;
    }

    public boolean isLogin() {
        return this.token != null;
    }

    public Object getToken() {
        return token;
    }

    public void setToken(Object token) {
        this.token = token;
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public void clear() {
        attributes.clear();
    }

}
