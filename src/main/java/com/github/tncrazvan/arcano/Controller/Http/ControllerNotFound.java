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
        String dir = configDir+"/"+scripts+"/Http";
        if(Files.exists(Path.of(dir))){
            if(args.length == 0){
                if(Files.exists(Path.of(dir+"/.js"))){
                    executor = new JavaScriptExecutor();
                    executor.execute(e, null, dir+"/.js", args, input);
                }else{
                    e.send("");
                }
            }else{
                for(int i=args.length;i>=0;i--){
                    String filename = dir + "/"+String.join("/", Arrays.copyOfRange(args, 0, i));
                    if(Files.exists(Path.of(filename+".js"))){
                        String[] ar = Arrays.copyOfRange(args,i,args.length);
                        executor = new JavaScriptExecutor();
                        executor.execute(e, null, filename+".js", ar, input);
                        break;
                    }
                    if(Files.exists(Path.of(filename+".JS"))){
                        String[] ar = Arrays.copyOfRange(args,i,args.length);
                        executor = new JavaScriptExecutor();
                        executor.execute(e, null, filename+".JS", ar, input);
                        break;
                    }
                    if(Files.exists(Path.of(filename+".jS"))){
                        String[] ar = Arrays.copyOfRange(args,i,args.length);
                        executor = new JavaScriptExecutor();
                        executor.execute(e, null, filename+".jS", ar, input);
                        break;
                    }
                    if(Files.exists(Path.of(filename+".Js"))){
                        String[] ar = Arrays.copyOfRange(args,i,args.length);
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
