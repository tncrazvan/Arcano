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
public class BroadcastController implements WebSocketInterface{

    @Override
    public void onOpen(WebSocketEvent e, ArrayList<String> args) {
        System.out.println("Connected.");
    }

    @Override
    public void onMessage(WebSocketEvent e, String msg, ArrayList<String> args) {
        e.broadcast(msg);
        System.out.println(msg);
        e.send("0");
    }

    @Override
    public void onClose(WebSocketEvent e, ArrayList<String> args) {
        System.out.println("Disconnected.");
    }
    
}
