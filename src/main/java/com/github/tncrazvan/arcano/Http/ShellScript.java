/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Http;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Administrator
 */
public class ShellScript {
    private String[] script;
    private final File workspace;
    
    //CHANGE DIR FROM STRING
    public ShellScript(File workspace, String... script) {
        this.workspace = workspace;
        this.script = script;
    }
    
    //CHANGE DIR FROM STRING
    public ShellScript(String... script) {
        this.workspace = null;
        this.script = script;
    }
    
    public void execute(HttpEvent controller) throws IOException, InterruptedException{
        final JsonArray argsArray = new JsonArray();
        for (String arg : controller.args) {
            argsArray.add(arg);
        }
        final JsonObject queryObject = new JsonObject();
        controller.queryString.forEach((key, value) -> {
            queryObject.addProperty(key, value);
        });
        
        final JsonObject headersObject = new JsonObject();
        for(Entry<String,String> entry : controller.reader.request.headers.getHashMap().entrySet()){
            headersObject.addProperty(entry.getKey(), entry.getValue());
        }
        
        final JsonObject pargs = new JsonObject();
        
        pargs.add("HEADERS", headersObject);
        pargs.add("ARGS", argsArray);
        pargs.add("QUERY", queryObject);
        pargs.addProperty("BODY", new String(controller.reader.request.content,controller.config.charset));
        
        /*script = Regex.replace(script, "(?<!\\\\)\\$_INPUT", Base64.btoa(pargs.toString() , controller.so.config.charset));
        script = Regex.replace(script, "\\$_INPUT", "$_INPUT");*/
        
        String data = pargs.toString();
        
        ProcessBuilder builder = new ProcessBuilder(script[0],script[1],data.length()+"");
        builder.directory(this.workspace == null?new File(controller.so.config.dir):this.workspace);
        Process process = builder.start();
        
        OutputStream stdin = process.getOutputStream();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin))) {
            writer.write(data);
            writer.flush();
        }
        
        process.waitFor(controller.so.config.timeout,TimeUnit.MILLISECONDS);
        process.destroyForcibly();
        InputStream error = process.getErrorStream();
        final byte[] errors = error.readAllBytes();
        if(errors.length > 0){
            controller.setResponseHeaderField("Content-Type", "text/plain");
            controller.setResponseHeaderField("Content-Length", errors.length+"");
            controller.send(errors);
        }else{
            String result = new String(process.getInputStream().readAllBytes());
            controller.send(result,false);
        }
    }
}
