/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.catpaw.Controller.WebSocket;

import com.github.tncrazvan.catpaw.WebSocket.WebSocketController;
import com.github.tncrazvan.catpaw.Beans.Web;

/**
 *
 * @author Administrator
 */
@Web(path = "/Test")
public class Test extends WebSocketController{
    @Override
    public void onOpen() {
        System.out.println("hello"); //To change body of generated methods, choose Tools | Templates.
        e.send("hello");
    }

    @Override
    public void onMessage(byte[] data) {
        e.send(new String(data));
        System.out.println("message: "+new String(data)); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onClose() {
        System.out.println("CLOSED!");
    }
}
