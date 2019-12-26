package com.github.tncrazvan.arcano.Controller.Http;

import com.github.tncrazvan.arcano.Http.HttpController;
import java.io.UnsupportedEncodingException;
import com.github.tncrazvan.arcano.Bean.Web.WebPath;

/**
 *
 * @author Razvan
 */
@WebPath(name="/@unset")
public class Unset extends HttpController{
    @WebPath(name="/cookie")
    public void cookie(){
        String name = String.join("/", args);
        if(issetRequestCookie(name)){
            unsetResponseCookie(name, getRequestQueryString("path"), getRequestQueryString("domain"));
            send(0);
        }else{
            send(0);
        }
    }
    
}
