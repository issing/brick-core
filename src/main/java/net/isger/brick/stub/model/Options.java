package net.isger.brick.stub.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Options implements Cloneable {

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

    public Options clone() {
        Options options;
        try {
            options = (Options) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Failure to clone options", e);
        }
        options.options = new ArrayList<Option>();
        for (Option option : this.options) {
            options.options.add(option.clone());
        }
        return options;
    }

}
