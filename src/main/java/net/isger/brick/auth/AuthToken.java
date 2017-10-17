package net.isger.brick.auth;

/**
 * 认证令牌
 * 
 * @author issing
 * @param <T>
 */
public abstract class AuthToken<T> {

    protected final T source;

    @SuppressWarnings("unchecked")
    public AuthToken() {
        this.source = (T) this;
    }

    public AuthToken(T source) {
        this.source = source;
    }

    public T getSource() {
        return source;
    }

    public abstract Object getPrincipal();

    public abstract Object getCredentials();

}
