/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.catpaw.Tools;

import com.github.tncrazvan.catpaw.Server;
import com.google.gson.JsonArray;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 *
 * @author Administrator
 */
public class Minifier {
    private HashMap<String,Long> updatesMap;
    private String inputDirName;
    private String outputSubDirName;
    private boolean isWindows;
    private Consumer<String> consumer;
    private File root;
    private final String JS_PATTERN = "^.+\\.js$";
    private final String CSS_PATTERN = "^.+\\.css$";
    private JsonArray assets;
    private File assetsFile;
    private final String inputFilename = "assets.json";
    private final String outputDirectoryname = "minified";
    private final String outputFilename = "minified";
    public Minifier(String inputDirName,String outputSubDirName, Consumer<String> consumer) throws IOException {
        this.updatesMap = new HashMap<>();
        assetsFile = new File(inputDirName+inputFilename);
        FileInputStream fis = new FileInputStream(assetsFile);
        this.assets = Server.JSONPARSER.parse(new String(fis.readAllBytes())).getAsJsonArray();
        fis.close();
        fis=null;
        
        this.inputDirName = inputDirName;
        this.outputSubDirName = outputSubDirName;
        this.isWindows = System
                        .getProperty("os.name")
                        .toLowerCase()
                        .startsWith("windows");
        this.consumer = consumer;
        this.root = new File(inputDirName);
    }
    
    public static byte[] minify(byte[] content,String type) throws IOException{
        return minify(content, "",type);
    }
    
    public static byte[] minify(byte[] content,String type,String hashCode) throws IOException{
        File tmp = new File("tmp");
        if(!tmp.exists())
            tmp.mkdir();
        else if(!tmp.isDirectory()){
            tmp.delete();
            tmp.mkdir();
        }
        
        tmp = new File("tmp/."+Thread.currentThread().getId()+String.format("."+type+".minified.input.tmp", type));
        if(tmp.exists())
            tmp.delete();
        tmp.createNewFile();
        FileOutputStream fos = new FileOutputStream(tmp);
        fos.write(content);
        fos.close();
        
        Process process;
        String cmd = String.format("minify --type=%s %s", type, tmp.toPath().toAbsolutePath().toString());
        process = Runtime.getRuntime().exec(cmd);
        byte[] result =  process.getInputStream().readAllBytes();
        process.destroy();
        tmp.delete();
        return result;
    }
    
    
    
    private String js="";
    private String css="";
    private File f;
    private FileInputStream fis;
    private int size;
    private String filename,key;
    public void minify() throws IOException{
        size = assets.size();
        boolean changes = false;
        try{
            for(int i=0;i<size;i++){
                filename = inputDirName+assets.get(i).getAsString();
                if(!filename.endsWith(".js") && !filename.endsWith(".css") && !filename.endsWith(".html") && !filename.endsWith(".htm")) continue;
                f = new File(filename);
                fis = new FileInputStream(f);
                if(filename.endsWith(".js")){
                    key = f.toPath().toString();
                    if(!updatesMap.containsKey(key) || (long)updatesMap.get(key) < f.lastModified()){
                        changes = true;
                        updatesMap.put(key, f.lastModified());
                        js += new String(minify(fis.readAllBytes(),"js",this.hashCode()+""));
                    }
                }else if(filename.endsWith(".css")){
                    key = f.toPath().toString();
                    if(!updatesMap.containsKey(key) || (long)updatesMap.get(key) < f.lastModified()){
                        changes = true;
                        updatesMap.put(key, f.lastModified());
                        css += new String(minify(fis.readAllBytes(),"css",this.hashCode()+""));
                    }
                }
                
                fis.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        
        fis=null;
        
        if(!changes) return;
        
        save(js.getBytes(), "js");
        save(css.getBytes(), "css");
    }
    
    private File minified;
    private File dir;
    private void save(byte[] contents,String type) throws IOException{
        dir = new File(inputDirName+outputDirectoryname);
        if(!dir.exists())
            dir.mkdir();
        minified = new File(inputDirName+outputDirectoryname+"/"+outputFilename+"."+type);
        if(minified.exists())
            minified.delete();
        minified.createNewFile();
        FileOutputStream fos = new FileOutputStream(minified);
        fos.write(contents);
        fos.close();
    }
}
