package com.github.tncrazvan.arcano.Controller.Http;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.github.tncrazvan.arcano.Controller.WebSocket.WebSocketGroupApi;
import java.io.IOException;
import com.github.tncrazvan.arcano.Http.HttpController;
import com.github.tncrazvan.arcano.WebSocket.WebSocketGroup;
import com.github.tncrazvan.arcano.Http.HttpResponse;
import java.io.File;
import com.github.tncrazvan.arcano.Bean.Web.WebPath;
import static com.github.tncrazvan.arcano.Tool.Encoding.JsonTools.jsonStringify;
import static com.github.tncrazvan.arcano.Tool.System.Memory.getAllocatedMemory;
import static com.github.tncrazvan.arcano.Tool.System.Memory.getFreeMemory;
import static com.github.tncrazvan.arcano.Tool.System.Memory.getMaxMemory;
import static com.github.tncrazvan.arcano.Tool.Http.Status.STATUS_NOT_FOUND;
import com.github.tncrazvan.arcano.Bean.Security.ArcanoSecret;
/**
 *
 * @author Razvan
 */
@WebPath(name = "/@get")
public class Get extends HttpController{
    
    @ArcanoSecret
    @WebPath(name="/memory")
    public JsonObject memory(){
        final JsonObject result = new JsonObject();
        result.addProperty("free", getFreeMemory());
        result.addProperty("allocated", getAllocatedMemory());
        result.addProperty("max", getMaxMemory());
        return result;
    }

    @WebPath(name = "/file")
    public HttpResponse file() throws IOException {
        return new HttpResponse(new File(so.config.webRoot + String.join("/", args)));
    }

    class Cookie {
        String type, value;

        public Cookie(final String type, final String value) {
            this.type = type;
            this.value = value;
        }

    }

    @WebPath(name = "/allWebSocketGroups")
    public void allWebSocketGroups() {
        WebSocketGroup group;
        final JsonArray arr = new JsonArray();
        for (final String key : WebSocketGroupApi.GROUP_MANAGER.getAllGroups().keySet()) {
            group = WebSocketGroupApi.GROUP_MANAGER.getGroup(key);
            if (group.getVisibility() == WebSocketGroup.PUBLIC) {
                final JsonObject o = new JsonObject();
                o.addProperty("name", group.getGroupName());
                o.addProperty("id", key);
                arr.add(o);
            }

        }
        send(arr.toString());
    }

    @WebPath(name = "/cookie")
    public void cookie() {
        final String name = String.join("/", args);
        if (issetRequestCookie(name)) {
            setResponseContentType("application/json");
            final String jsonCookie = jsonStringify(new Cookie("Cookie", getRequestCookie(name)));
            send(jsonCookie);
        }else{
            setResponseStatus(STATUS_NOT_FOUND);
            flush();
        }
    }
}
