package com.github.tncrazvan.arcano.Tool.System;

import com.github.tncrazvan.arcano.Tool.Actions.WorkspaceAction;
import java.io.File;

/**
 *
 * @author Administrator
 */
public interface FileSystem {
    static void explore(final String dir, final boolean recursive, final WorkspaceAction c) {
        explore(new File(dir), recursive, c);
    }

    static void explore(final File dir, final boolean recursive, final WorkspaceAction c) {
        if(!dir.isDirectory()) return;
        if(c.base == null)
            c.base = dir;
        final File[] files = dir.listFiles();
        if(files != null){
            //lookup directories
            for (final File file : files) {
                if(file.isDirectory() && c.callback(file) && recursive){
                    explore(file, recursive, c);
                }
            }
            //lookup files
            for (final File file : files) {
                if(!file.isDirectory() && c.callback(file) && recursive){
                    explore(file, recursive, c);
                }
            }
        }
    }
    
    /**
     * Removes the given directory.
     * 
     * @param directory directory to be removed.
     */
    public static void rmdir(File directory){
        File[] files = directory.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
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
