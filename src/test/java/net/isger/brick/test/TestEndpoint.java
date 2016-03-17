package net.isger.brick.test;

import net.isger.brick.bus.Endpoint;
import net.isger.brick.bus.Status;
import net.isger.brick.util.DynamicOperator;

public class TestEndpoint extends DynamicOperator implements Endpoint {

    private Status status;

    public Status getStatus() {
        return status;
    }

    public void initial() {
        System.out.println("TestEndpoint.initial().");
        status = Status.ACTIVATED;
    }

    public void destroy() {
        System.out.println("TestEndpoint.destroy().");
        status = Status.DEACTIVATED;
    }

}
