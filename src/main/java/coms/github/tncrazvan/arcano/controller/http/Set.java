package com.github.tncrazvan.arcano.controller.http;

import static com.github.tncrazvan.arcano.tool.encoding.JsonTools.jsonObject;
import static com.github.tncrazvan.arcano.tool.http.Status.STATUS_NOT_FOUND;

import com.github.tncrazvan.arcano.bean.http.HttpService;
import com.github.tncrazvan.arcano.controller.websocket.WebSocketGroupApi;
import com.github.tncrazvan.arcano.http.HttpController;
import com.github.tncrazvan.arcano.http.HttpSession;
import com.github.tncrazvan.arcano.websocket.WebSocketGroup;
import com.google.gson.JsonObject;

/**
 *
 * @author Razvan
 */
@HttpService(path = "/@set")
public class Set extends HttpController {
    private static final String 
            GROUPS_NOT_ALLOWED = "WebSocket groups are not allowd.";

    @HttpService(path="/webSocketGroup",method = "POST")
    public void webSocketGroup(){
        if(reader.so.config.webSocket.groups.enabled){
            final HttpSession session = startSession();
            final WebSocketGroup group = new WebSocketGroup(session);
            if (issetRequestQueryString("visibility")) {
                group.setVisibility(Integer.parseInt(getRequestQueryString("visibility")));
            }
            if (issetRequestQueryString("name")) {
                group.setGroupName(getRequestQueryString("name"));
            }
            WebSocketGroupApi.GROUP_MANAGER.addGroup(group);
            push(group.getKey());
        } else {
            setResponseStatus(STATUS_NOT_FOUND);
            push(GROUPS_NOT_ALLOWED);
        }
    }

    @HttpService(path = "/cookie",method = "POST")
    public void cookie() {
        final String name = String.join("/", reader.args);
        String tmp = new String(reader.request.content);
        final JsonObject data = jsonObject(tmp);
        setResponseCookie(name, data.get("value").getAsString(), getRequestQueryString("path"), getRequestQueryString("path"), Integer.parseInt(getRequestQueryString("expire")));
    }
}
