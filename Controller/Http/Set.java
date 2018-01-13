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
package elkserver.Controller.Http;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import elkserver.Http.Cookie;
import elkserver.Http.HttpEvent;
import elkserver.Http.HttpInterface;
import elkserver.ELK;

/**
 *
 * @author Razvan
 */
public class Set implements HttpInterface{

    @Override
    public void main(HttpEvent e, ArrayList<String> get_data,JsonObject post_data) {}
    
    @Override
    public void onClose() {}
    
    public void cookie(HttpEvent e, ArrayList<String> get_data,JsonObject post_data){
        if(e.getMethod().equals("POST")){
            if(post_data.has("name") 
                && post_data.has("value") 
                && post_data.has("path") 
                && post_data.has("domain") 
                && post_data.has("expire")){
                e.setContentType("application/json");
                String 
                    name = post_data.get("name").getAsString(),
                    value = post_data.get("value").getAsString(),
                    path = post_data.get("path").getAsString(),
                    domain = post_data.get("domain").getAsString(),
                    expire = post_data.get("expire").getAsString();
                e.setCookie(name, value, path, domain, expire);

                String jsonCookie = ELK.JSON_PARSER.toJson(new Cookie("Cookie", value));
                e.send(jsonCookie);
                
                //System.out.println(e.getHeader().toString());
            }else{
                String jsonCookie = ELK.JSON_PARSER.toJson(new Cookie("Error", "-1"));
                e.send(jsonCookie);
            }
        }else{
            String jsonCookie = ELK.JSON_PARSER.toJson(new Cookie("Error", "-2"));
            e.send(jsonCookie);
        }
    }
}
