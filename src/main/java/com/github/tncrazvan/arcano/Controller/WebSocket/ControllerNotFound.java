package com.github.tncrazvan.arcano.Controller.WebSocket;

import com.github.tncrazvan.arcano.Bean.NotFound;
import com.github.tncrazvan.arcano.Tool.JavaScriptExecutor;
import com.github.tncrazvan.arcano.WebSocket.WebSocketController;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import javax.script.ScriptException;

/**
 *
 * @author razvan
 */
@NotFound
public class ControllerNotFound extends WebSocketController{
    private JavaScriptExecutor executor = null;
    @Override
    public void onOpen() {
        
        if(Files.exists(Path.of(configDir+"/"+scripts+"/WebSocket"))){
            String filename = configDir+"/"+scripts+"/WebSocket";
            try{
                for(short i=0;i<args.length;i++){
                    filename +="/"+args[i];
                    if(Files.exists(Path.of(filename+".js"))){
                        String[] ar = i==args.length-1?new String[]{}:Arrays.copyOfRange(args,i+1,args.length);
                        executor = new JavaScriptExecutor();
                        executor.execute(e, null, filename+".js", ar);
                        break;
                    }
                    if(Files.exists(Path.of(filename+".JS"))){
                        String[] ar = i==args.length-1?new String[]{}:Arrays.copyOfRange(args,i+1,args.length);
                        executor = new JavaScriptExecutor();
                        executor.execute(e, null, filename+".JS", ar);
                        break;
                    }
                    if(Files.exists(Path.of(filename+".jS"))){
                        String[] ar = i==args.length-1?new String[]{}:Arrays.copyOfRange(args,i+1,args.length);
                        executor = new JavaScriptExecutor();
                        executor.execute(e, null, filename+".jS", ar);
                        break;
                    }
                    if(Files.exists(Path.of(filename+".Js"))){
                        String[] ar = i==args.length-1?new String[]{}:Arrays.copyOfRange(args,i+1,args.length);
                        executor = new JavaScriptExecutor();
                        executor.execute(e, null, filename+".Js", ar);
                        break;
                    }
                }
            }catch(ScriptException | IOException e){
                
            }
            if(executor.onOpen.todo != null)
                executor.onOpen.todo.apply(null);
        }else
            e.close();
    }

    @Override
    public void onMessage(byte[] data) {
        if(executor != null && executor.onMessage.todo != null)
            executor.onMessage.todo.apply(new String(data));
    }

    @Override
    public void onClose() {
        if(executor != null && executor.onClose.todo != null)
            executor.onClose.todo.apply(null);
    }

}