/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elkserver.WebSocket;

import java.util.ArrayList;

/**
 *
 * @author Razvan
 */
public interface WebSocketInterface {
    public void onOpen(WebSocketEvent e, ArrayList<String> args);
    public void onMessage(WebSocketEvent e, byte[] data, ArrayList<String> args);
    public void onClose(WebSocketEvent e, ArrayList<String> args);
}
