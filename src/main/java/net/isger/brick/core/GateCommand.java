package net.isger.brick.core;

import net.isger.util.Helpers;
import net.isger.util.Strings;

public class GateCommand extends BaseCommand {

    public static final String CTRL_DOMAIN = "gate-domain";

    public static final String CTRL_TRANSIENT = "gate-transient";

    public static final String CTRL_IMMEDIATE = "gate-immediate";

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
        return cmd == null || cmd.getClass() == GateCommand.class ? (GateCommand) cmd : cmd.infect(new GateCommand(false));
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

    public static String getDomain(BaseCommand cmd) {
        return cmd.getHeader(CTRL_DOMAIN);
    }

    public static void setDomain(BaseCommand cmd, String domain) {
        cmd.setHeader(CTRL_DOMAIN, domain);
    }

    public String getDomain() {
        return getDomain(this);
    }

    public void setDomain(String domain) {
        setDomain(this, domain);
    }

    public static boolean getTransient(BaseCommand cmd) {
        return Helpers.toBoolean(cmd.getHeader(CTRL_TRANSIENT));
    }

    public static void setTransient(BaseCommand cmd, boolean transiented) {
        cmd.setHeader(CTRL_TRANSIENT, transiented);
    }

    public boolean getTransient() {
        return getTransient(this);
    }

    public void setTransient(boolean transiented) {
        setTransient(this, transiented);
    }

    public static boolean getImmediate(BaseCommand cmd) {
        return Helpers.toBoolean(cmd.getHeader(CTRL_IMMEDIATE));
    }

    public static void setImmediate(BaseCommand cmd, boolean immediated) {
        cmd.setHeader(CTRL_IMMEDIATE, immediated);
    }

    public boolean getImmediate() {
        return getImmediate(this);
    }

    public void setImmediate(boolean immediated) {
        setImmediate(this, immediated);
    }

}
