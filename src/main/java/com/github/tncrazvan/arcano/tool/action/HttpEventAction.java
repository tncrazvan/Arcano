package com.github.tncrazvan.arcano.tool.action;

import com.github.tncrazvan.arcano.http.HttpEvent;

/**
 * Make a callback and defined its return type (R) and its parameter type (P).
 * 
 * @author Razvan Tanase
 * @param <R> type of the returned object.
 */
public interface HttpEventAction<R>{
    public abstract R callback(HttpEvent e);
}