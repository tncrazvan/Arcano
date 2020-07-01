package com.github.tncrazvan.arcano.websocket;

import com.github.tncrazvan.arcano.WebObject;
import com.github.tncrazvan.arcano.http.HttpRequestReader;
import com.github.tncrazvan.arcano.tool.action.WebSocketEventAction;


/**
 *
 * @author Razvan Tanase
 */
public class WebSocketEvent extends WebSocketEventManager{
    private WebSocketEventAction action = null;

    public void activateWebObject(WebObject wo){
        action = wo.getWebSocketEventAction();
        this.execute();
    }

    public static final void serve(HttpRequestReader reader){
        if(reader.location.length == 0 || "".equals(reader.location[0]))
            reader.location = new String[]{"/"};
            
        String key = String.join("/",reader.location);
        WebObject wo = reader.so.WEB_SOCKET_ROUTES.get(key);
        if(wo == null){
            wo = reader.so.WEB_SOCKET_ROUTES.get("@404");
            if(wo == null){
                return;
            }
            return;
        }
        
        WebSocketController controller = new WebSocketController();
        controller.install(reader);
        controller.activateWebObject(wo);
    }

    @Override
    protected void onOpen() {
        if(action == null) 
            return;
        action.onOpen(this);
    }

    @Override
    protected void onMessage(WebSocketCommit payload) {
        if(action == null) 
            return;
        action.onMessage(this,payload);
    }

    @Override
    protected void onClose() {
        if(action == null) 
            return;
        action.onClose(this);
    }
}