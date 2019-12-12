package com.github.tncrazvan.arcano.Controller.WebSocket;

import com.github.tncrazvan.arcano.Bean.NotFound;
import com.github.tncrazvan.arcano.WebSocket.WebSocketController;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import javax.script.ScriptException;

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