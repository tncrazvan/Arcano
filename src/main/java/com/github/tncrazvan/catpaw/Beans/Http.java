/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.catpaw.Beans;

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
public @interface Http {
    public String[] route() default {};
    public String[] method() default {"GET"};
    public Class<?>[] pack() default {Http.class};
}
