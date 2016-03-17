package net.isger.brick.test;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Context;
import net.isger.brick.core.GateModule;

public class TestModule extends GateModule {

    private String something;

    public void operate() {
        try {
            super.operate();
        } catch (Exception e) {
            System.out.println("TestModule does not implement the operate ["
                    + Context.getAction().getCommand().getOperate() + "]");
            BaseCommand.getAction().setResult(
                    "Result: TestModule does not implement the operate ["
                            + Context.getAction().getCommand().getOperate()
                            + "]");
        }
    }

    public void say() {
        System.out.println("TestModule say [" + something + "]");
        BaseCommand.getAction().setResult(
                "Result: TestModule say [" + something + "]");
    }

}
