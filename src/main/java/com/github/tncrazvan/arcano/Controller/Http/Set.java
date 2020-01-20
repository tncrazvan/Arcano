package com.github.tncrazvan.arcano.Controller.Http;

import com.github.tncrazvan.arcano.Controller.WebSocket.WebSocketGroupApi;
import com.github.tncrazvan.arcano.Http.HttpController;
import com.github.tncrazvan.arcano.Http.HttpSession;
import com.github.tncrazvan.arcano.WebSocket.WebSocketGroup;
import static com.github.tncrazvan.arcano.Tool.Encoding.JsonTools.jsonObject;
import static com.github.tncrazvan.arcano.Tool.Http.Status.STATUS_NOT_FOUND;
import com.google.gson.JsonObject;
import com.github.tncrazvan.arcano.Bean.Http.HttpService;

/**
 *
 * @author Razvan
 */
public class Set extends HttpController {
    private static final String 
            GROUPS_NOT_ALLOWED = "WebSocket groups are not allowd.";

    @HttpService(path="/@set/webSocketGroup",method = "POST")
    public void webSocketGroup(){
        if(so.config.webSocket.groups.enabled){
            final HttpSession session = startSession();
            final WebSocketGroup group = new WebSocketGroup(session);
            if (issetRequestQueryString("visibility")) {
                group.setVisibility(Integer.parseInt(getRequestQueryString("visibility")));
            }
            if (issetRequestQueryString("name")) {
                group.setGroupName(getRequestQueryString("name"));
            }
            WebSocketGroupApi.GROUP_MANAGER.addGroup(group);
            send(group.getKey());
        } else {
            setResponseStatus(STATUS_NOT_FOUND);
            send(GROUPS_NOT_ALLOWED);
        }
    }

    @HttpService(path = "/@set/cookie",method = "POST")
    public void cookie() {
        final String name = String.join("/", args);
        final JsonObject data = jsonObject(new String(request.content));
        setResponseCookie(name, data.get("value").getAsString(), getRequestQueryString("path"), getRequestQueryString("path"), Integer.parseInt(getRequestQueryString("expire")));
    }
}
