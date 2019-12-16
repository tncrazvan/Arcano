package com.github.tncrazvan.arcano.Controller.WebSocket;

import com.github.tncrazvan.arcano.Bean.NotFound;
import com.github.tncrazvan.arcano.WebSocket.WebSocketController;

/**
 *
 * @author razvan
 */
@NotFound
public class ControllerNotFound extends WebSocketController{
    @Override
    public void onOpen() {
        e.close();
    }

    @Override
    public void onMessage(byte[] data) {}

    @Override
    public void onClose() {}
}