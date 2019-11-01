/**
 * CatPaw is a Java library that makes it easier
 * to program and manage a Java Web Server by providing different tools
 * such as:
 * 1) An MVC (Model-View-Controller) alike design pattern to manage 
 *    client requests without using any URL rewriting rules.
 * 2) A WebSocket Manager, allowing the server to accept and manage 
 *    incoming WebSocket connections.
 * 3) Direct access to every socket bound to every client application.
 * 4) Direct access to the headers of the incomming and outgoing Http messages.
 * Copyright (C) 2016-2018  Tanase Razvan Catalin
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package space.catpaw.Controller.Http;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import space.catpaw.Controller.WebSocket.WebSocketGroupApplicationProgramInterface;
import java.io.IOException;
import space.catpaw.Http.HttpController;
import space.catpaw.WebSocket.WebSocketGroup;
import java.io.UnsupportedEncodingException;
import space.catpaw.Http.HttpResponse;
import java.io.File;
import space.catpaw.Bean.Web;
/**
 *
 * @author Razvan
 */
@Web(path = "/get")
public class Get extends HttpController{
    @Web(path="/file")
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
    
    @Web(path="/allWebSocketGroups")
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
    
    @Web(path="/cookie")
    public void cookie() throws UnsupportedEncodingException{
        String name = String.join("/", args);
        if(e.issetCookie(name)){
            e.setContentType("application/json");
            
            String jsonCookie = jsonEncode(new Cookie("Cookie", e.getCookie(name)));
            e.send(jsonCookie);
        }else{
            e.setStatus(STATUS_NOT_FOUND);
            e.flush();
        }
    }
}
