package net.isger.brick.stub.model;

import java.util.ArrayList;
import java.util.List;

public class Options {

    private List<Option> options;

    public Options() {
        options = new ArrayList<Option>();
    }

    public void add(Option option) {
        if (!options.contains(option)) {
            options.add(option);
        }
    }

    public Option[] gets() {
        return options.toArray(new Option[options.size()]);
    }

    public int size() {
        return options.size();
    }

}
