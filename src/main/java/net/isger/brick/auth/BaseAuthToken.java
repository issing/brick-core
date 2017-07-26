package net.isger.brick.auth;

public class BaseAuthToken extends AuthToken<BaseAuthToken> {

    private Object principal;

    private Object credentials;

    public BaseAuthToken(Object principal, Object credentials) {
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
