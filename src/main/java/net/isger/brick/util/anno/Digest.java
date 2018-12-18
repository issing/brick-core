package net.isger.brick.util.anno;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 整理注解
 * 
 * @author issing
 */
@Target(ElementType.METHOD)
@Retention(RUNTIME)
public @interface Digest {

    Stage stage() default Stage.INITIAL;

    int value() default -1;

    public enum Stage {

        INITIAL(0),

        DESTROY(1);

        public static final int INITIAL_VALUE = 0;

        public static final int DESTROY_VALUE = 1;

        private final int value;

        private Stage(int value) {
            this.value = value;
        }

        public final int getNumber() {
            return value;
        }

        public static Stage forNumber(int value) {
            switch (value) {
            case INITIAL_VALUE:
                return INITIAL;
            case DESTROY_VALUE:
                return DESTROY;
            default:
                return null;
            }
        }

    }

}
