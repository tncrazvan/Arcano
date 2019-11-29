package com.github.tncrazvan.arcano.Controller.Http;

import com.github.tncrazvan.arcano.Bean.NotFound;
import com.github.tncrazvan.arcano.Http.HttpController;
import com.github.tncrazvan.arcano.Tool.JavaScriptExecutor;
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
public class ControllerNotFound extends HttpController{    
    private JavaScriptExecutor executor;
    public void main() throws ScriptException, IOException {
        String filename = configDir+"/"+scripts+"/Http";
        if(Files.exists(Path.of(filename))){
            if(args.length == 0){
                if(Files.exists(Path.of(filename+"/.js"))){
                    executor = new JavaScriptExecutor();
                    executor.execute(e, null, filename+"/.js", args, input);
                }else{
                    e.send("");
                }
            }else{
                for(short i=0;i<args.length;i++){
                    filename +="/"+args[i];
                    if(Files.exists(Path.of(filename+".js"))){
                        String[] ar = i==args.length-1?new String[]{}:Arrays.copyOfRange(args,i+1,args.length);
                        executor = new JavaScriptExecutor();
                        executor.execute(e, null, filename+".js", ar, input);
                        break;
                    }
                    if(Files.exists(Path.of(filename+".JS"))){
                        String[] ar = i==args.length-1?new String[]{}:Arrays.copyOfRange(args,i+1,args.length);
                        executor = new JavaScriptExecutor();
                        executor.execute(e, null, filename+".JS", ar, input);
                        break;
                    }
                    if(Files.exists(Path.of(filename+".jS"))){
                        String[] ar = i==args.length-1?new String[]{}:Arrays.copyOfRange(args,i+1,args.length);
                        executor = new JavaScriptExecutor();
                        executor.execute(e, null, filename+".jS", ar, input);
                        break;
                    }
                    if(Files.exists(Path.of(filename+".Js"))){
                        String[] ar = i==args.length-1?new String[]{}:Arrays.copyOfRange(args,i+1,args.length);
                        executor = new JavaScriptExecutor();
                        executor.execute(e, null, filename+".Js", ar, input);
                        break;
                    }
                }
            }
        }else{
            e.send("");
        }
    }
}
