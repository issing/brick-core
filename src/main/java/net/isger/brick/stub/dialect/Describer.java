package net.isger.brick.stub.dialect;

import net.isger.brick.stub.model.Field;
import net.isger.brick.stub.model.Option;

public interface Describer {

    public String describe(Field field);

    public String describe(Option option, Object... extents);

}
