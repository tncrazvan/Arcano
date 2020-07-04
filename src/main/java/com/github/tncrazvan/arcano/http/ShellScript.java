/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 *
 * @author Razvan Tanase
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
    
    public final void execute(HttpEvent event) throws IOException, InterruptedException{
        final JsonArray argsArray = new JsonArray();
        for (String arg : event.request.reader.args) {
            argsArray.add(arg);
        }
        final JsonObject queryObject = new JsonObject();
        event.request.queryStrings.forEach((key, value) -> {
            queryObject.addProperty(key, value);
        });
        
        final JsonObject headersObject = new JsonObject();
        for(Entry<String,String> entry : event.request.reader.content.headers.getHashMap().entrySet()){
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
        tmp[tmp.length-1] = event.request.reader.content.body.length+"";
        
        ProcessBuilder builder = new ProcessBuilder(tmp);
        builder.directory(this.workspace == null?new File(event.so.config.dir):this.workspace);
        Process process = builder.start();
        try (OutputStream stdin = process.getOutputStream()) {
            if(overhead.length > 0)
                stdin.write(overhead);
            if(event.request.reader.content.body.length > 0)
                stdin.write(event.request.reader.content.body);
            stdin.flush();
            stdin.close();
        }
        
        process.waitFor(event.so.config.timeout,TimeUnit.MILLISECONDS);
        process.destroyForcibly();
        InputStream error = process.getErrorStream();
        final byte[] errors = error.readAllBytes();
        if(errors.length > 0){
            event.response.headers.set("Content-Type", "text/plain");
            event.response.headers.set("Content-Length", errors.length+"");
            event.push(errors);
        }else{
            String result = new String(process.getInputStream().readAllBytes());
            event.push(result,false);
        }
    }
}
