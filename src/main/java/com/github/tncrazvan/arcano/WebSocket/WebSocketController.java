package com.github.tncrazvan.arcano.WebSocket;

import com.github.tncrazvan.arcano.Http.HttpRequestReader;
import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import static com.github.tncrazvan.arcano.Tool.Encoding.Hashing.getSha1String;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

public abstract class WebSocketController extends WebSocketEvent{

    protected String[] args;
    
    public void setArgs(final String[] args) {
        this.args=args;
    }
    
    public static final WebSocketGroupManager GROUP_MANAGER = new WebSocketGroupManager();
    
    
    public final WebSocketController init(final HttpRequestReader reader){
        try{
            this.reader = reader;
            this.args = reader.args;
            this.resolveRequestId();
            this.setSharedObject(reader.so);
            this.initEventManager();
            this.findRequestLanguages();
            return this;
        } catch (final UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
