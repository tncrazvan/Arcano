package com.github.tncrazvan.arcano.WebSocket;

import com.github.tncrazvan.arcano.Common;

/**
 *
 * @author razvan
 */
public abstract class WebSocketController extends Common{
    protected WebSocketEvent event,e;
    protected String[] args;
    
    
    public void setEvent(WebSocketEvent event){
        this.event=event;
        this.e=this.event;
    }
    
    public void setArgs(String[] args){
        this.args=args;
    }
    
    public static final WebSocketGroupManager GROUP_MANAGER = new WebSocketGroupManager();
    public abstract void onOpen();
    public abstract void onMessage(byte[] data);
    public abstract void onClose();
    
}
