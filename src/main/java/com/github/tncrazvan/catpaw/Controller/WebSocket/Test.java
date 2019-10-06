/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.catpaw.Controller.WebSocket;

import com.github.tncrazvan.catpaw.WebSocket.WebSocketController;
import com.github.tncrazvan.catpaw.WebSocket.WebSocketEvent;

/**
 *
 * @author Administrator
 */
public class Test extends WebSocketController{

    @Override
    public void onOpen(WebSocketEvent e, String[] args) {
        System.out.println("hello");
    }

    @Override
    public void onMessage(WebSocketEvent e, byte[] data, String[] args) {
        System.out.println("message: "+new String(data));
    }

    @Override
    public void onClose(WebSocketEvent e, String[] args) {
        System.out.println("CLOSED!");
    }
    
}
