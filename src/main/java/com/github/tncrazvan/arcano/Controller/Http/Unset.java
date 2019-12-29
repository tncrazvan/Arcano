package com.github.tncrazvan.arcano.Controller.Http;

import com.github.tncrazvan.arcano.Http.HttpController;
import com.github.tncrazvan.arcano.Bean.Web.HttpPath;

/**
 *
 * @author Razvan
 */
@HttpPath(name="/@unset")
public class Unset extends HttpController{
    @HttpPath(name="/cookie")
    public void cookie(){
        final String name = String.join("/", args);
        if(issetRequestCookie(name)){
            unsetResponseCookie(name, getRequestQueryString("path"), getRequestQueryString("domain"));
            send(0);
        }else{
            send(0);
        }
    }
    
}
