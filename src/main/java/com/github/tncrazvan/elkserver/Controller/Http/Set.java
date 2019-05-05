/**
 * ElkServer is a Java library that makes it easier
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
package com.github.tncrazvan.elkserver.Controller.Http;

import com.google.gson.JsonObject;
import com.github.tncrazvan.elkserver.Controller.WebSocket.WebSocketGroupApplicationProgramInterface;
import com.github.tncrazvan.elkserver.Http.HttpEvent;
import com.github.tncrazvan.elkserver.Http.HttpController;
import com.github.tncrazvan.elkserver.Http.HttpSession;
import com.github.tncrazvan.elkserver.Settings;
import com.github.tncrazvan.elkserver.WebSocket.WebSocketGroup;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author Razvan
 */
public class Set extends HttpController{
    private static final String 
            GROUPS_NOT_ALLOWED = "WebSocket groups are not allowd.",
            GROUPS_POLICY_NOT_DEFINED = "WebSocket groups policy is not defined by the server therefore by default it is disabled.";
    @Override
    public void main(HttpEvent e, String[] args, StringBuilder content) {}
    
    @Override
    public void onClose() {}
    
    public void webSocketGroup(HttpEvent e, String[] args, StringBuilder content) throws UnsupportedEncodingException{
        
        if(Settings.isset("groups")){
            JsonObject groups = Settings.get("groups").getAsJsonObject();
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
    
    public void cookie(HttpEvent e, String[] args, StringBuilder content) throws UnsupportedEncodingException{
        if(e.getMethod().equals("POST")){
            String name = String.join("/", args);
            JsonObject data = readAsJsonObject(content);
            try{
                e.setCookie(name, data.get("value").getAsString(), e.getUrlQuery("path"), e.getUrlQuery("path"), Integer.parseInt(e.getUrlQuery("expire")));
            }catch(NumberFormatException ex){
                e.setCookie(name, data.get("value").getAsString(), e.getUrlQuery("path"), e.getUrlQuery("path"), e.getUrlQuery("expire"));
            }
        }else{
            e.setStatus(STATUS_METHOD_NOT_ALLOWED);
            e.flush();
        }
    }
}
