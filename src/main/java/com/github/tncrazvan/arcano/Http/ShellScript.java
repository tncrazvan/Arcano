/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Http;

import static com.github.tncrazvan.arcano.SharedObject.RUNTIME;
import com.github.tncrazvan.arcano.Tool.Encoding.Base64;
import com.github.tncrazvan.arcano.Tool.Regex;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Administrator
 */
public class ShellScript {
    private String script;
    private final HashMap<String, String> query;
    private final String[] args;
    private final String workspace;
    private final HttpRequest request;
    
    //CHANGE DIR FROM STRING
    public ShellScript(String workspace, String script, String[] args, HashMap<String, String> query, HttpRequest request) {
        this.workspace = workspace;
        this.script = script;
        this.args = args;
        this.query = query;
        this.request = request;
    }
    
    public ShellScript(String workspace, String script, String[] args, HashMap<String, String> query) {
        this(workspace,script,args,query,null);
    }
    
    public ShellScript(String workspace,String script, String[] args) {
        this(workspace,script,args,null,null);
    }
    
    public ShellScript(String workspace,String script) {
        this(workspace,script,null,null,null);
    }
    
    
    //CHANGE DIR FROM FILE
    public ShellScript(File workspace, String script, String[] args, HashMap<String, String> query,HttpRequest request) {
        this(workspace.getAbsolutePath(),script,args,query,request);
    }
    
    public ShellScript(File workspace, String script, String[] args, HashMap<String, String> query) {
        this(workspace.getAbsolutePath(),script,args,query,null);
    }
    
    public ShellScript(File workspace,String script, String[] args) {
        this(workspace.getAbsolutePath(),script,args,null,null);
    }
    
    public ShellScript(File workspace,String script) {
        this(workspace,script,null,null,null);
    }
    
    //NO DIR
    public ShellScript(String script, String[] args, HashMap<String, String> query,HttpRequest request) {
        this("",script,args,query,request);
    }
    public ShellScript(String script, String[] args, HashMap<String, String> query) {
        this("",script,args,query,null);
    }
    
    public ShellScript(String script, String[] args) {
        this("",script,args,null,null);
    }
    
    public ShellScript(String script) {
        this("",script,null,null,null);
    }
    
    public void execute(HttpEvent controller) throws IOException, InterruptedException{
        final JsonArray argsArray = new JsonArray();
        if(args != null)
        for (String arg : args) {
            argsArray.add(arg);
        }
        final JsonObject queryObject = new JsonObject();
        if(query != null)
        query.forEach((key, value) -> {
            queryObject.addProperty(key, value);
        });
        
        final JsonObject headersObject = new JsonObject();
        if(request.headers != null)
        for(Entry<String,String> entry : request.headers.getHashMap().entrySet()){
            headersObject.addProperty(entry.getKey(), entry.getValue());
        }
        
        final JsonObject pargs = new JsonObject();
        
        pargs.add("headers", headersObject);
        pargs.add("args", argsArray);
        pargs.add("query", queryObject);
        pargs.addProperty("body", new String(request.content,controller.config.charset));
        
        /*script = Regex.replace(script, "(?<!\\\\)\\$_INPUT", Base64.btoa(pargs.toString() , controller.so.config.charset));
        script = Regex.replace(script, "\\$_INPUT", "$_INPUT");*/
        
        ProcessBuilder pb = new ProcessBuilder("C:\\xampp\\php\\php.exe","C:\\Users\\RazvanTanase\\Documents\\JavaProjects\\MyApp\\server\\src\\main\\resources\\php\\index.php");
        pb.directory(this.workspace.equals("")?new File(controller.so.config.dir):new File(this.workspace));
        Process p = pb.start();
        p.waitFor(controller.so.config.timeout,TimeUnit.MILLISECONDS);
        p.destroyForcibly();
        InputStream error = p.getErrorStream();
        final byte[] errors = error.readAllBytes();
        if(errors.length > 0){
            controller.setResponseHeaderField("Content-Type", "text/plain");
            controller.setResponseHeaderField("Content-Length", errors.length+"");
            controller.send(errors);
        }else{
            String result = new String(p.getInputStream().readAllBytes());
            controller.send(result,false);
        }
        /*OutputStream pos = p.getOutputStream();

        InputStream fis = new ByteArrayInputStream(pargs.toString().getBytes(controller.config.charset));
        byte[] buffer = new byte[1024];
        int read = 0;
        while((read = fis.read(buffer)) != -1) {
            pos.write(buffer, 0, read);
        }
        fis.close();*/
        /*
        
        Process p = RUNTIME.exec(
                script,
                new String[]{}, 
                this.workspace.equals("")?new File(controller.so.config.dir):new File(this.workspace)
        );
        p.waitFor(controller.so.config.timeout,TimeUnit.MILLISECONDS);
        p.destroyForcibly();
        InputStream error = p.getErrorStream();
        final byte[] errors = error.readAllBytes();
        if(errors.length > 0){
            controller.setResponseHeaderField("Content-Type", "text/plain");
            controller.setResponseHeaderField("Content-Length", errors.length+"");
            controller.send(errors);
        }else{
            controller.send(p.getInputStream().readAllBytes(),false);
        }
        */
    }
}
