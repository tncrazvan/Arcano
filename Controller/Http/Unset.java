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

import java.util.ArrayList;
import com.razshare.elkserver.Http.Cookie;
import com.razshare.elkserver.Http.HttpEvent;
import com.razshare.elkserver.Elk;
import com.razshare.elkserver.Http.HttpController;
import java.util.Map;

/**
 *
 * @author Razvan
 */
public class Unset extends HttpController{

    @Override
    public void main(HttpEvent e, ArrayList<String> path, String content) {}
    
    @Override
    public void onClose() {}
    
    public void cookie(HttpEvent e, ArrayList<String> path, String content){
        Map<String,String> multipart = readAsMultipartFormData(content);
        
        if(e.getMethod().equals("POST")){
            if(multipart.containsKey("name") 
                    && multipart.containsKey("domain") 
                    && multipart.containsKey("path")){
                String name = (String) multipart.get("name");
                if(e.cookieIsset(name)){
                    e.unsetCookie(name, (String) multipart.get("path"), (String) multipart.get("domain"));
                    e.send(0);
                }else{
                    e.send(0);
                }
            }else{
                e.setStatus(HttpEvent.STATUS_NOT_FOUND);
                e.flushHeaders();
            }
        }else{
            String jsonCookie = Elk.JSON_PARSER.toJson(new Cookie("Error", "-1"));
            e.send(jsonCookie);
        }
    }
    
}
