package net.isger.brick.stub;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Command;
import net.isger.brick.core.GateCommand;

public class StubCommand extends GateCommand {

    // public static final String KEY_TRANSIENT = "stub-transient";

    public static final String CTRL_TRANSACTION = "stub-transaction";

    public static final String CTRL_TABLE = "stub-table";

    public static final String CTRL_OPERATE = "stub-operate";

    public static final String CTRL_CONDITION = "stub-condition";

    public static final String OPERATE_INSERT = "insert";

    public static final String OPERATE_DELETE = "delete";

    public static final String OPERATE_UPDATE = "update";

    public static final String OPERATE_SELECT = "select";

    public StubCommand() {
    }

    public StubCommand(Command cmd) {
        super(cmd);
    }

    public StubCommand(boolean hasShell) {
        super(hasShell);
    }

    public static StubCommand getAction() {
        return cast(BaseCommand.getAction());
    }

    public static StubCommand newAction() {
        return cast(BaseCommand.newAction());
    }

    public static StubCommand cast(BaseCommand cmd) {
        return cmd == null || cmd.getClass() == StubCommand.class
                ? (StubCommand) cmd : cmd.infect(new StubCommand(false));
    }

    public void useModel(Class<?> clazz) {
        setTable(getParameter(clazz));
    }

    public static Object getTable(BaseCommand cmd) {
        return cmd.getHeader(CTRL_TABLE);
    }

    public static void setTable(BaseCommand cmd, Object table) {
        cmd.setHeader(CTRL_TABLE, table);
    }

    public Object getTable() {
        return getTable(this);
    }

    public void setTable(Object table) {
        setTable(this, table);
    }

    public static Object getCondition(BaseCommand cmd) {
        return cmd.getHeader(CTRL_CONDITION);
    }

    public static void setCondition(BaseCommand cmd, Condition condition) {
        cmd.setHeader(CTRL_CONDITION, condition);
    }

    public static void setCondition(BaseCommand cmd, Object... condition) {
        cmd.setHeader(CTRL_CONDITION, condition);
    }

    public Object getCondition() {
        return getCondition(this);
    }

    public void setCondition(Condition condition) {
        setCondition(this, condition);
    }

    public void setCondition(Object... condition) {
        setCondition(this, condition);
    }

    public StubCommand clone() {
        return (StubCommand) super.clone();
    }

}
