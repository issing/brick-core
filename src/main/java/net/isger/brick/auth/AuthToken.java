package net.isger.brick.auth;

public abstract class AuthToken {

    private Object source;

    public AuthToken(Object source) {
        this.source = source;
    }

    public Object getSource() {
        return source;
    }

    public abstract Object getPrincipal();

    public abstract Object getCredentials();

}
