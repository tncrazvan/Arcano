package com.github.tncrazvan.arcano.Controller.Http;

import com.github.tncrazvan.arcano.Http.HttpController;
import java.io.IOException;
import com.github.tncrazvan.arcano.Bean.Http.HttpDefault;
import com.github.tncrazvan.arcano.Bean.Http.HttpService;
import com.github.tncrazvan.arcano.Tool.Encoding.JsonTools;
import com.github.tncrazvan.arcano.Tool.Http.Status;
import com.github.tncrazvan.arcano.Tool.System.ServerFile;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.io.File;

/**
 *
 * @author Administrator
 */
public class FileService extends HttpController {
    @HttpDefault
    public File main() throws IOException, ClassNotFoundException {
        if(reader.args.length == 0) 
            reader.args = new String[]{reader.so.config.entryPoint};
        else if(reader.args.length == 1 && reader.args[0].equals("")) 
            reader.args[0] = reader.so.config.entryPoint;
        return new Delegate<Get>(){}.start().file();
    }
    
    @HttpService(path = "/pack")
    public synchronized void pack() throws IOException{
        if(reader.args.length == 0){
            this.setResponseStatus(Status.STATUS_BAD_REQUEST);
            return;
        }
        if(!reader.args[0].endsWith(".json")){
            this.setResponseStatus(Status.STATUS_BAD_REQUEST);
            return;
        }
        
        ServerFile f = new ServerFile(reader.so.config.webRoot,reader.args[0]);
        if(!f.exists()){
            this.setResponseStatus(Status.STATUS_BAD_REQUEST);
            return;
        }
            
        JsonArray arr = JsonTools.jsonArray(f.readString(reader.so.config.charset));
        String item;
        String js = "";
        String css = "";
        System.out.println("================================");
        for(JsonElement e : arr){
            item = e.getAsString();
            ServerFile current = new ServerFile(reader.so.config.webRoot,item);
            if(!current.exists()) continue;
            if(item.trim().endsWith(".css")){
                css += current.readString(reader.so.config.charset)+"\n";
                System.out.println(item);
            }else if(item.trim().endsWith(".js")){
                js += current.readString(reader.so.config.charset)+"\n";
                System.out.println(item);
            }
        }
        ServerFile mainCSS = new ServerFile(reader.so.config.webRoot,"pack/main.css");
        ServerFile mainJS = new ServerFile(reader.so.config.webRoot,"pack/main.js");
        mainCSS.getParentFile().mkdirs();
        mainJS.getParentFile().mkdirs();
        if(!mainCSS.exists())
            mainCSS.createNewFile();
        if(!mainJS.exists())
            mainJS.createNewFile();
        mainCSS.write(css, reader.so.config.charset);
        mainJS.write(js, reader.so.config.charset);
    }
}
