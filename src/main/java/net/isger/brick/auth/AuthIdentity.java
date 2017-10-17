package net.isger.brick.auth;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.isger.util.Helpers;

/**
 * 认证身份
 * 
 * @author issing
 */
public class AuthIdentity {

    private String id;

    private long time;

    private AuthToken<?> token;

    private Map<String, Object> attributes;

    public AuthIdentity() {
        this.id = Helpers.makeUUID();
        this.time = System.currentTimeMillis();
        this.attributes = new HashMap<String, Object>();
    }

    public AuthIdentity(AuthToken<?> token) {
        this();
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public Date getTime() {
        return new Date(time);
    }

    public boolean isLogin() {
        return this.token != null;
    }

    public AuthToken<?> getToken() {
        return token;
    }

    void setToken(AuthToken<?> token) {
        this.token = token;
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        if (value == null) {
            attributes.remove(name);
        } else {
            attributes.put(name, value);
        }
    }

    public void active() {
        this.time = System.currentTimeMillis();
    }

    public void clear() {
        attributes.clear();
    }

}
