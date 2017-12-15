/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elkserver.Controller.WebSocket;

import elkserver.WebSocket.WebSocketEvent;
import elkserver.WebSocket.WebSocketInterface;
import java.util.ArrayList;

/**
 *
 * @author razvan
 */
public class ControllerNotFound implements WebSocketInterface{

    @Override
    public void onOpen(WebSocketEvent e, ArrayList<String> args) {
        e.send("Elk server error: Controller not found");
        e.close();
    }

    @Override
    public void onMessage(WebSocketEvent e, byte[] data, ArrayList<String> args) {}

    @Override
    public void onClose(WebSocketEvent e, ArrayList<String> args) {}
    
}