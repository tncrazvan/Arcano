package com.github.tncrazvan.arcano.Tool.System;

import java.io.File;

import com.github.tncrazvan.arcano.Tool.Actions.WorkspaceAction;

/**
 *
 * @author Razvan Tanase
 */
public interface FileSystem {
    /**
     * Explore the contents of a directory and calls a WorkspaceAction funcitonal interface while iterating files.
     * NOTE: retusn immediately without warning if the provided resource is not a valid directory.
     * @param dir directory to explore
     * @param recursive is true, then the method will be evaluated recursively for each subgroups of directories inside the provided directory.
     * @param action the action to execute for each encountered file.
     */
    static void explore(final String dir, final boolean recursive, final WorkspaceAction action) {
        explore(new File(dir), recursive, action);
    }

    /**
     * Explore the contents of a directory and calls a WorkspaceAction funcitonal interface while iterating files.
     * NOTE: retusn immediately without warning if the provided resource is not a valid directory.
     * @param dir directory to explore.
     * @param recursive is true, then the method will be evaluated recursively for each subgroups of directories inside the provided directory.
     * @param action the action to execute for each encountered file.
     */
    static void explore(final File dir, final boolean recursive, final WorkspaceAction action) {
        if(!dir.isDirectory()) return;
        if(action.getWorkspace() == null)
            action.setWorkspace(dir);
        final File[] files = dir.listFiles();
        if(files != null){
            //lookup directories
            for (final File file : files) {
                if(file.isDirectory() && action.callback(file) && recursive){
                    explore(file, recursive, action);
                }
            }
            //lookup files
            for (final File file : files) {
                if(!file.isDirectory() && action.callback(file) && recursive){
                    explore(file, recursive, action);
                }
            }
        }
    }
    
    /**
     * Removes the given directory.
     * 
     * @param directory directory to be removed.
     */
    public static void rmdir(final File directory) {
        final File[] files = directory.listFiles();
        if (files != null) { // some JVMs return null for empty dirs
            for (final File f : files) {
                if(f.isDirectory()) {
                    rmdir(f);
                } else {
                    f.delete();
                }
            }
        }
        directory.delete();
    }
}
