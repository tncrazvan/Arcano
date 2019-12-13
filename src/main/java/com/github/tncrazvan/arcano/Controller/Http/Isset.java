package com.github.tncrazvan.arcano.Controller.Http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import com.github.tncrazvan.arcano.Http.HttpController;
import com.github.tncrazvan.arcano.Bean.WebPath;

/**
 *
 * @author Razvan
 */

@WebPath(name="/isset")
public class Isset extends HttpController{
    @WebPath(name="/file")
    public void file() throws FileNotFoundException, IOException{
        String url = String.join("/", args);
        File f = new File(webRoot,url);
        if(f.exists()){
            e.setStatus(STATUS_FOUND);
        }else{
            e.setStatus(STATUS_NOT_FOUND);
        }
        e.flush();
    }
    
    @WebPath(name="/cookie")
    public void cookie(){
            String name = String.join("/",args);
            if(e.issetCookie(name)){
                e.setStatus(STATUS_FOUND);
            }else{
                e.setStatus(STATUS_NOT_FOUND);
            }
           e.flush();
    }
}
