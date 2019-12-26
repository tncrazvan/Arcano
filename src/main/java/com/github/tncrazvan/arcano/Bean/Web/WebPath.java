package com.github.tncrazvan.arcano.Bean.Web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Administrator
 */


@Target(value = {ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface WebPath {
    public String name() default "/";
}
