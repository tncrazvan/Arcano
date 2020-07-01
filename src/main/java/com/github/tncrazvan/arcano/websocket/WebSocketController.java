package com.github.tncrazvan.arcano.websocket;

import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import com.github.tncrazvan.arcano.http.HttpHeaders;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import com.github.tncrazvan.arcano.http.HttpRequestReader;

/**
 *
 * @author Razvan Tanase
 */
public class WebSocketController extends WebSocketEvent{

    protected String[] args;
    
    public void setArgs(final String[] args) {
        this.args=args;
    }
    
    public static final WebSocketGroupManager GROUP_MANAGER = new WebSocketGroupManager();
    
    
    public final WebSocketController install(final HttpRequestReader reader){
        try{
            this.reader = reader;
            this.args = reader.args;
            this.setResponseHttpHeaders(HttpHeaders.response());
            this.resolveRequestId();
            this.initEventManager();
            this.findRequestLanguages();
            return this;
        } catch (final UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
