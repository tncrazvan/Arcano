package com.github.tncrazvan.arcano.Controller.Http;

import com.github.tncrazvan.arcano.Controller.WebSocket.WebSocketGroupApi;
import com.github.tncrazvan.arcano.Http.HttpController;
import com.github.tncrazvan.arcano.Http.HttpSession;
import com.github.tncrazvan.arcano.WebSocket.WebSocketGroup;
import java.io.UnsupportedEncodingException;
import com.github.tncrazvan.arcano.Bean.WebMethod;
import com.github.tncrazvan.arcano.Bean.WebPath;
import com.github.tncrazvan.arcano.Tool.JsonTools;
import static com.github.tncrazvan.arcano.Tool.Status.STATUS_NOT_FOUND;
import com.google.gson.JsonObject;

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
    public void webSocketGroup(){
        if(so.config.isset("groups")){
            JsonObject groups = so.config.get("groups").getAsJsonObject();
            if(groups.has("allow")){
                if(groups.get("allow").getAsBoolean()){
                    HttpSession session = startSession();
                    WebSocketGroup group = new WebSocketGroup(session);
                    if(issetRequestQueryString("visibility")){
                        group.setVisibility(Integer.parseInt(getRequestQueryString("visibility")));
                    }
                    if(issetRequestQueryString("name")){
                        group.setGroupName(getRequestQueryString("name"));
                    }
                    WebSocketGroupApi.GROUP_MANAGER.addGroup(group);
                    send(group.getKey());
                }else{
                    setResponseStatus(STATUS_NOT_FOUND);
                    send(GROUPS_NOT_ALLOWED);
                }
            }else{
                setResponseStatus(STATUS_NOT_FOUND);
                send(GROUPS_NOT_ALLOWED);
            }
        }else{
            setResponseStatus(STATUS_NOT_FOUND);
            send(GROUPS_POLICY_NOT_DEFINED);
        }
    }
    
    @WebPath(name="/cookie")
    @WebMethod(name="POST")
    public void cookie(){
        String name = String.join("/", args);
        JsonObject data = jsonObject(new String(request.content));
        setCookie(name, data.get("value").getAsString(), getRequestQueryString("path"), getRequestQueryString("path"), Integer.parseInt(getRequestQueryString("expire")));
    }
}
