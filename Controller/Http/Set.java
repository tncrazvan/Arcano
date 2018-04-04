/**
 * ElkServer is a Java library that makes it easier
 * to program and manage a Java servlet by providing different tools
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
package com.razshare.elkserver.Controller.Http;

import com.google.gson.JsonObject;
import com.razshare.elkserver.Controller.WebSocket.WebSocketGroupApplicationProgramInterface;
import java.util.ArrayList;
import com.razshare.elkserver.Http.Cookie;
import com.razshare.elkserver.Http.HttpEvent;
import com.razshare.elkserver.Elk;
import com.razshare.elkserver.Http.HttpController;
import com.razshare.elkserver.Http.HttpSession;
import com.razshare.elkserver.Settings;
import com.razshare.elkserver.WebSocket.WebSocketGroup;
import java.util.Map;

/**
 *
 * @author Razvan
 */
public class Set extends HttpController{
    private static final String 
            GROUPS_NOT_ALLOWED = "WebSocket groups are not allowd.",
            GROUPS_POLICY_NOT_DEFINED = "WebSocket groups policy is not defined by the server therefore by default it is disabled.";
    @Override
    public void main(HttpEvent e, ArrayList<String> path, String content) {}
    
    @Override
    public void onClose() {}
    
    public void webSocketGroup(HttpEvent e, ArrayList<String> path, String content){
        
        if(Settings.isset("groups")){
            JsonObject groups = Settings.get("groups").getAsJsonObject();
            if(groups.has("allow")){
                if(groups.get("allow").getAsBoolean()){
                    HttpSession session = new HttpSession(e);
                    WebSocketGroup group = new WebSocketGroup(session);
                    WebSocketGroupApplicationProgramInterface.GROUP_MANAGER.addGroup(group);
                    e.send(group.getKey());
                }else{
                    e.setStatus(HttpEvent.STATUS_NOT_FOUND);
                    e.send(GROUPS_NOT_ALLOWED);
                }
            }else{
                e.setStatus(HttpEvent.STATUS_NOT_FOUND);
                e.send(GROUPS_NOT_ALLOWED);
            }
        }else{
            e.setStatus(HttpEvent.STATUS_NOT_FOUND);
            e.send(GROUPS_POLICY_NOT_DEFINED);
        }
        
    }
    
    public void cookie(HttpEvent e, ArrayList<String> path, String content){
        Map<String,Object> multipart = readAsMultipartFormData(content);
        
        if(e.getMethod().equals("POST")){
            if(multipart.containsKey("name") 
                && multipart.containsKey("value") 
                && multipart.containsKey("path") 
                && multipart.containsKey("domain") 
                && multipart.containsKey("expire")){
                e.setContentType("application/json");
                String 
                    name = (String) multipart.get("name"),
                    value = (String) multipart.get("value"),
                    cpath = (String) multipart.get("path"),
                    domain = (String) multipart.get("domain"),
                    expire = (String) multipart.get("expire");
                e.setCookie(name, value, cpath, domain, expire);

                String jsonCookie = Elk.JSON_PARSER.toJson(new Cookie("Cookie", value));
                e.send(jsonCookie);
                
                //System.out.println(e.getHeader().toString());
            }else{
                String jsonCookie = Elk.JSON_PARSER.toJson(new Cookie("Error", "-1"));
                e.send(jsonCookie);
            }
        }else{
            String jsonCookie = Elk.JSON_PARSER.toJson(new Cookie("Error", "-2"));
            e.send(jsonCookie);
        }
    }
}
