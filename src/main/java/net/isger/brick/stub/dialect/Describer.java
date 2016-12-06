package net.isger.brick.stub.dialect;

import net.isger.brick.stub.model.Meta;
import net.isger.brick.stub.model.Option;

public interface Describer {

    public String describe(Meta field);

    public String describe(Option option, Object... extents);

}
