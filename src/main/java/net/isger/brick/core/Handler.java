package net.isger.brick.core;

public interface Handler {

    public int getStatus();

    public Object handle(Object message);

    public static final Handler NOP = new Handler() {

        public int getStatus() {
            return 1;
        }

        public Object handle(Object message) {
            return message;
        }

    };

}
