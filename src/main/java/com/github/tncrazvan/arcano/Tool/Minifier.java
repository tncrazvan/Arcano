package com.github.tncrazvan.arcano.Tool;

import static com.github.tncrazvan.arcano.Tool.Encoding.JsonTools.jsonArray;
import com.google.gson.JsonArray;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Administrator
 */
public class Minifier{
    private final String inputDirName;
    private final File assets;
    private final String outputDirectoryname = "minified";
    private final String outputFilename = "minified";
    private final File minifiedJS;
    private final File minifiedCSS;
    private File dir;

    public Minifier(File assetsFile, String inputDirName, String outputSubDirName) throws IOException {
        new HashMap<>();
        this.assets = assetsFile;
        this.inputDirName = inputDirName;
        dir = new File(inputDirName+outputDirectoryname);
        minifiedJS = new File(inputDirName+outputDirectoryname+"/"+outputFilename+".js");
        minifiedCSS = new File(inputDirName+outputDirectoryname+"/"+outputFilename+".css");
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
    private String[] filenames;
    private final String REGEX_PATTERN_CALL = "\\/+\\$regex[:\\s].*";
    private final String REGEX_PATTERN_CALL_VALUE = "(?<=\\/+\\$regex[:\\s]).*";
    Pattern pattern;
    Matcher matcher;
    public void minify() throws IOException{
        minify(true);
    }
    public void minify(boolean min) throws IOException{
        js = "";
        css = "";
        JsonArray arr = new JsonArray();
        try (FileInputStream fis = new FileInputStream(assets)) {
            arr = jsonArray(new String(fis.readAllBytes()));
            fis.close();
        }
        size = arr.size();
        try{
            for(int i=0;i<size;i++){
                filename = inputDirName+arr.get(i).getAsString();
                pattern = Pattern.compile(REGEX_PATTERN_CALL);
                matcher = pattern.matcher(filename);
                if(matcher.find()){
                    ArrayList<String> tmpFilenames = new ArrayList<>();
                    String dirname = filename.replaceAll(REGEX_PATTERN_CALL, "");
                    dir = new File(dirname);
                    if(!dir.isDirectory()) continue;
                    pattern = Pattern.compile(REGEX_PATTERN_CALL);
                    matcher = pattern.matcher(filename);
                    if(!matcher.find()) continue;
                    //gets the first match only, I don't care about the rest
                    String regexCall = matcher.group();
                    pattern = Pattern.compile(REGEX_PATTERN_CALL_VALUE);
                    matcher = pattern.matcher(regexCall);
                    if(!matcher.find()) continue;
                    String regexValue = matcher.group();
                    pattern = Pattern.compile(regexValue);
                    for(String listedFilename : dir.list()){
                        matcher = pattern.matcher(listedFilename);
                        if(!matcher.find())
                            continue;
                        tmpFilenames.add(dir+"/"+listedFilename);
                    }
                    filenames = tmpFilenames.toArray(filenames);
                }else{
                    if(!filename.endsWith(".js") && !filename.endsWith(".css") && !filename.endsWith(".html") && !filename.endsWith(".htm")) continue;
                    filenames = new String[]{filename};
                }
                
                
                for(String listedFilename : filenames){
                    if(listedFilename == null) continue;
                    f = new File(listedFilename);
                    fis = new FileInputStream(f);
                    if(listedFilename.endsWith(".js")){
                        js += min?new String(minify(fis.readAllBytes(),"js",this.hashCode()+"")):new String(fis.readAllBytes());
                    }else if(listedFilename.endsWith(".css")){
                        css += min?new String(minify(fis.readAllBytes(),"css",this.hashCode()+"")):new String(fis.readAllBytes());
                    }

                    fis.close();
                }
                
            }
        }catch(IOException e){
            e.printStackTrace(System.out);
        }
        
        
        fis=null;
        
        save(dir,minifiedJS,js.getBytes());
        save(dir,minifiedCSS,css.getBytes());
    }
    
    
    private void save(File dir,File minified,byte[] contents){
        try{
            if(!dir.exists())
                dir.mkdir();


            if(minified.exists())
                minified.delete();
            minified.createNewFile();
            try (FileOutputStream fos = new FileOutputStream(minified)) {
                fos.write(contents);
                fos.close();
            }
        }catch(IOException e){}
    }
}
 