package net.isger.brick.stub.dialect;

import net.isger.brick.stub.model.Meta;
import net.isger.brick.stub.model.Option;

public class DescriberAdapter implements Describer {

    private String name;

    protected DescriberAdapter() {
    }

    protected DescriberAdapter(String name) {
        this.name = name;
    }

    public String describe(Meta field) {
        return null;
    }

    public String describe(Option option, Object... extents) {
        return null;
    }

    public String getName() {
        return name;
    }

}
