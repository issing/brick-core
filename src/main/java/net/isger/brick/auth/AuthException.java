package net.isger.brick.auth;

public class AuthException extends RuntimeException {

    private static final long serialVersionUID = 5201955887730899455L;

    public AuthException() {
        super();
    }

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthException(Throwable cause) {
        super(cause);
    }

}
