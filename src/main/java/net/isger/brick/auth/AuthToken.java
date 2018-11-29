package net.isger.brick.auth;

/**
 * 认证令牌
 * 
 * @author issing
 * @param <T>
 */
public abstract class AuthToken<T> {

    protected final T source;

    public AuthToken() {
        this(null);
    }

    @SuppressWarnings("unchecked")
    protected AuthToken(T source) {
        this.source = source == null ? (T) this : source;
    }

    public T getSource() {
        return source;
    }

    public abstract Object getPrincipal();

    public abstract Object getCredentials();

}
