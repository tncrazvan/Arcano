package com.github.tncrazvan.arcano.Tool;

import java.io.File;

/**
 *
 * @author Administrator
 */
public interface FileSystem {
    static void explore(final String dir, final boolean recursive, final Action c) {
        explore(new File(dir), recursive, c);
    }

    static void explore(final File dir, final boolean recursive, final Action c) {
        if(!dir.isDirectory()) return;
        final File[] files = dir.listFiles();
        if(files != null)
            for (final File file : files) {
                if(c.callback(file) && recursive){
                    explore(file, recursive, c);
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
