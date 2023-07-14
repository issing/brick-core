package net.isger.brick.stub.model;

import net.isger.util.anno.Affix;
import net.isger.util.anno.Alias;

@Alias("t_brick_stub_option")
public class Option implements Cloneable {

    /** 标识 */
    @Affix("{length : 20, options : [1, 3]}")
    private String id;

    /** 字段 */
    @Affix("{length : 20, options : 3}")
    private String itemId;

    /** 类型（0：默认；1：主键；2：外键；3：非空；4：唯一；5：检查；6：索引） */
    @Affix("{length : 2, options : 3}")
    private Number type;

    /** 标签 */
    @Affix("{length : 50}")
    private String label;

    /** 名称 */
    @Affix("{length : 50}")
    private String name;

    /** 项值 */
    @Affix("{type : text}")
    private Object value;

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

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
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
