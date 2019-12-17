package com.github.tncrazvan.arcano.WebSocket;

public abstract class WebSocketController extends WebSocketEvent{

    protected String[] args;
    
    public void setArgs(String[] args){
        this.args=args;
    }
    
    public static final WebSocketGroupManager GROUP_MANAGER = new WebSocketGroupManager();
}
