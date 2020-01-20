package com.github.tncrazvan.arcano.Controller.Http;

import com.github.tncrazvan.arcano.Http.HttpController;
import com.github.tncrazvan.arcano.Bean.Http.HttpService;

/**
 *
 * @author Razvan
 */
public class Unset extends HttpController{
    @HttpService(path="/@unset/cookie")
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
