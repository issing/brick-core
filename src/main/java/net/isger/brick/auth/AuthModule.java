package net.isger.brick.auth;

import net.isger.brick.Constants;
import net.isger.brick.core.Gate;
import net.isger.brick.core.GateModule;

/**
 * 认证模块
 * 
 * @author issing
 *
 */
public class AuthModule extends GateModule {

    public Class<? extends Gate> getTargetClass() {
        return Auth.class;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Gate> getImplementClass() {
        Class<? extends Gate> implClass = (Class<? extends Gate>) getImplementClass(Constants.MOD_AUTH, null);
        if (implClass == null) {
            implClass = super.getImplementClass();
        }
        return implClass;
    }

    public Class<? extends Gate> getBaseClass() {
        return BaseAuth.class;
    }

    public void initial() {
        if (this.getGate(Constants.SYSTEM) == null) {
            this.setGate(Constants.SYSTEM, create());
        }
        super.initial();
    }

}
