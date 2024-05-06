package net.isger.brick.bus;

import net.isger.brick.auth.AuthIdentity;

public class IdentityHandlerAdapter implements IdentityHandler {

    public int getStatus() {
        return 1;
    }

    public Object handle(Object message) {
        return message;
    }

    public void open(Endpoint endpoint, AuthIdentity identity) {
    }

    public void reload(Endpoint endpoint, AuthIdentity identity) {
    }

    public Object handle(Endpoint endpoint, AuthIdentity identity, Object message) {
        return this.handle(message);
    }

    public void unload(Endpoint endpoint, AuthIdentity identity) {

    }

    public void close(Endpoint endpoint, AuthIdentity identity) {
    }

}
