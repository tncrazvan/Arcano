/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool;

import java.io.File;

/**
 *
 * @author Administrator
 */
public interface FileSystem {
    static void explore(String dir,boolean recursive,Action c){
        explore(new File(dir),recursive,c);
    }
    
    static void explore(File dir,boolean recursive,Action c){
        File[] files = dir.listFiles();
        for (File file : files) {
            c.callback(file);
        }
    }
}
