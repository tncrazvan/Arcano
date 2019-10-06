/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.catpaw.Tools;

import java.io.File;

/**
 *
 * @author Administrator
 */
public class FileSystem {
    public static void explore(String dir,boolean recursive,Callable c){
        explore(new File(dir),recursive,c);
    }
    
    public static void explore(File dir,boolean recursive,Callable c){
        File[] files = dir.listFiles();
        for(int i=0;i<files.length;i++){
            c.callback(files[i]);
        }
    }
}
