package net.isger.brick.stub;

import net.isger.brick.auth.AuthCommand;
import net.isger.brick.core.Gate;
import net.isger.brick.core.GateModule;

/**
 * 存根模块
 * 
 * @author issing
 * 
 */
public class StubModule extends GateModule {

    public static final String STUB = "stub";

    public Class<? extends Gate> getTargetClass() {
        return Stub.class;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Gate> getImplementClass() {
        Class<? extends Gate> implClass = (Class<? extends Gate>) getImplementClass(
                STUB, null);
        if (implClass == null) {
            implClass = super.getImplementClass();
        }
        return implClass;
    }

    public Class<? extends Gate> getBaseClass() {
        return SqlStub.class;
    }

    protected void initial(String domain, Gate gate) {
        AuthCommand cmd = new AuthCommand();
        StubCommand token = new StubCommand();
        token.setDomain(domain);
        token.setOperate(StubCommand.OPERATE_INITIAL);
        cmd.setToken(token);
        console.execute(cmd);
    }

}
