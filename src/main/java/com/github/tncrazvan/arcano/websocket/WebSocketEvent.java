package com.github.tncrazvan.arcano.websocket;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import com.github.tncrazvan.arcano.SharedObject;
import com.github.tncrazvan.arcano.WebObject;
import com.github.tncrazvan.arcano.http.HttpHeaders;
import com.github.tncrazvan.arcano.http.HttpRequestReader;
import com.github.tncrazvan.arcano.tool.action.WebSocketEventAction;


/**
 *
 * @author Razvan Tanase
 */
public class WebSocketEvent extends WebSocketEventManager{
    private WebSocketEventAction action = null;
    protected String[] args;
    public static final WebSocketGroupManager GROUP_MANAGER = new WebSocketGroupManager();
    
    public WebSocketEvent(HttpRequestReader reader, SharedObject so, WebObject wo) throws UnsupportedEncodingException {
        super(reader,so);

        this.request.reader = reader;
        this.args = reader.args;

        this.response.setHttpHeaders(HttpHeaders.response());
        this.request.resolveId();
        this.request.findLanguages();
    
        this.activateWebObject(wo);
    }

    public void setArgs(final String[] args) {
        this.args=args;
    }

    public void activateWebObject(WebObject wo){
        action = wo.getWebSocketEventAction();
        this.execute();
    }

    public static final void serve(HttpRequestReader reader, SharedObject so) throws UnsupportedEncodingException {
        if(reader.location.length == 0 || "".equals(reader.location[0]))
            reader.location = new String[]{"/"};
            
        String key = String.join("/",reader.location);
        WebObject wo = so.WEB_SOCKET_ROUTES.get(key);
        if(wo == null){
            wo = so.WEB_SOCKET_ROUTES.get("@404");
            if(wo == null){
                return;
            }
            return;
        }
        
        new WebSocketEvent(reader,so,wo);
    }

    @Override
    protected void onOpen() {
        if(action == null) 
            return;
        action.onOpen(this);
    }

    @Override
    protected void onMessage(WebSocketCommit commit) {
        if(action == null) 
            return;
        action.onMessage(this,commit);
    }

    @Override
    protected void onClose() {
        if(action == null) 
            return;
        action.onClose(this);
    }
}