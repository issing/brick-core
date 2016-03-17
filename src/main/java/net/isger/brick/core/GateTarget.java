package net.isger.brick.core;

import net.isger.brick.Constants;
import net.isger.brick.util.DynamicOperator;

public class GateTarget extends DynamicOperator {

    protected Console getConsole() {
        return Console.getAction();
    }

    protected BaseCommand getCommand() {
        return BaseCommand.getAction();
    }

    protected Module getModule() {
        return (Module) ((InternalContext) Context.getAction())
                .getInternal(Constants.BRICK_MODULE);
    }

    protected Gate getGate() {
        return (Gate) ((InternalContext) Context.getAction())
                .getInternal(Gate.BRICK_GATE);
    }

    protected BaseCommand mockCommand() {
        return Context.getAction().mockCommand();
    }

    protected BaseCommand realCommand() {
        return Context.getAction().realCommand();
    }

}
