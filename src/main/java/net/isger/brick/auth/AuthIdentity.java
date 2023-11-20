package net.isger.brick.auth;

import java.util.HashMap;
import java.util.Map;

import net.isger.util.Helpers;
import net.isger.util.Strings;

/**
 * 认证身份
 * 
 * @author issing
 */
public class AuthIdentity {

    private String id;

    private long mark;

    private long time;

    private int frequency;

    private AuthToken<?> token;

    private Map<String, Object> attributes;

    public AuthIdentity() {
        this(null, null);
    }

    public AuthIdentity(AuthToken<?> token) {
        this(null, token);
    }

    public AuthIdentity(String id) {
        this(id, null);
    }

    public AuthIdentity(String id, AuthToken<?> token) {
        this.id = Strings.empty(id, Helpers.makeUUID());
        this.mark = this.time = System.currentTimeMillis();
        this.attributes = new HashMap<String, Object>();
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public int getFrequency() {
        return frequency;
    }

    public boolean isLogin() {
        return this.token != null;
    }

    public AuthToken<?> getToken() {
        return token;
    }

    protected void setToken(AuthToken<?> token) {
        this.token = token;
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        if (value == null) {
            this.attributes.remove(name);
        } else {
            this.attributes.put(name, value);
        }
    }

    public void active(boolean create) {
        this.time = System.currentTimeMillis();
        if ((this.time - this.mark) / 1000 / 60 >= 1) {
            this.mark = this.time;
            this.frequency = 0;
        }
        this.frequency++;
    }

    public void setTimeout(int timeout) {
    }

    public void clear() {
        this.attributes.clear();
    }

}
