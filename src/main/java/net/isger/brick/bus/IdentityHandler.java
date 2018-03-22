package net.isger.brick.bus;

import net.isger.brick.auth.AuthIdentity;
import net.isger.brick.core.Handler;

public interface IdentityHandler extends Handler {

    public Object handle(Endpoint endpoint, AuthIdentity identity,
            Object message);

}
