package net.isger.brick.stub;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Command;
import net.isger.brick.core.GateCommand;

public class StubCommand extends GateCommand {

    public static final String OPERATE_INSERT = "insert";

    public static final String OPERATE_DELETE = "delete";

    public static final String OPERATE_UPDATE = "update";

    public static final String OPERATE_SELECT = "select";

    // public static final String KEY_TRANSIENT = "stub-transient";

    public static final String KEY_TRANSACTION = "stub-transaction";

    public static final String KEY_TABLE = "stub-table";

    public static final String KEY_OPERATE = "stub-operate";

    public static final String KEY_CONDITION = "stub-condition";

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
        return cmd == null || cmd.getClass() == StubCommand.class ? (StubCommand) cmd
                : cmd.infect(new StubCommand(false));
    }

    public void useModel(Class<?> clazz) {
        setTable(getParameter(clazz));
    }

    public static Object getTable(BaseCommand cmd) {
        return cmd.getHeader(KEY_TABLE);
    }

    public static void setTable(BaseCommand cmd, Object table) {
        cmd.setHeader(KEY_TABLE, table);
    }

    public Object getTable() {
        return getTable(this);
    }

    public void setTable(Object table) {
        setTable(this, table);
    }

    public static Object getCondition(BaseCommand cmd) {
        return cmd.getHeader(KEY_CONDITION);
    }

    public static void setCondition(BaseCommand cmd, Condition condition) {
        cmd.setHeader(KEY_CONDITION, condition);
    }

    public static void setCondition(BaseCommand cmd, Object... condition) {
        cmd.setHeader(KEY_CONDITION, condition);
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

}
