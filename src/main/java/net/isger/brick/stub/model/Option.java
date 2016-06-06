package net.isger.brick.stub.model;

public class Option {

    private int code;

    private String name;

    private Object value;

    public Option() {
    }

    public Option(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
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

    public boolean equals(Object value) {
        return value instanceof Option ? ((Option) value).code == code : false;
    }

}
