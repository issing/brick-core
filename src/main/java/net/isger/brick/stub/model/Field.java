package net.isger.brick.stub.model;

import net.isger.brick.core.BaseCommand;
import net.isger.raw.Artifact;
import net.isger.raw.Depository;
import net.isger.raw.StringRaw;
import net.isger.util.Sqls;
import net.isger.util.Strings;
import net.isger.util.reflect.BoundField;
import net.isger.util.reflect.Converter;

public class Field {

    private String label;

    private String name;

    private String type;

    private int length;

    private int scale;

    private Options options;

    private Object value;

    static {
        Converter.addConversion(OptionsConversion.CONVERSION);
    }

    public Field() {
        this((Object[]) null);
    }

    public Field(Object... args) {
        options = new Options();
        if (args != null) {
            switch (args.length) {
            case 4:
                this.length = (Integer) args[3];
            case 3:
                this.type = (String) args[2];
            case 2:
                this.name = (String) args[1];
            case 1:
                this.label = (String) args[0];
            }
        }
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public Option[] getOptions() {
        return options.gets();
    }

    public void addOption(Option option) {
        options.add(option);
    }

    public boolean hasOption() {
        return options.size() > 0;
    }

    public Object getValue() {
        if (value == null) {
            BaseCommand cmd = BaseCommand.getAction();
            if (cmd != null) {
                value = cmd.getParameter(name);
            }
        }
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public static Field create(BoundField field) {
        Field result = new Field();
        String affix = field.getAffix();
        if (Strings.isNotEmpty(affix)) {
            Artifact artifact = Depository.getArtifact(new StringRaw(affix));
            if (artifact != null) {
                result = artifact.transform(Field.class);
            }
        }
        if (Strings.isEmpty(result.name)) {
            result.name = field.getAlias();
            if (Strings.isEmpty(result.name)) {
                result.name = Sqls.toColumnName(field.getName());
            }
        }
        if (Strings.isEmpty(result.type)) {
            result.type = field.getField().getType().getSimpleName();
        }
        return result;
    }
}
