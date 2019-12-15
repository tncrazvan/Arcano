package com.github.tncrazvan.arcano.Tool;

import java.io.File;

/**
 *
 * @author Administrator
 * @param <T>
 */
public abstract class Action<T>{
    public File base = null;
    public abstract boolean callback(T o);
}