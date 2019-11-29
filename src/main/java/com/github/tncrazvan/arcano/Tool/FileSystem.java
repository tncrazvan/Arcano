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
        final File[] files = dir.listFiles();
        for (final File file : files) {
            c.callback(file);
        }
    }
}
