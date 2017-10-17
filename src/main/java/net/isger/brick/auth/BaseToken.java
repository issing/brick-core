package net.isger.brick.auth;

/**
 * 基础令牌
 * 
 * @author issing
 */
public class BaseToken extends AuthToken<BaseToken> {

    private Object principal;

    private Object credentials;

    public BaseToken(Object principal, Object credentials) {
        this.principal = principal;
        this.credentials = credentials;
    }

    public Object getPrincipal() {
        return principal;
    }

    public Object getCredentials() {
        return credentials;
    }

}
