package net.isger.brick.bus;

import net.isger.brick.auth.AuthIdentity;
import net.isger.brick.core.Handler;

/**
 * 身份处理器
 * 
 * @author issing
 *
 */
public interface IdentityHandler extends Handler {

    public void open(Endpoint endpoint, AuthIdentity identity);

    public void reload(Endpoint endpoint, AuthIdentity identity);

    public Object handle(Endpoint endpoint, AuthIdentity identity, Object message);

    public void unload(Endpoint endpoint, AuthIdentity identity);

    public void close(Endpoint endpoint, AuthIdentity identity);

}
