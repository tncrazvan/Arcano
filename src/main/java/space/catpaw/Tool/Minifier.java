/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package space.catpaw.Tool;

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
public class Minifier implements JsonTools{
    private final HashMap<String,Long> updatesMap;
    private final String inputDirName;
    private final JsonArray assets;
    private final String outputDirectoryname = "minified";
    private final String outputFilename = "minified";
    public Minifier(File assetsFile,String inputDirName,String outputSubDirName) throws IOException {
        this.updatesMap = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(assetsFile)) {
            this.assets = toJsonArray(new String(fis.readAllBytes())).getAsJsonArray();
        }
        this.inputDirName = inputDirName;
    }
    
    public static byte[] minify(byte[] content,String type) throws IOException{
        return minify(content, type, Thread.currentThread().getId()+"");
    }
    
    public static byte[] minify(byte[] content,String type,String hashCode) throws IOException{
        File tmp = new File("tmp");
        if(!tmp.exists())
            tmp.mkdir();
        else if(!tmp.isDirectory()){
            tmp.delete();
            tmp.mkdir();
        }
        
        tmp = new File("tmp/."+hashCode+"."+type+".minified.input.tmp");
        if(tmp.exists())
            tmp.delete();
        tmp.createNewFile();
        try (FileOutputStream fos = new FileOutputStream(tmp)) {
            fos.write(content);
        }
        
        Process process;
        String filename = tmp.toPath().toAbsolutePath().toString().replace("\\", "/");
        String cmd = String.format("minify --type=%s \"%s\"", type, filename);
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
        }catch(IOException e){
            e.printStackTrace(System.out);
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
        try (FileOutputStream fos = new FileOutputStream(minified)) {
            fos.write(contents);
        }
    }
}
 