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
package com.razshare.elkserver.Controller.Http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
public class Isset extends HttpController{
    @Override
    public void main(HttpEvent e, ArrayList<String> path, String content){}
    
    @Override
    public void onClose() {}
    
    public void file(HttpEvent e, ArrayList<String> path, String content) throws FileNotFoundException, IOException{
        if(path.size() >= 0){
            File f = new File(Elk.webRoot+"/"+path.get(0));
            if(f.exists()){
                e.send(0);
            }else{
                e.send(-2);
            }
        }else{
            e.send(-1);
        }
    }
    
    public void cookie(HttpEvent e, ArrayList<String> path, String content){
        if(e.getClientHeader().get("Method").equals("POST")){
            Map<String,String> multipart = readAsMultipartFormData(content);
           if(multipart.containsKey("name")){
                String name = (String) multipart.get("name");
                if(e.cookieIsset(name)){
                    e.send(0);
                }else{
                    e.send(-2);
                }
            }else{
                e.send(-1);
            } 
        }else{
            String jsonCookie = Elk.JSON_PARSER.toJson(new Cookie("Error", "-1"));
            e.send(jsonCookie);
        }
    }
}
