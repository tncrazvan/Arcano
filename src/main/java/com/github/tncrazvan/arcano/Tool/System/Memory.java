/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool.System;

import static com.github.tncrazvan.arcano.SharedObject.RUNTIME;

/**
 *
 * @author RazvanTanase
 */
public interface Memory {
    public static long getFreeMemory(){
        return RUNTIME.freeMemory();
    }
    public static long getAllocatedMemory(){
        return RUNTIME.totalMemory();
    }
    public static long getMaxMemory(){
        return RUNTIME.maxMemory();
    }
    public static long getTotalFreeMemory(){
        return (getFreeMemory() + (getMaxMemory() - getAllocatedMemory())) / 1024;
    }
}
