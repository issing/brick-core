package net.isger.brick.test;

import net.isger.brick.bus.BusCommand;
import net.isger.brick.bus.Endpoint;
import net.isger.brick.bus.Status;
import net.isger.brick.util.CommandOperator;

public class TestEndpoint extends CommandOperator implements Endpoint {

    private Status status;

    public String name() {
        return null;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isActive() {
        return status == Status.ACTIVATED;
    }

    public void initial() {
        System.out.println("TestEndpoint.initial().");
        status = Status.ACTIVATED;
    }

    public void operate(BusCommand cmd) {
        super.operate(cmd);
    }

    public void destroy() {
        System.out.println("TestEndpoint.destroy().");
        status = Status.DEACTIVATED;
    }

}
