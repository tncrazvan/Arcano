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
package com.github.tncrazvan.catpaw.Http;

import com.github.tncrazvan.catpaw.Server;
import com.github.tncrazvan.catpaw.WebMethod;
import java.lang.reflect.Method;
import com.github.tncrazvan.catpaw.Beans.Route;

/**
 *
 * @author razvan
 */
public abstract class HttpController extends Server{
    protected HttpEvent event,e;
    protected String[] args;
    protected StringBuilder content;
    public HttpController() {
        Method[] methods = this.getClass().getDeclaredMethods();
        for(Method method : methods){
            Route http = method.getAnnotation(Route.class);
            if(http != null){
                String[] routes = http.path();
                if(routes.length > 0){
                    WebMethod wm = new WebMethod(this.getClass().getCanonicalName(), method.getName(), http.method()[0]);
                    this.routes.put(routes[0], wm);
                }
            }
        }
    }
    
    public void setEvent(HttpEvent event){
        this.event=event;
        this.e=this.event;
    }
    public void setArgs(String[] args){
        this.args=args;
    }
    public void setContent(StringBuilder content){
        this.content=content;
    }
}
