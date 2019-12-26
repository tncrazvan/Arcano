/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool.Reflect;

import java.lang.reflect.Constructor;

/**
 *
 * @author Administrator
 */
public interface ConstructorFinder {
    public static Constructor<?> getNoParametersConstructor(Class<?> cls){
        for (Constructor<?> constructor : cls.getDeclaredConstructors()) {
            if(constructor.getParameterCount() == 0)
                return constructor;
        }
        
        return null;
    }
}
