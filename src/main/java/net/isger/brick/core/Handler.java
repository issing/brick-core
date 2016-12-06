package net.isger.brick.core;

public interface Handler {

    public Object handle(Object message);

    public static final Handler NOP = new Handler() {

        public Object handle(Object message) {
            return message;
        }

    };

}
