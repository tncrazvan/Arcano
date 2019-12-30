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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Administrator
 */
public class ShellScript {
    private String script;
    private final HashMap<String, String> query;
    private final String[] args;

    public ShellScript(String script, String[] args, HashMap<String, String> query) {
        this.script = script;
        this.args = args;
        this.query = query;
    }
    
    public ShellScript(String script, String[] args) {
        this.script = script;
        this.args = args;
        this.query = null;
    }
    
    public ShellScript(String script, HashMap<String, String> query) {
        this.script = script;
        this.args = null;
        this.query = query;
    }
    
    public ShellScript(String script) {
        this.script = script;
        this.args = null;
        this.query = null;
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
        
        final JsonObject pargs = new JsonObject();
        
        pargs.add("args", argsArray);
        pargs.add("query", queryObject);
        
        script = Regex.replace(script, "(?<!\\\\)\\$_INPUT", "\""+Base64.btoa(pargs.toString() , controller.so.config.charset)+"\"");
        script = Regex.replace(script, "\\$_INPUT", "$_INPUT");
        
        Process p = RUNTIME.exec(
                script,
                new String[]{}, 
                new File(controller.so.config.dir)
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
        
    }
}
