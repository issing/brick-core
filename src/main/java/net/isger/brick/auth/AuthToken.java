package net.isger.brick.auth;

public abstract class AuthToken<T> {

    protected final T source;

    @SuppressWarnings("unchecked")
    public AuthToken() {
        this.source = (T) this;
    }

    public AuthToken(T source) {
        this.source = source;
    }

    public abstract Object getPrincipal();

    public abstract Object getCredentials();

}
