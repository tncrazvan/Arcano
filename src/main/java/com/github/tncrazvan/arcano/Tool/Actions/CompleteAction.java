package com.github.tncrazvan.arcano.Tool.Actions;

/**
 *
 * @author Administrator
 * @param <R>
 * @param <T>
 */
public interface CompleteAction<R,T>{
    public abstract R callback(T e);
}