package com.github.tncrazvan.arcano.Tool.Actions;

/**
 *
 * @author Administrator
 * @param <T>
 */
public interface TypedAction<T>{
    public abstract void callback(T e);
}