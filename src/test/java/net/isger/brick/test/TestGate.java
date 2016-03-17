package net.isger.brick.test;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.BaseGate;
import net.isger.brick.core.Context;

public class TestGate extends BaseGate {

    private String something;

    public void operate() {
        try {
            super.operate();
        } catch (Exception e) {
            System.out.println("TestGate does not implement the operate ["
                    + Context.getAction().getCommand().getOperate() + "]");
            BaseCommand.getAction().setResult("this is TestGate.exception");
        }
    }

    public void say() {
        System.out.println("TestGate say [" + something + "]");
        BaseCommand.getAction().setResult("this is TestGate.say");
    }

}
