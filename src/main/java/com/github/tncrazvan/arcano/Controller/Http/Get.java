package com.github.tncrazvan.arcano.Controller.Http;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.github.tncrazvan.arcano.Controller.WebSocket.WebSocketGroupApplicationProgramInterface;
import java.io.IOException;
import com.github.tncrazvan.arcano.Http.HttpController;
import com.github.tncrazvan.arcano.WebSocket.WebSocketGroup;
import java.io.UnsupportedEncodingException;
import com.github.tncrazvan.arcano.Http.HttpResponse;
import java.io.File;
import com.github.tncrazvan.arcano.Bean.WebPath;
/**
 *
 * @author Razvan
 */
@WebPath(name = "/get")
public class Get extends HttpController{
    @WebPath(name="/file")
    public HttpResponse file() throws IOException {
        return new HttpResponse(null,new File(webRoot+String.join("/", args)));
    }
    
    class Cookie{
        String 
                type,
                value;

        public Cookie(String type,String value) {
            this.type=type;
            this.value=value;
        }
        
    }
    
    @WebPath(name="/allWebSocketGroups")
    public void allWebSocketGroups(){
        WebSocketGroup group;
        JsonArray arr = new JsonArray();
        for(String key : 
        WebSocketGroupApplicationProgramInterface
        .GROUP_MANAGER
        .getAllGroups().keySet()){
            group = WebSocketGroupApplicationProgramInterface
                            .GROUP_MANAGER
                            .getGroup(key);
            if(group.getVisibility() == WebSocketGroup.PUBLIC){
                JsonObject o = new JsonObject();
                o.addProperty("name", group.getGroupName());
                o.addProperty("id", key);
                arr.add(o);
            }
            
        }
        e.send(arr.toString());
    }
    
    @WebPath(name="/cookie")
    public void cookie() throws UnsupportedEncodingException{
        String name = String.join("/", args);
        if(e.issetCookie(name)){
            e.setContentType("application/json");
            
            String jsonCookie = jsonEncodeObject(new Cookie("Cookie", e.getCookie(name)));
            e.send(jsonCookie);
        }else{
            e.setStatus(STATUS_NOT_FOUND);
            e.flush();
        }
    }
}
