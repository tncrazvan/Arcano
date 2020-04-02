package com.github.tncrazvan.arcano.Tool.Actions;

/**
 * Make a callback and define its return type (R).
 * @author Razvan Tanase
 * @param <R> type of the returned object.
 */
public interface ReturnedAction<R>{
    public abstract R callback();
}