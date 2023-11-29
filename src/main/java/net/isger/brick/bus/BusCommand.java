package net.isger.brick.bus;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Command;

public class BusCommand extends BaseCommand {

    public static final String CTRL_ENDPOINT = "bus-endpoint";

    public static final String OPERATE_SEND = "send";

    public BusCommand() {
    }

    public BusCommand(Command cmd) {
        super(cmd);
    }

    public BusCommand(boolean hasShell) {
        super(hasShell);
    }

    public static BusCommand getAction() {
        return cast(BaseCommand.getAction());
    }

    public static BusCommand newAction() {
        return cast(BaseCommand.newAction());
    }

    public static BusCommand mockAction() {
        return cast(BaseCommand.mockAction());
    }

    public static BusCommand realAction() {
        return cast(BaseCommand.realAction());
    }

    public static BusCommand cast(BaseCommand cmd) {
        return cmd == null || cmd.getClass() == BusCommand.class ? (BusCommand) cmd : cmd.infect(new BusCommand(false));
    }

    public String getEndpoint() {
        return getHeader(CTRL_ENDPOINT);
    }

    public void setEndpoint(String endpoint) {
        setHeader(CTRL_ENDPOINT, endpoint);
    }

    public BusCommand clone() {
        return (BusCommand) super.clone();
    }

}
