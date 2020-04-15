package com.github.tncrazvan.arcano.Controller.Http;

import java.io.IOException;

import com.github.tncrazvan.arcano.SharedObject;
import com.github.tncrazvan.arcano.Bean.Http.HttpService;
import com.github.tncrazvan.arcano.Http.HttpController;
import com.github.tncrazvan.arcano.Http.HttpResponse;
import com.github.tncrazvan.arcano.Tool.System.ServerFile;

/**
 *
 * @author Administrator
 */
public class FileService extends HttpController {
    @HttpService(path = "/", method = "GET")
    public HttpResponse main() throws IOException, ClassNotFoundException {
        if(reader.args.length == 0) 
            reader.args = new String[]{reader.so.config.entryPoint};
        else if(reader.args.length == 1 && reader.args[0].equals("")) 
            reader.args[0] = reader.so.config.entryPoint;
        ServerFile response = new Delegate<Get>(){}.start().file();
        if(response.exists())
            return new HttpResponse(response);
        return SharedObject.RESPONSE_NOT_FOUND;
    }
    
    /*@HttpService(path = "/pack")
    public synchronized void pack() throws IOException{
        if(reader.args.length == 0){
            this.setResponseStatus(Status.STATUS_BAD_REQUEST);
            return;
        }
        if(!reader.args[0].endsWith(".json")){
            this.setResponseStatus(Status.STATUS_BAD_REQUEST);
            return;
        }
        
        if(!reader.so.config.pack(reader.so.config.webRoot,Strings.join(reader.args, "/")))
            this.setResponseStatus(Status.STATUS_BAD_REQUEST);
    }*/
}
