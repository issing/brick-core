package net.isger.brick.stub.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    public void set(Object... options) {
        set((Options) OptionsConversion.CONVERSION.convert(options));
    }

    public void set(Option... options) {
        set(Arrays.asList(options));
    }

    public void set(Options options) {
        set(options == null ? null : options.options);
    }

    public void set(List<Option> options) {
        this.options.clear();
        if (options == null || options.size() == 0) {
            return;
        }
        this.options.addAll(options);
    }

    public List<Option> values() {
        return Collections.unmodifiableList(options);
    }

    public int size() {
        return options.size();
    }

}
