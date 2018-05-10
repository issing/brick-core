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

    int value() default -1;

}
