package com.github.tncrazvan.arcano.Http;

import com.github.tncrazvan.arcano.SharedObject;

/**
 *
 * @author razvan
 */
public class HttpController{
    protected HttpEvent event,e;
    protected String[] args;
    protected byte[] input;
    protected SharedObject so;
    
    public void setEvent(HttpEvent event){
        this.so = event.so;
        this.event=event;
        this.e=this.event;
    }
    public void setArgs(String[] args){
        this.args=args;
    }
    public void setInput(byte[] input){
        this.input=input;
    }
}
