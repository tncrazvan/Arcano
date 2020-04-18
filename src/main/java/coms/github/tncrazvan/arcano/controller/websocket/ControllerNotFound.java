package com.github.tncrazvan.arcano.controller.websocket;

import com.github.tncrazvan.arcano.bean.websocket.WebSocketControllerNotFound;
import com.github.tncrazvan.arcano.websocket.WebSocketCommit;
import com.github.tncrazvan.arcano.websocket.WebSocketController;

/**
 *
 * @author razvan
 */
@WebSocketControllerNotFound
public class ControllerNotFound extends WebSocketController{
    @Override
    public void onOpen() {
        close();
    }

    @Override
    public void onMessage(final WebSocketCommit message) {}

    @Override
    public void onClose() {}
}