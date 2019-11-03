/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool;

import static com.github.tncrazvan.arcano.Common.STATUS_SUCCESS;
import static com.github.tncrazvan.arcano.Common.js;
import com.github.tncrazvan.arcano.Http.HttpEvent;
import com.github.tncrazvan.arcano.WebSocket.WebSocketEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

/**
 *
 * @author Administrator
 */
public class JavaScriptExecutor {
    private final static String NASHORN_ARGS = "nashorn.args";
    private final static String ES_6 = "--language=es6 --no-deprecation-warning";
    
    private abstract class JavaScriptCurrentContext{
        final String dirname;

        public JavaScriptCurrentContext(String dirname) {
            this.dirname = dirname;
        }
        
    }
    
    private final class JSFile extends JavaScriptCurrentContext implements Function<String, File> {
        public JSFile(String dirname) {
            super(dirname);
        }
        
        @Override
        public File apply(String filename) {
            return new File(dirname+"/"+filename);
        }
    }
    
    
    //Http
    public void execute(HttpEvent e,ScriptContext context,String filename,String[] args,StringBuilder content) throws ScriptException, IOException{
        ScriptEngineManager mgr = new ScriptEngineManager();
        if(js == null){
            System.setProperty(NASHORN_ARGS, ES_6);
            js = mgr.getEngineByName("nashorn");
        }
        eval(e,context,filename,args,content);
    }
    
    public final class JSLog implements Function<String, Void>{
        public Function todo;
        
        @Override
        public Void apply(String message) {
            System.out.println(message);
            return null;
        }
    }
    
    public final class EventListener<T> implements Function<Function<T,Void>, Void>{
        public Function todo;
        
        @Override
        public Void apply(Function<T,Void> todo) {
            this.todo = todo;
            return null;
        }
    }
    
    //Http
    private void eval(HttpEvent e,ScriptContext context,String filename,String[] args,StringBuilder content) throws ScriptException, IOException{
        String dirname = Path.of(filename).getParent().toString();
        e.setStatus(STATUS_SUCCESS);
        String script = Files.readString(Path.of(filename));
        if(context != null)
            js.setContext(context);
        
        
        js.eval("function send(data){server.send(data);}\nfunction require(filename){load('"+dirname.replace("\\", "/")+"/'+filename);}\n"+script+"\nmain();",new SimpleBindings(
            new HashMap<String,Object>(){{
                put("args",args);
                put("log",new JSLog());
                put("method",e.getMethod());
                put("content",content.toString());
                put("server",e);
                put("File",new JSFile(dirname));
            }}
        ));
    }
    
    
    
    //WebSockets
    public void execute(WebSocketEvent e,ScriptContext context,String filename,String[] args) throws ScriptException, IOException{
        ScriptEngineManager mgr = new ScriptEngineManager();
        if(js == null){
            System.setProperty(NASHORN_ARGS, ES_6);
            js = mgr.getEngineByName("nashorn");
        }
        eval(e,context,filename,args);
    }
    
    public EventListener<Void> onOpen;
    public EventListener<String> onMessage;
    public EventListener<Void> onClose;
    //WebSockets
    private void eval(WebSocketEvent e,ScriptContext context,String filename,String[] args) throws ScriptException, IOException{
        String dirname = Path.of(filename).getParent().toString();
        String script = Files.readString(Path.of(filename));
        if(context != null)
            js.setContext(context);
        
        onOpen = new EventListener<>();
        onMessage = new EventListener<>();
        onClose = new EventListener<>();
        
        Object o = js.eval("function send(data){server.send(data);}\nfunction require(filename){load('"+dirname.replace("\\", "/")+"/'+filename);}\n"+script+"\nmain();",new SimpleBindings(
            new HashMap<String,Object>(){{
                put("args",args);
                put("log",new JSLog());
                put("server",e);
                put("File",new JSFile(dirname));
                put("onOpen",onOpen);
                put("onMessage",onMessage);
                put("onClose",onClose);
            }}
        ));
        
    }
}
