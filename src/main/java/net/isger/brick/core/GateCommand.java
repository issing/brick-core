package net.isger.brick.core;

import net.isger.util.Strings;

public class GateCommand extends BaseCommand {

    public static final String KEY_DOMAIN = "gate-domain";

    public static final String KEY_TRANSIENT = "gate-transient";

    public static final String OPERATE_INITIAL = "initial";

    public static final String OPERATE_CREATE = "create";

    public static final String OPERATE_REMOVE = "remove";

    public static final String OPERATE_DESTROY = "destroy";

    public GateCommand() {
    }

    public GateCommand(Command source) {
        super(source);
    }

    public GateCommand(boolean hasShell) {
        super(hasShell);
    }

    public static GateCommand getAction() {
        return cast(BaseCommand.getAction());
    }

    public static GateCommand cast(Command cmd) {
        return cast(BaseCommand.cast(cmd));
    }

    public static GateCommand cast(BaseCommand cmd) {
        return cmd == null || cmd.getClass() == GateCommand.class ? (GateCommand) cmd
                : cmd.infect(new GateCommand(false));
    }

    public final String getPermission() {
        StringBuffer buffer = new StringBuffer(128);
        String section = getDomain();
        if (Strings.isNotEmpty(section)) {
            buffer.append(section).append(":");
        }
        if (Strings.isNotEmpty(section = getAccess())) {
            buffer.append(section).append(":");
        }
        buffer.append(super.getPermission());
        return buffer.toString();
    }

    protected String getAccess() {
        return null;
    }

    public String getDomain() {
        return getHeader(KEY_DOMAIN);
    }

    public void setDomain(String domain) {
        setHeader(KEY_DOMAIN, domain);
    }

    public boolean getTransient() {
        Boolean transiented = getHeader(KEY_TRANSIENT);
        return transiented != null && transiented;
    }

    public void setTransient(boolean transiented) {
        setHeader(KEY_TRANSIENT, transiented);
    }

}
