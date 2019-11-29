package com.github.tncrazvan.arcano.Http;

import com.github.tncrazvan.arcano.Common;

/**
 *
 * @author razvan
 */
public class HttpController extends Common{
    protected HttpEvent event,e;
    protected String[] args;
    protected byte[] input;
    
    public void setEvent(HttpEvent event){
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
