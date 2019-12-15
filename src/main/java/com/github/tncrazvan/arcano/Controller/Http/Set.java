package com.github.tncrazvan.arcano.Controller.Http;

import com.google.gson.JsonObject;
import com.github.tncrazvan.arcano.Controller.WebSocket.WebSocketGroupApplicationProgramInterface;
import com.github.tncrazvan.arcano.Http.HttpEvent;
import com.github.tncrazvan.arcano.Http.HttpController;
import com.github.tncrazvan.arcano.Http.HttpSession;
import com.github.tncrazvan.arcano.WebSocket.WebSocketGroup;
import java.io.UnsupportedEncodingException;
import com.github.tncrazvan.arcano.Bean.WebMethod;
import com.github.tncrazvan.arcano.Bean.WebPath;
import com.github.tncrazvan.arcano.Tool.JsonTools;
import static com.github.tncrazvan.arcano.Tool.Status.STATUS_NOT_FOUND;

/**
 *
 * @author Razvan
 */
@WebPath(name="/@set")
public class Set extends HttpController implements JsonTools{
    private static final String 
            GROUPS_NOT_ALLOWED = "WebSocket groups are not allowd.",
            GROUPS_POLICY_NOT_DEFINED = "WebSocket groups policy is not defined by the server therefore by default it is disabled.";

    @WebPath(name="/webSocketGroup")
    @WebMethod(name="POST")
    public void webSocketGroup() throws UnsupportedEncodingException{
        if(so.config.isset("groups")){
            JsonObject groups = so.config.get("groups").getAsJsonObject();
            if(groups.has("allow")){
                if(groups.get("allow").getAsBoolean()){
                    HttpSession session = e.startSession();
                    WebSocketGroup group = new WebSocketGroup(session);
                    if(e.issetUrlQuery("visibility")){
                        group.setVisibility(Integer.parseInt(e.getUrlQuery("visibility")));
                    }
                    if(e.issetUrlQuery("name")){
                        group.setGroupName(e.getUrlQuery("name"));
                    }
                    WebSocketGroupApplicationProgramInterface.GROUP_MANAGER.addGroup(group);
                    e.send(group.getKey());
                }else{
                    e.setStatus(STATUS_NOT_FOUND);
                    e.send(GROUPS_NOT_ALLOWED);
                }
            }else{
                e.setStatus(STATUS_NOT_FOUND);
                e.send(GROUPS_NOT_ALLOWED);
            }
        }else{
            e.setStatus(STATUS_NOT_FOUND);
            e.send(GROUPS_POLICY_NOT_DEFINED);
        }
    }
    
    @WebPath(name="/cookie")
    @WebMethod(name="POST")
    public void cookie() throws UnsupportedEncodingException{
        String name = String.join("/", args);
        JsonObject data = jsonObject(new String(input));
        try{
            e.setCookie(name, data.get("value").getAsString(), e.getUrlQuery("path"), e.getUrlQuery("path"), Integer.parseInt(e.getUrlQuery("expire")));
        }catch(NumberFormatException ex){
            e.setCookie(name, data.get("value").getAsString(), e.getUrlQuery("path"), e.getUrlQuery("path"), e.getUrlQuery("expire"));
        }
    }
}
