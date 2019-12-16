package com.github.tncrazvan.arcano.Controller.Http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import com.github.tncrazvan.arcano.Http.HttpController;
import com.github.tncrazvan.arcano.Bean.WebPath;
import static com.github.tncrazvan.arcano.Tool.Status.STATUS_FOUND;
import static com.github.tncrazvan.arcano.Tool.Status.STATUS_NOT_FOUND;

/**
 *
 * @author Razvan
 */

@WebPath(name="/@isset")
public class Isset extends HttpController{
    @WebPath(name="/file")
    public void file() throws FileNotFoundException, IOException{
        String url = String.join("/", args);
        File f = new File(so.webRoot,url);
        if(f.exists()){
            e.setResponseStatus(STATUS_FOUND);
        }else{
            e.setResponseStatus(STATUS_NOT_FOUND);
        }
        e.flush();
    }
    
    @WebPath(name="/cookie")
    public void cookie(){
            String name = String.join("/",args);
            if(e.issetCookie(name)){
                e.setResponseStatus(STATUS_FOUND);
            }else{
                e.setResponseStatus(STATUS_NOT_FOUND);
            }
           e.flush();
    }
}
