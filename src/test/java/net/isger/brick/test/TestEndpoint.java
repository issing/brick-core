package net.isger.brick.test;

import net.isger.brick.bus.BusCommand;
import net.isger.brick.bus.Endpoint;
import net.isger.brick.util.CommandOperator;

public class TestEndpoint extends CommandOperator implements Endpoint {

    private Status status;

    public String name() {
        return null;
    }

    public boolean hasReady() {
        return status == Status.INITIALIZED;
    }

    public Status getStatus() {
        return status;
    }

    public void initial() {
        System.out.println("TestEndpoint.initial().");
        status = Status.INITIALIZED;
    }

    public void operate(BusCommand cmd) {
        super.operate(cmd);
    }

    public void destroy() {
        System.out.println("TestEndpoint.destroy().");
        status = Status.DESTROYED;
    }

}
