package net.isger.brick.stub.model;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.isger.util.Asserts;
import net.isger.util.Reflects;
import net.isger.util.reflect.ClassAssembler;
import net.isger.util.reflect.conversion.Conversion;

public class OptionsConversion implements Conversion {

    private static OptionsConversion INSTANCE;

    private OptionsConversion() {
    }

    public static OptionsConversion getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OptionsConversion();
        }
        return INSTANCE;
    }

    public boolean isSupport(Type type) {
        return Options.class.equals(Reflects.getRawClass(type));
    }

    public Object convert(Object value) {
        return convert(Options.class, value, null);
    }

    public Object convert(Type type, Object value, ClassAssembler assembler) {
        Options options = new Options();
        if (value instanceof Object[]) {
            value = Arrays.asList((Object[]) value);
        }
        if (value instanceof List) {
            for (Object i : (List<?>) value) {
                options.add(createOption(i, assembler));
            }
        } else {
            options.add(createOption(value, assembler));
        }
        return options;
    }

    @SuppressWarnings("unchecked")
    private Option createOption(Object value, ClassAssembler assembler) {
        Option option;
        if (value instanceof Number) {
            option = new Option();
            option.setType((Number) value);
        } else if (value instanceof Map) {
            option = Reflects.newInstance(Option.class, (Map<String, Object>) value, assembler);
        } else if (value instanceof Option) {
            option = (Option) value;
        } else {
            throw Asserts.state("Unexpected class conversion for %s", value);
        }
        return option;
    }

    public String toString() {
        return Options.class.getName();
    }

}
