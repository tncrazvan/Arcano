/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Http;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Administrator
 */
public class ShellScript {
    private String program;
    private String[] args;
    private final File workspace;
    
    //CHANGE DIR FROM STRING
    public ShellScript(File workspace, String program, String... args) {
        this.workspace = workspace;
        this.program = program;
        this.args = args;
    }
    
    //CHANGE DIR FROM STRING
    public ShellScript(String program, String... script) {
        this.workspace = null;
        this.program = program;
        this.args = script;
    }
    
    public final void execute(HttpEvent controller) throws IOException, InterruptedException{
        final JsonArray argsArray = new JsonArray();
        for (String arg : controller.reader.args) {
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
        
        JsonArray overheadArray = new JsonArray();
        overheadArray.add(headersObject);
        overheadArray.add(argsArray);
        overheadArray.add(queryObject);
        byte[] overhead = overheadArray.toString().getBytes();
        
        String[] tmp = new String[args.length+3];
        tmp[0] = this.program;
        for(int i=0; i< args.length; i++){
            tmp[i+1] = args[i];
        }
        tmp[tmp.length-2] = overhead.length+"";
        tmp[tmp.length-1] = controller.reader.request.content.length+"";
        
        ProcessBuilder builder = new ProcessBuilder(tmp);
        builder.directory(this.workspace == null?new File(controller.reader.so.config.dir):this.workspace);
        Process process = builder.start();
        try (OutputStream stdin = process.getOutputStream()) {
            if(overhead.length > 0)
                stdin.write(overhead);
            if(controller.reader.request.content.length > 0)
                stdin.write(controller.reader.request.content);
            stdin.flush();
            stdin.close();
        }
        
        process.waitFor(controller.reader.so.config.timeout,TimeUnit.MILLISECONDS);
        process.destroyForcibly();
        InputStream error = process.getErrorStream();
        final byte[] errors = error.readAllBytes();
        if(errors.length > 0){
            controller.setResponseHeaderField("Content-Type", "text/plain");
            controller.setResponseHeaderField("Content-Length", errors.length+"");
            controller.push(errors);
        }else{
            String result = new String(process.getInputStream().readAllBytes());
            controller.push(result,false);
        }
    }
}
