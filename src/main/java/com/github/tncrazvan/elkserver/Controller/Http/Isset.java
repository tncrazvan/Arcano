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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import com.github.tncrazvan.elkserver.Http.Cookie;
import com.github.tncrazvan.elkserver.Http.HttpEvent;
import com.github.tncrazvan.elkserver.Elk;
import com.github.tncrazvan.elkserver.Http.HttpController;
import java.util.Map;

/**
 *
 * @author Razvan
 */
public class Isset extends HttpController{
    @Override
    public void main(HttpEvent e, String[] args, StringBuilder content){}
    
    @Override
    public void onClose() {}
    
    public void file(HttpEvent e, String[] args, StringBuilder content) throws FileNotFoundException, IOException{
        String url = String.join("/", args);
        File f = new File(Elk.webRoot,url);
        if(f.exists()){
            e.setStatus(STATUS_FOUND);
        }else{
            e.setStatus(STATUS_NOT_FOUND);
        }
        e.flush();
    }
    
    public void cookie(HttpEvent e, String[] args, StringBuilder content){
            String name = String.join("/",args);
            if(e.cookieIsset(name)){
                e.setStatus(STATUS_FOUND);
            }else{
                e.setStatus(STATUS_NOT_FOUND);
            }
           e.flush();
    }
}
