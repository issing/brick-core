package net.isger.brick.bus;

import net.isger.util.Manageable;
import net.isger.util.Operator;

public interface Endpoint extends Operator, Manageable {

    public static final String BRICK_ENDPOINT = "brick.core.endpoint";

    public Status getStatus();

}
