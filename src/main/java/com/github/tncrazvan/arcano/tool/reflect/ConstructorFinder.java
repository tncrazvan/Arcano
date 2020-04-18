/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.tool.reflect;

import java.lang.reflect.Constructor;

/**
 *
 * @author Razvan Tanase
 */
public interface ConstructorFinder {
    public static Constructor<?> getNoParametersConstructor(final Class<?> cls) {
        for (final Constructor<?> constructor : cls.getDeclaredConstructors()) {
            if(constructor.getParameterCount() == 0)
                return constructor;
        }
        
        return null;
    }
}
