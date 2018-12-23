package net.isger.brick.stub.dialect;

import net.isger.brick.stub.model.Meta;
import net.isger.brick.stub.model.Option;

public class DescriberDelegate implements Describer {

    private Describer source;

    public DescriberDelegate(Describer source) {
        this.source = source;
    }

    public String describe(Meta field) {
        return source.describe(field);
    }

    public String describe(Option option, Object... extents) {
        return source.describe(option, extents);
    }

}
