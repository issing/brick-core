package net.isger.brick.bus;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Command;

public class BusCommand extends BaseCommand {

    public static final String OPERATE_SEND = "send";

    public static final String KEY_ENDPOINT = "bus-endpoint";

    public static final String KEY_PAYLOAD = "bus-payload";

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

    public static BusCommand cast(BaseCommand cmd) {
        return cmd == null || cmd.getClass() == BusCommand.class ? (BusCommand) cmd
                : cmd.infect(new BusCommand(false));
    }

    public String getEndpoint() {
        return getHeader(KEY_ENDPOINT);
    }

    public void setEndpoint(String endpoint) {
        setHeader(KEY_ENDPOINT, endpoint);
    }

    public Object getPayload() {
        return getParameter(KEY_PAYLOAD);
    }

    public void setPayload(Object payload) {
        setParameter(KEY_PAYLOAD, payload);
    }

}
