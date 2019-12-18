package com.github.tncrazvan.arcano.Controller.Http;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.github.tncrazvan.arcano.Controller.WebSocket.WebSocketGroupApi;
import java.io.IOException;
import com.github.tncrazvan.arcano.Http.HttpController;
import com.github.tncrazvan.arcano.WebSocket.WebSocketGroup;
import java.io.UnsupportedEncodingException;
import com.github.tncrazvan.arcano.Http.HttpResponse;
import java.io.File;
import com.github.tncrazvan.arcano.Bean.WebPath;
import com.github.tncrazvan.arcano.Tool.JsonTools;
import static com.github.tncrazvan.arcano.Tool.Status.STATUS_NOT_FOUND;
/**
 *
 * @author Razvan
 */
@WebPath(name = "/@get")
public class Get extends HttpController implements JsonTools{
    @WebPath(name="/file")
    public HttpResponse file() throws IOException {
        return new HttpResponse(new File(so.webRoot+String.join("/", args)));
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
        WebSocketGroupApi
        .GROUP_MANAGER
        .getAllGroups().keySet()){
            group = WebSocketGroupApi
                            .GROUP_MANAGER
                            .getGroup(key);
            if(group.getVisibility() == WebSocketGroup.PUBLIC){
                JsonObject o = new JsonObject();
                o.addProperty("name", group.getGroupName());
                o.addProperty("id", key);
                arr.add(o);
            }
            
        }
        send(arr.toString());
    }
    
    @WebPath(name="/cookie")
    public void cookie(){
        String name = String.join("/", args);
        if(issetCookie(name)){
            setResponseContentType("application/json");
            String jsonCookie = jsonStringify(new Cookie("Cookie", getCookie(name)));
            send(jsonCookie);
        }else{
            setResponseStatus(STATUS_NOT_FOUND);
            flush();
        }
    }
}
