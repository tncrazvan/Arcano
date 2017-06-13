/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahttpserver.Controller.WebSocket;

import java.util.ArrayList;
import javahttpserver.WebSocket.WebSocketEvent;
import javahttpserver.WebSocket.WebSocketInterface;

/**
 *
 * @author Razvan
 */
public class Chat implements WebSocketInterface{

    @Override
    public void onOpen(WebSocketEvent e, ArrayList<String> args) {
        System.out.println("Chat client connected");
    }

    @Override
    public void onMessage(WebSocketEvent e, String msg, ArrayList<String> args) {
        e.broadcast(msg);
    }

    @Override
    public void onClose(WebSocketEvent e, ArrayList<String> args) {
        System.out.println("Chat client disconnected");
    }
    
}
