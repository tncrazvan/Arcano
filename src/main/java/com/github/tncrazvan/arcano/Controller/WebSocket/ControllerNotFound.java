package com.github.tncrazvan.arcano.Controller.WebSocket;

import com.github.tncrazvan.arcano.WebSocket.WebSocketController;
import com.github.tncrazvan.arcano.WebSocket.WebSocketMessage;
import com.github.tncrazvan.arcano.Bean.Web.WebPathNotFound;

/**
 *
 * @author razvan
 */
@WebPathNotFound
public class ControllerNotFound extends WebSocketController{
    @Override
    public void onOpen() {
        close();
    }

    @Override
    public void onMessage(WebSocketMessage message) {}

    @Override
    public void onClose() {}
}