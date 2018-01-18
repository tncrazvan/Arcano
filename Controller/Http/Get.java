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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import elkserver.Http.HttpEvent;
import elkserver.ELK;
import elkserver.Http.HttpController;

/**
 *
 * @author Razvan
 */
public class Get extends HttpController{
    @Override
    public void main(HttpEvent e, ArrayList<String> get_data, JsonObject post_data){}
    
    @Override
    public void onClose() {}
    
    public void file(HttpEvent e, ArrayList<String> get_data, JsonObject post_data) throws FileNotFoundException, IOException{
        e.setContentType(ELK.processContentType(get_data.get(0)));
        e.sendFileContents("/"+(get_data.get(0).equals("")?get_data.get(1):get_data.get(0)));
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
    
    public void cookie(HttpEvent e, ArrayList<String> get_data, JsonObject post_data){

        if(e.getMethod().equals("POST")){
            if(post_data.has("name")){
                String name = post_data.get("name").getAsString();
                if(e.cookieIsset(name)){
                    e.setContentType("application/json");
                    String jsonCookie = ELK.JSON_PARSER.toJson(new Cookie("Cookie", e.getCookie(name)));
                    e.send(jsonCookie);
                }else{
                    e.setContentType("text/plain");
                    e.setHeaderField("Status", "HTTP/1.1 404 Not Found");
                    e.flush();
                }
            }else{
                e.setContentType("text/plain");
                e.setHeaderField("Status", "HTTP/1.1 404 Not Found");
                e.flush();
            }
        }else{
            String jsonCookie = ELK.JSON_PARSER.toJson(new elkserver.Http.Cookie("Error", "-1"));
            e.send(jsonCookie);
        }
    }
}
