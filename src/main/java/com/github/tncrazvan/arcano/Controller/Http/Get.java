package com.github.tncrazvan.arcano.Controller.Http;

import static com.github.tncrazvan.arcano.Tool.Encoding.JsonTools.jsonStringify;
import static com.github.tncrazvan.arcano.Tool.Http.Status.STATUS_NOT_FOUND;
import static com.github.tncrazvan.arcano.Tool.System.Memory.getFreeMemory;
import static com.github.tncrazvan.arcano.Tool.System.Memory.getMaxMemory;
import static com.github.tncrazvan.arcano.Tool.System.Memory.getTotalMemory;

import java.io.IOException;

import com.github.tncrazvan.arcano.Bean.Http.HttpService;
import com.github.tncrazvan.arcano.Controller.WebSocket.WebSocketGroupApi;
import com.github.tncrazvan.arcano.Http.HttpController;
import com.github.tncrazvan.arcano.Tool.System.ServerFile;
import com.github.tncrazvan.arcano.WebSocket.WebSocketGroup;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
/**
 *
 * @author Razvan
 */
@HttpService(path = "/@get")
public class Get extends HttpController{
    
    @HttpService(path="/memory")
    public JsonObject memory(){
        final JsonObject result = new JsonObject();
        result.addProperty("free", getFreeMemory());
        result.addProperty("allocated", getTotalMemory());
        result.addProperty("max", getMaxMemory());
        return result;
    }

    @HttpService(path = "/file")
    public ServerFile file() throws IOException {
        ServerFile file = new ServerFile(reader.so.config.webRoot, String.join("/", reader.args));
        if(this.issetRequestHeaderField("Range"))
            file.resolveRangesFromHeader(this.getRequestHeaderField("Range"));
        return file;
    }

    class Cookie {
        String type, value;

        public Cookie(final String type, final String value) {
            this.type = type;
            this.value = value;
        }

    }

    @HttpService(path = "/allWebSocketGroups")
    public String allWebSocketGroups() {
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
        return arr.toString();
    }

    @HttpService(path = "/cookie")
    public String cookie() {
        final String name = String.join("/", reader.args);
        if (issetRequestCookie(name)) {
            setResponseContentType("application/json");
            final String jsonCookie = jsonStringify(new Cookie("Cookie", getRequestCookie(name)));
            return jsonCookie;
        }else{
            setResponseStatus(STATUS_NOT_FOUND);
            return "";
        }
    }
}
