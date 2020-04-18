package com.github.tncrazvan.arcano.controller.http;

import com.github.tncrazvan.arcano.http.HttpController;
import com.github.tncrazvan.arcano.bean.http.HttpService;

/**
 *
 * @author Razvan
 */
@HttpService(path = "/@unset")
public class Unset extends HttpController{
    @HttpService(path="/cookie")
    public void cookie(){
        final String name = String.join("/", reader.args);
        if(issetRequestCookie(name)){
            unsetResponseCookie(name, getRequestQueryString("path"), getRequestQueryString("domain"));
            push(0);
        }else{
            push(0);
        }
    }
    
}
