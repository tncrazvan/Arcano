package com.github.tncrazvan.arcano.Tool;

/**
 *
 * @author Administrator
 * @param <T>
 */
public abstract class Action<T>{
    public abstract void callback(T o);
}