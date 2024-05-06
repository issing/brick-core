package net.isger.brick.test;

import net.isger.brick.core.BaseGate;
import net.isger.brick.core.Context;

public class TestGate extends BaseGate {

    private String something;

    public void operate() {
        System.out.println("TestGate does not implement the operate [" + Context.getAction().getCommand().getOperate() + "]");
    }

    public void say() {
        System.out.println("TestGate say [" + something + "]");
    }

}
