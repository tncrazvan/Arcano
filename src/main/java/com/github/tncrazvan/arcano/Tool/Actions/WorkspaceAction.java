package com.github.tncrazvan.arcano.Tool.Actions;

import java.io.File;

/**
 *
 * @author Administrator
 * @param <T>
 */
public abstract class WorkspaceAction<T>{
    public File base = null;
    public abstract boolean callback(T o);
}