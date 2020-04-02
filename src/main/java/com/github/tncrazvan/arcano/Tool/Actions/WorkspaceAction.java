package com.github.tncrazvan.arcano.Tool.Actions;

import java.io.File;

/**
 * Make a generic callback that returns a boolean and expects a <T> as parameter.
 * This class differs from the ReturnedAction class in that it defines a property "workspace" (which is null by default) 
 * and the callback returns a boolean instead of being void.
 * @author Razvan Tanase
 * @param <T> this is the type of the input parameter expected in the callback method.
 */
public abstract class WorkspaceAction<T>{
    public File workspace = null;
    public abstract boolean callback(T o);

    public final File getWorkspace(){
        return this.workspace;
    }

    public final void setWorkspace(final File workspace) {
        this.workspace = workspace;
    }
}