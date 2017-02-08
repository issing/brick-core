package net.isger.brick.stub.model;

import net.isger.util.anno.Affix;
import net.isger.util.anno.Alias;

@Alias("t_brick_stub_option")
public class Option implements Cloneable {

    /** 标识 */
    @Affix("{length : 20, options : [1, 3]}")
    private String id;

    @Affix("{length : 20, options : 3}")
    private String itemId;

    /** 类型 */
    @Affix("{length : 2, options : 3}")
    private Number type;

    /** 标签 */
    @Affix("{length : 50}")
    private String label;

    /** 名称 */
    @Affix("{length : 50}")
    private String name;

    /** 值 */
    @Affix("{type : text}")
    private String value;

    public Number getType() {
        return type;
    }

    public void setType(Number type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean equals(Object instance) {
        boolean result = instance instanceof Option;
        if (result) {
            return ((Option) instance).type == type;
        }
        return result;
    }

    public Option clone() {
        try {
            return (Option) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Failure to clone option", e);
        }
    }

}
