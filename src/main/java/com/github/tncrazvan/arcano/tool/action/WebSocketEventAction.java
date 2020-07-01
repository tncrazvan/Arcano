package com.github.tncrazvan.arcano.tool.action;

import com.github.tncrazvan.arcano.websocket.WebSocketCommit;
import com.github.tncrazvan.arcano.websocket.WebSocketEvent;

/**
 * Make a callback and defined its return type (R) and its parameter type (P).
 * 
 * @author Razvan Tanase
 */
public interface WebSocketEventAction{
    public abstract void onOpen(WebSocketEvent e);
    public abstract void onMessage(WebSocketEvent e,WebSocketCommit commit);
    public abstract void onClose(WebSocketEvent e);
}