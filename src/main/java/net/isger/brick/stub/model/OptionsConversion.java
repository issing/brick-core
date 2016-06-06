package net.isger.brick.stub.model;

import java.util.List;
import java.util.Map;

import net.isger.util.Reflects;
import net.isger.util.reflect.conversion.Conversion;

public class OptionsConversion implements Conversion {

    public static final OptionsConversion CONVERSION = new OptionsConversion();

    private OptionsConversion() {
    }

    public boolean isSupport(Class<?> type) {
        return type.equals(Options.class);
    }

    public Object convert(Class<?> type, Object value) {
        Options options = new Options();
        if (value instanceof List) {
            for (Object i : (List<?>) value) {
                options.add(createOption(i));
            }
        } else {
            options.add(createOption(value));
        }
        return options;
    }

    @SuppressWarnings("unchecked")
    private Option createOption(Object value) {
        Option option;
        if (value instanceof Number) {
            option = new Option();
            option.setCode(((Number) value).intValue());
        } else if (value instanceof Map) {
            option = Reflects.newInstance(Option.class,
                    (Map<String, Object>) value);
        } else {
            throw new IllegalStateException("Unexpected class conversion for "
                    + value);
        }
        return option;
    }

    public String toString() {
        return Options.class.getName();
    }

}
